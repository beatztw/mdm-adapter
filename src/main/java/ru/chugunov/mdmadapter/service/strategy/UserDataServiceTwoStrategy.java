package ru.chugunov.mdmadapter.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.chugunov.mdmadapter.dto.requests.UpdateUserDataServiceTwoRequest;
import ru.chugunov.mdmadapter.dto.requests.UserDataServiceTwoEvent;
import ru.chugunov.mdmadapter.dto.responses.*;
import ru.chugunov.mdmadapter.exeption.BusinessException;
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

@Slf4j
@Component
public class UserDataServiceTwoStrategy extends AbstractMdmMessageOutboxStrategy {

    private final UserDataServiceTwoClient userDataServiceTwoClient;

    protected UserDataServiceTwoStrategy(JsonUtils jsonUtils,
                                         MdmProperty mdmProperty,
                                         ExecutorService externalServiceExecutor,
                                         MdmMessageOutboxRepository mdmMessageOutboxRepository,
                                         UserDataServiceTwoClient userDataServiceTwoClient) {
        super(jsonUtils, mdmProperty, externalServiceExecutor, mdmMessageOutboxRepository);
        this.userDataServiceTwoClient = userDataServiceTwoClient;
    }

    @Override
    protected String getServiceName() {
        return "user-data-service-two";
    }

    @Override
    protected CompletableFuture<Void> callService(MdmMessage mdmMessage, String senderName, MdmMessageOutbox outbox) {
        UpdateUserDataServiceTwoRequest request = buildRequest(mdmMessage, senderName);

        return CompletableFuture.supplyAsync(() -> userDataServiceTwoClient.updatePhone(request),
                        externalServiceExecutor)
                .thenAcceptAsync(response -> handleResponse(outbox, response), externalServiceExecutor)
                .exceptionallyAsync(ex -> {
                    handleOutboxMessageException(outbox, ex);

                    throw new BusinessException(ex);
                }, externalServiceExecutor);
    }

    @Override
    protected void handleResponse(MdmMessageOutbox outbox, Object response) {
        UserDataServiceTwoResponse serviceTwoResponse = (UserDataServiceTwoResponse) response;
        UserDataServiceTwoResponseBody body = serviceTwoResponse.getBody();

        if (ServiceResponseStatus.SUCCESS.equals(body.getStatus())) {
            outbox.setStatus(MdmMessageOutboxStatus.DELIVERED);
        } else {
            outbox.setStatus(MdmMessageOutboxStatus.ERROR);
        }

        outbox.setResponseData(jsonUtils.toJson(Map.of("response", body)));
        mdmMessageOutboxRepository.save(outbox);
    }

    @Override
    public MdmMessageOutboxTarget getTarget() {
        return MdmMessageOutboxTarget.USER_DATA_SERVICE_TWO;
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
