package ru.chugunov.mdmadapter.service.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.chugunov.mdmadapter.dto.requests.UpdateUserDataServiceTwoRequest;
import ru.chugunov.mdmadapter.dto.requests.UserDataServiceTwoEvent;
import ru.chugunov.mdmadapter.dto.responses.ServiceResponseStatus;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceTwoResponse;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceTwoResponseBody;
import ru.chugunov.mdmadapter.exeption.BusinessException;
import ru.chugunov.mdmadapter.exeption.SendOutboxTimeoutException;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxStatus;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;
import ru.chugunov.mdmadapter.property.MdmProperty;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.service.client.UserDataServiceTwoClient;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataServiceTwoStrategy implements MdmMessageOutboxStrategy {

    private final JsonUtils jsonUtils;
    private final MdmProperty mdmProperty;
    private final ExecutorService externalServiceExecutor;
    private final UserDataServiceTwoClient userDataServiceTwoClient;
    private final MdmMessageOutboxRepository mdmMessageOutboxRepository;

    @Override
    public MdmMessageOutboxTarget getTarget() {
        return MdmMessageOutboxTarget.USER_DATA_SERVICE_TWO;
    }

    @Override
    public void send(MdmMessage mdmMessage, MdmMessageOutbox outbox) {
        String senderName = mdmProperty.getSystem().getUsername();

        callService(mdmMessage, senderName, outbox);
    }

    private CompletableFuture<Void> callService(MdmMessage mdmMessage,
                                                String senderName,
                                                MdmMessageOutbox outbox) throws BusinessException {
        UpdateUserDataServiceTwoRequest request = buildRequest(mdmMessage, senderName);

            return CompletableFuture.supplyAsync(() -> userDataServiceTwoClient.updatePhone(request),
                            externalServiceExecutor)
                    .thenAccept(response -> handleResponse(outbox, response))
                    .exceptionallyAsync(ex -> {
                        if (ex instanceof TimeoutException) {
                            log.error("Превышено время ожидания от сервиса при отправке события id={}, target={}",
                                    outbox.getMdmMessageId(), outbox.getTarget());

                            throw new SendOutboxTimeoutException("user-data-service-one");
                        } else {
                            log.error("При отправке события id={}, target={} в сервисы возникла непредвиденная ошибка",
                                    outbox.getMdmMessageId(), outbox.getTarget());

                            throw new BusinessException("Ошибка при вызове сервиса user-data-service-two");
                        }
                    }, externalServiceExecutor);

    }

    private void handleResponse(MdmMessageOutbox outbox, UserDataServiceTwoResponse response) {
        UserDataServiceTwoResponseBody body = response.getBody();

        if (ServiceResponseStatus.SUCCESS.equals(body.getStatus())) {
            outbox.setStatus(MdmMessageOutboxStatus.DELIVERED);
        } else {
            outbox.setStatus(MdmMessageOutboxStatus.ERROR);
        }

        outbox.setResponseData(jsonUtils.toJson(Map.of("response", body)));
        mdmMessageOutboxRepository.save(outbox);
    }

    private UpdateUserDataServiceTwoRequest buildRequest(MdmMessage mdmMessage, String senderName) {
        return UpdateUserDataServiceTwoRequest.builder()
                .id(mdmMessage.getId())
                .systemId("mdm-" + senderName)
                .events(List.of(UserDataServiceTwoEvent.builder()
                        .eventType("change_phone")
                        .guid(mdmMessage.getGuid())
                        .phone(mdmMessage.getPayload().getPhone())
                        .build()))
                .build();
    }
}
