package ru.chugunov.mdmadapter.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.chugunov.mdmadapter.exeption.BusinessException;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.property.MdmResendMessageProperty;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.service.MdmOutboxSender;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResendMdmMessageTask {

    private final MdmMessageOutboxRepository mdmMessageOutboxRepository;
    private final MdmOutboxSender mdmOutboxSender;
    private final MdmResendMessageProperty property;

    @Async("resendMdmMessageOutboxExecutor")
    @Scheduled(cron = "${mdm.scheduler.resend-mdm-message-outbox.cron}")
    public void resendMdmMessageOutbox() {
        log.info("Повторная попытка отправки событий в сервисы");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime updateTimeTo = now.minusMinutes(property.getUpdateTimeToMinutes());
        LocalDateTime updateTimeFrom = now.minusMinutes(property.getUpdateTimeFromMinutes());

        int totalFailed = 0;
        int totalSuccess = 0;
        Long lastMessageId = 0L;
        int pageSizeForRetry = property.getPageSize();
        List<MdmMessageOutbox> messagesForRetry;

        do {
            messagesForRetry = mdmMessageOutboxRepository.findMdmMessageForRetry(
                    updateTimeTo,
                    updateTimeFrom,
                    List.of("NEW", "ERROR"),
                    lastMessageId,
                    Pageable.ofSize(pageSizeForRetry)
            );

            if (messagesForRetry.isEmpty()) {
                break;
            }

            for (MdmMessageOutbox message : messagesForRetry) {
                try {
                    mdmOutboxSender.sendOutbox(message).join();

                    totalSuccess++;
                } catch (BusinessException e) {
                    totalFailed++;
                    log.error("Не удалось повторно отправить событие id={}, target={}. Возникла ошибка {}",
                            message.getId(), message.getTarget(), e.getMessage(), e);
                } catch (Exception e) {
                    totalFailed++;
                    log.error("Произошла непредвиденная ошибка при повторной отправки события id={}, target={}",
                            message.getMdmMessageId(), message.getTarget());
                }

                lastMessageId = message.getId();
            }
        } while (messagesForRetry.size() == pageSizeForRetry);

        log.info("Была совершена попытка переотправки событий в сервисы. Успешно отправлено {}. Не удалось отправить {}",
                totalSuccess, totalFailed);
    }
}
