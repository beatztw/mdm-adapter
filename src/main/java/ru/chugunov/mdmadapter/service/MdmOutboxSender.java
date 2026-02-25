package ru.chugunov.mdmadapter.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chugunov.mdmadapter.dto.responses.*;
import ru.chugunov.mdmadapter.exeption.BusinessException;
import ru.chugunov.mdmadapter.exeption.MdmMessageNotFoundException;
import ru.chugunov.mdmadapter.exeption.SendOutboxTimeoutException;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxStatus;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.repository.MdmMessageRepository;
import ru.chugunov.mdmadapter.service.strategy.MdmMessageOutboxStrategy;
import ru.chugunov.mdmadapter.service.strategy.UserDataServiceOneStrategy;
import ru.chugunov.mdmadapter.service.strategy.UserDataServiceTwoStrategy;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MdmOutboxSender {

    private final JsonUtils jsonUtils;
    private final MdmMessageRepository mdmMessageRepository;
    private final ExecutorService processOutboxEventExecutor;
    private final MdmMessageOutboxRepository mdmMessageOutboxRepository;
    private final Map<MdmMessageOutboxTarget, MdmMessageOutboxStrategy<?>> mdmMessageOutboxMap;

    public CompletableFuture<Void> sendOutbox(MdmMessageOutbox outbox) {
        MdmMessage mdmMessage = mdmMessageRepository.findById(outbox.getMdmMessageId())
                .orElseThrow(MdmMessageNotFoundException::new);

        MdmMessageOutboxStrategy<?> messageOutboxStrategy = mdmMessageOutboxMap.get(outbox.getTarget());

        if (messageOutboxStrategy instanceof UserDataServiceOneStrategy) {
            return handleUserDataServiceOne(outbox, mdmMessage, (UserDataServiceOneStrategy) messageOutboxStrategy);
        } else if (messageOutboxStrategy instanceof UserDataServiceTwoStrategy) {
            return handleUserDataServiceTwo(outbox, mdmMessage, (UserDataServiceTwoStrategy) messageOutboxStrategy);
        } else {
            handleFatalError(outbox,
                    new BusinessException("Неизвестный тип стратегии: " + messageOutboxStrategy.getTarget())
            );

            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> handleUserDataServiceOne(MdmMessageOutbox outbox,
                                                             MdmMessage mdmMessage,
                                                             UserDataServiceOneStrategy strategy) {

        return strategy.send(mdmMessage, outbox)
                .thenAcceptAsync(response -> handleUserDataServiceOneResponse(outbox, response),
                        processOutboxEventExecutor)
                .exceptionallyAsync(ex -> {
                            handleOutboxMessageException(outbox, ex, strategy.getServiceName());

                            throw new BusinessException(ex);
                        },
                        processOutboxEventExecutor);
    }

    private void handleUserDataServiceOneResponse(MdmMessageOutbox outbox, UserDataServiceOneResponse response) {
        UserDataServiceOneResponseBody body = response.getBody();

        if (ServiceResponseStatus.SUCCESS.equals(body.getStatus())) {
            outbox.setStatus(MdmMessageOutboxStatus.DELIVERED);
        } else {
            outbox.setStatus(MdmMessageOutboxStatus.ERROR);
        }

        outbox.setResponseData(jsonUtils.toJson(Map.of("response", body)));
        mdmMessageOutboxRepository.save(outbox);
    }

    private CompletableFuture<Void> handleUserDataServiceTwo(MdmMessageOutbox outbox,
                                                             MdmMessage mdmMessage,
                                                             UserDataServiceTwoStrategy strategy) {

        return strategy.send(mdmMessage, outbox)
                .thenAcceptAsync(response -> handleUserDataServiceTwoResponse(outbox, response),
                        processOutboxEventExecutor)
                .exceptionallyAsync(ex -> {
                            handleOutboxMessageException(outbox, ex, strategy.getServiceName());

                            throw new BusinessException(ex);
                        },
                        processOutboxEventExecutor);
    }

    private void handleUserDataServiceTwoResponse(MdmMessageOutbox outbox, UserDataServiceTwoResponse response) {
        UserDataServiceTwoResponseBody body = response.getBody();

        if (ServiceResponseStatus.SUCCESS.equals(body.getStatus())) {
            outbox.setStatus(MdmMessageOutboxStatus.DELIVERED);
        } else {
            outbox.setStatus(MdmMessageOutboxStatus.ERROR);
        }

        outbox.setResponseData(jsonUtils.toJson(Map.of("response", body)));
        mdmMessageOutboxRepository.save(outbox);
    }

    private void handleOutboxMessageException(MdmMessageOutbox outbox, Throwable ex, String serviceName) {
        Throwable cause = ex;
        while (cause instanceof CompletionException) {
            if (cause.getCause() == null) break;
            cause = cause.getCause();
        }

        if (cause instanceof TimeoutException) {
            log.warn("Превышено время ожидания от сервиса {} при отправке события id={}, target={}",
                    serviceName, outbox.getMdmMessageId(), outbox.getTarget());

            handleError(outbox, new SendOutboxTimeoutException(serviceName));
        } else if (cause instanceof FeignException feignEx) {
            log.warn("Произошла ошибка при вызове сервиса {}. Статус ответа {}. Сообщение об ошибке {}",
                    serviceName, feignEx.status(), feignEx.contentUTF8());

            handleError(outbox, new BusinessException("Не удалось выполнить вызов сервиса " + serviceName));
        } else {
            log.error("Произошла непредвиденная ошибка при обращении к сервису {}.", serviceName);

            handleFatalError(outbox,
                    new BusinessException("Непредвиденная ошибка при обращении к сервису " + serviceName));
        }
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
