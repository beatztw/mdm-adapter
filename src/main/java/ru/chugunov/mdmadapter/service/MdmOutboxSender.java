package ru.chugunov.mdmadapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chugunov.mdmadapter.exeption.BusinessException;
import ru.chugunov.mdmadapter.exeption.MdmMessageNotFoundException;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxStatus;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.repository.MdmMessageRepository;
import ru.chugunov.mdmadapter.service.strategy.MdmMessageOutboxStrategy;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class MdmOutboxSender {

    private final JsonUtils jsonUtils;
    private final ExecutorService outboxElasticExecutor;
    private final MdmMessageRepository mdmMessageRepository;
    private final MdmMessageOutboxRepository mdmMessageOutboxRepository;
    private final Map<MdmMessageOutboxTarget, MdmMessageOutboxStrategy> mdmMessageOutboxMap;

    public CompletableFuture<Void> sendOutbox(MdmMessageOutbox outbox) {
        MdmMessage mdmMessage = mdmMessageRepository.findById(outbox.getMdmMessageId())
                .orElseThrow(MdmMessageNotFoundException::new);

        return CompletableFuture.runAsync(() -> {
            try {
                MdmMessageOutboxStrategy messageOutboxStrategy = mdmMessageOutboxMap.get(outbox.getTarget());

                messageOutboxStrategy.send(mdmMessage, outbox);

            } catch (BusinessException e) {
                log.error("Превышено время ожидания от сервиса при отправке события id={}, target={}",
                        outbox.getMdmMessageId(), outbox.getTarget());

                handleError(outbox, e);
            }
            catch (Exception errorMessage) {
                log.error("При отправке события id={}, target={} в сервисы возникла непредвиденная ошибка",
                        outbox.getMdmMessageId(), outbox.getTarget());

                handleFatalError(outbox, errorMessage);
            }

        }, outboxElasticExecutor);
    }


    private void handleFatalError(MdmMessageOutbox outbox, Exception e) {
        outbox.setStatus(MdmMessageOutboxStatus.FATAL_ERROR);
        outbox.setResponseData(jsonUtils.toJson(Map.of("errors", List.of(e))));

        mdmMessageOutboxRepository.save(outbox);
    }

    private void handleError(MdmMessageOutbox outbox, Exception e) {
        outbox.setStatus(MdmMessageOutboxStatus.ERROR);
        outbox.setResponseData(jsonUtils.toJson(Map.of("response", List.of(e))));

        mdmMessageOutboxRepository.save(outbox);
    }

}
