package ru.chugunov.mdmadapter.service.strategy;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import ru.chugunov.mdmadapter.exeption.BusinessException;
import ru.chugunov.mdmadapter.exeption.SendOutboxTimeoutException;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxStatus;
import ru.chugunov.mdmadapter.property.MdmProperty;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

@Slf4j
public abstract class AbstractMdmMessageOutboxStrategy implements MdmMessageOutboxStrategy {

    protected final JsonUtils jsonUtils;
    protected final MdmProperty mdmProperty;
    protected final ExecutorService externalServiceExecutor;
    protected final MdmMessageOutboxRepository mdmMessageOutboxRepository;

    protected AbstractMdmMessageOutboxStrategy(
            JsonUtils jsonUtils,
            MdmProperty mdmProperty,
            ExecutorService externalServiceExecutor,
            MdmMessageOutboxRepository mdmMessageOutboxRepository) {
        this.jsonUtils = jsonUtils;
        this.mdmProperty = mdmProperty;
        this.externalServiceExecutor = externalServiceExecutor;
        this.mdmMessageOutboxRepository = mdmMessageOutboxRepository;
    }

    @Override
    public CompletableFuture<Void> send(MdmMessage mdmMessage, MdmMessageOutbox outbox) {
        String senderName = mdmProperty.getSystem().getUsername();
        return callService(mdmMessage, senderName, outbox);
    }

    protected abstract String getServiceName();

    protected abstract void handleResponse(MdmMessageOutbox outbox, Object response);

    protected abstract CompletableFuture<Void> callService(MdmMessage mdmMessage, String senderName, MdmMessageOutbox outbox);

    protected void handleOutboxMessageException(MdmMessageOutbox outbox, Throwable ex) {
        String serviceName = getServiceName();

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

    protected void handleFatalError(MdmMessageOutbox outbox, Exception e) {
        outbox.setStatus(MdmMessageOutboxStatus.FATAL_ERROR);
        outbox.setResponseData(jsonUtils.toJson(Map.of("errors", List.of(e))));

        mdmMessageOutboxRepository.save(outbox);
    }

    protected void handleError(MdmMessageOutbox outbox, Exception e) {
        outbox.setStatus(MdmMessageOutboxStatus.ERROR);
        outbox.setResponseData(jsonUtils.toJson(Map.of("response", List.of(e))));

        mdmMessageOutboxRepository.save(outbox);
    }
}
