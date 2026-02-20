package ru.chugunov.mdmadapter.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
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

    @Value("${mdm.scheduler.resend-mdm-message-outbox.page-size}")
    private int pageSize;

    @Async("resendMdmMessageOutboxExecutor")
    @Scheduled(cron = "${mdm.scheduler.resend-mdm-message-outbox.cron}")
    public void resendMdmMessageOutbox() {
        log.info("Повторная попытка отправки событий в сервисы");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fifteenMinutesAgo = now.minusMinutes(15);
        LocalDateTime dayAgo = now.minusDays(1);

        Long lastMessageId = 0L;
        List<MdmMessageOutbox> messagesForRetry;

        do {
            messagesForRetry = mdmMessageOutboxRepository.findMdmMessageForRetry(fifteenMinutesAgo,
                    dayAgo,
                    List.of("NEW", "ERROR"),
                    lastMessageId);

            for (MdmMessageOutbox message : messagesForRetry) {
                try {
                    mdmOutboxSender.sendOutbox(message);
                } catch (Exception e) {
                    log.error("Не удалось повторно отправить событие id={}, target={} возникла ошибка: {}",
                            message.getMdmMessageId(), message.getTarget(), e.getMessage());
                }
                lastMessageId = message.getId();

            }
        } while (!messagesForRetry.isEmpty());

//        log.info("Было совершена попытка переотправки {} событий в сервисы",
//                messageForRetry.getContent().size());
    }
}
