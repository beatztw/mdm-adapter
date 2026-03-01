package ru.chugunov.mdmadapter.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chugunov.mdmadapter.dto.common.CommonServiceResponseBody;
import ru.chugunov.mdmadapter.dto.responses.ServiceResponseStatus;
import ru.chugunov.mdmadapter.exeption.BusinessException;
import ru.chugunov.mdmadapter.exeption.MdmMessageNotFoundException;
import ru.chugunov.mdmadapter.exeption.SendOutboxTimeoutException;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxStatus;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.repository.MdmMessageRepository;
import ru.chugunov.mdmadapter.service.strategy.ClientServiceStrategy;
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
    private final ExecutorService userDataIntegrationServiceExecutor;
    private final MdmMessageOutboxRepository mdmMessageOutboxRepository;
    private final Map<MdmMessageOutboxTarget, ClientServiceStrategy> clientServiceByTarget;

    public CompletableFuture<Void> sendOutbox(MdmMessageOutbox outbox) {
        MdmMessageOutboxTarget target = outbox.getTarget();
        ClientServiceStrategy clientServiceStrategy = clientServiceByTarget.get(target);

        if (clientServiceStrategy == null) {
            log.warn("Не удалось определить сервис для отправки outbox по направлению: {}", target);

            return CompletableFuture.completedFuture(null);
        }

        MdmMessage mdmMessage = mdmMessageRepository.findById(outbox.getMdmMessageId())
                .orElseThrow(MdmMessageNotFoundException::new);

        return clientServiceStrategy.send(mdmMessage, outbox)
                .thenAcceptAsync(sendingResult -> handleSendingResult(outbox, sendingResult),
                        userDataIntegrationServiceExecutor)
                .handleAsync((result, ex) -> {
                    if (ex != null) {
                        String serviceName = clientServiceStrategy.getServiceName();
                        handleOutboxMessageException(outbox, ex, serviceName);

                        throw new BusinessException(ex);
                    }

                    return null;
                }, userDataIntegrationServiceExecutor);
    }

    private void handleSendingResult(MdmMessageOutbox outbox, CommonServiceResponseBody body) {

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
            log.warn("Превышено время ожидания от сервиса {} при отправке mdm события id={} по нарпавлению {}",
                    serviceName, outbox.getMdmMessageId(), outbox.getTarget());

            handleError(outbox, new SendOutboxTimeoutException(serviceName));
        } else if (cause instanceof FeignException feignEx) {
            log.warn("Произошла ошибка при вызове сервиса {}. Статус ответа {}. Сообщение об ошибке {}",
                    serviceName, feignEx.status(), feignEx.contentUTF8());

            handleError(outbox, new BusinessException("Не удалось выполнить вызов сервиса " + serviceName));
        } else {
            log.error("Непредвиденная ошибка при отправке события mdm в сервис {}.", serviceName);

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
