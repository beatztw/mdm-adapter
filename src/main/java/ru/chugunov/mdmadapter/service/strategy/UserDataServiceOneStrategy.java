package ru.chugunov.mdmadapter.service.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.chugunov.mdmadapter.dto.requests.UpdateUserDataServiceOneRequest;
import ru.chugunov.mdmadapter.dto.requests.UserDataServiceOneBody;
import ru.chugunov.mdmadapter.dto.requests.UserDataServiceOneMeta;
import ru.chugunov.mdmadapter.dto.responses.ServiceResponseStatus;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceOneResponse;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceOneResponseBody;
import ru.chugunov.mdmadapter.exeption.BusinessException;
import ru.chugunov.mdmadapter.exeption.SendOutboxTimeoutException;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxStatus;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;
import ru.chugunov.mdmadapter.property.MdmProperty;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.service.client.UserDataServiceOneClient;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataServiceOneStrategy implements MdmMessageOutboxStrategy {

    private final JsonUtils jsonUtils;
    private final MdmProperty mdmProperty;
    private final ExecutorService externalServiceExecutor;
    private final UserDataServiceOneClient userDataServiceOneClient;
    private final MdmMessageOutboxRepository mdmMessageOutboxRepository;

    @Override
    public MdmMessageOutboxTarget getTarget() {
        return MdmMessageOutboxTarget.USER_DATA_SERVICE_ONE;
    }

    @Override
    public void send(MdmMessage mdmMessage, MdmMessageOutbox outbox) {
        String senderName = mdmProperty.getSystem().getUsername();

        callService(mdmMessage, senderName, outbox);
    }

    private CompletableFuture<Void> callService(MdmMessage mdmMessage, String senderName, MdmMessageOutbox outbox) {
        UpdateUserDataServiceOneRequest request = buildRequest(mdmMessage, senderName);

        return CompletableFuture.supplyAsync(() -> userDataServiceOneClient.updatePhone(request),
                        externalServiceExecutor)
                .orTimeout(mdmProperty.getService().getUserDataOne().getResponseTimeoutSeconds(),
                        TimeUnit.SECONDS)
                .thenAccept(response -> handleResponse(outbox, response))
                .exceptionallyAsync(ex -> {
                    if (ex instanceof TimeoutException) {
                        log.warn("Превышено время ожидания от сервиса при отправке события id={}, target={}",
                                outbox.getMdmMessageId(), outbox.getTarget());

                        handleError(outbox, new SendOutboxTimeoutException("user-data-service-one"));
                    } else {
                        log.error("При отправке события id={}, target={} в сервисы возникла непредвиденная ошибка",
                                outbox.getMdmMessageId(), outbox.getTarget());

                        handleFatalError(outbox,
                                new BusinessException("Ошибка при вызове сервиса user-data-service-one"));
                    }
                    return null;
                }, externalServiceExecutor);
    }

    private void handleResponse(MdmMessageOutbox outbox, UserDataServiceOneResponse response) {
        UserDataServiceOneResponseBody body = response.getBody();

        if (ServiceResponseStatus.SUCCESS.equals(body.getStatus())) {
            outbox.setStatus(MdmMessageOutboxStatus.DELIVERED);
        } else {
            outbox.setStatus(MdmMessageOutboxStatus.ERROR);
        }

        outbox.setResponseData(jsonUtils.toJson(Map.of("response", body)));
        mdmMessageOutboxRepository.save(outbox);
    }

    private UpdateUserDataServiceOneRequest buildRequest(MdmMessage mdmMessage, String senderName) {
        return UpdateUserDataServiceOneRequest.builder()
                .meta(UserDataServiceOneMeta.builder()
                        .sender(senderName)
                        .systemId("mdm-" + senderName)
                        .build())
                .body(UserDataServiceOneBody.builder()
                        .id(mdmMessage.getExternalId())
                        .guid(mdmMessage.getGuid())
                        .phone(mdmMessage.getPayload().getPhone())
                        .build())
                .build();
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
