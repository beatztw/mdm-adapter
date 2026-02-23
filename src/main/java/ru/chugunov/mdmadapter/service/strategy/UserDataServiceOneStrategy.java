package ru.chugunov.mdmadapter.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chugunov.mdmadapter.dto.requests.UpdateUserDataServiceOneRequest;
import ru.chugunov.mdmadapter.dto.requests.UserDataServiceOneBody;
import ru.chugunov.mdmadapter.dto.requests.UserDataServiceOneMeta;
import ru.chugunov.mdmadapter.dto.responses.ServiceResponseStatus;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceOneResponse;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceOneResponseBody;
import ru.chugunov.mdmadapter.exeption.BusinessException;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxStatus;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;
import ru.chugunov.mdmadapter.property.MdmProperty;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.service.client.UserDataServiceOneClient;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class UserDataServiceOneStrategy extends AbstractMdmMessageOutboxStrategy {

    private final UserDataServiceOneClient userDataServiceOneClient;

    protected UserDataServiceOneStrategy(JsonUtils jsonUtils,
                                         MdmProperty mdmProperty,
                                         ExecutorService externalServiceExecutor,
                                         MdmMessageOutboxRepository mdmMessageOutboxRepository,
                                         UserDataServiceOneClient userDataServiceOneClient) {
        super(jsonUtils, mdmProperty, externalServiceExecutor, mdmMessageOutboxRepository);
        this.userDataServiceOneClient = userDataServiceOneClient;
    }

    @Override
    protected String getServiceName() {
        return "user-data-service-one";
    }

    @Override
    protected CompletableFuture<Void> callService(MdmMessage mdmMessage, String senderName, MdmMessageOutbox outbox) {
        UpdateUserDataServiceOneRequest request = buildRequest(mdmMessage, senderName);

        return CompletableFuture.supplyAsync(() -> userDataServiceOneClient.updatePhone(request),
                        externalServiceExecutor)
                .thenAcceptAsync(response -> handleResponse(outbox, response), externalServiceExecutor)
                .exceptionallyAsync(ex -> {
                    handleOutboxMessageException(outbox, ex);

                    throw new BusinessException(ex);
                }, externalServiceExecutor);
    }

    @Override
    protected void handleResponse(MdmMessageOutbox outbox, Object response) {
        UserDataServiceOneResponse serviceOneResponse = (UserDataServiceOneResponse) response;
        UserDataServiceOneResponseBody body = serviceOneResponse.getBody();

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
        return MdmMessageOutboxTarget.USER_DATA_SERVICE_ONE;
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
}
