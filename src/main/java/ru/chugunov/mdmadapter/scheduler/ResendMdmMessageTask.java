package ru.chugunov.mdmadapter.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Value("${mdm.scheduler.resend-mdm-message-outbox.page-number}")
    private int pageNumber;
    @Value("${mdm.scheduler.resend-mdm-message-outbox.page-size}")
    private int pageSize;

    @Async("resendMdmMessageOutboxExecutor")
    @Scheduled(cron = "${mdm.scheduler.resend-mdm-message-outbox.cron}")
    public void resendMdmMessageOutbox() {
        log.info("[Scheduler] Повторная попытка отправки событий в сервисы ...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fifteenMinutesAgo = now.minusMinutes(15);
        LocalDateTime dayAgo = now.minusDays(1);


        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        Page<MdmMessageOutbox> pageForRetry = mdmMessageOutboxRepository.findMdmMessageForRetry(fifteenMinutesAgo,
                                                                                                dayAgo,
                                                                                                List.of("NEW", "ERROR"),
                                                                                                pageRequest);

        if (pageForRetry.getContent().isEmpty()) {
            log.info("[Scheduler] Не нашлось событий для повторной отправки в сервисы");
        } else {
            for (MdmMessageOutbox mdmMessageOutbox : pageForRetry.getContent()) {
                mdmOutboxSender.sendOutbox(mdmMessageOutbox);
            }
        }

        log.info("[Scheduler] Было совершена попытка переотправки {} событий в сервисы",
                pageForRetry.getContent().size());
    }
}
