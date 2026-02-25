package ru.chugunov.mdmadapter.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.chugunov.mdmadapter.dto.requests.UpdateUserDataServiceOneRequest;
import ru.chugunov.mdmadapter.dto.requests.UserDataServiceOneBody;
import ru.chugunov.mdmadapter.dto.requests.UserDataServiceOneMeta;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceOneResponse;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;
import ru.chugunov.mdmadapter.property.MdmProperty;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.service.client.UserDataServiceOneClient;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class UserDataServiceOneStrategy extends AbstractMdmMessageOutboxStrategy<UserDataServiceOneResponse> {

    private final UserDataServiceOneClient userDataServiceOneClient;

    protected UserDataServiceOneStrategy(JsonUtils jsonUtils,
                                         MdmProperty mdmProperty,
                                         ExecutorService userDataIntegrationServiceExecutor,
                                         MdmMessageOutboxRepository mdmMessageOutboxRepository,
                                         UserDataServiceOneClient userDataServiceOneClient) {
        super(jsonUtils, mdmProperty, userDataIntegrationServiceExecutor, mdmMessageOutboxRepository);
        this.userDataServiceOneClient = userDataServiceOneClient;
    }

    @Override
    public String getServiceName() {
        return "user-data-service-one";
    }

    @Override
    protected CompletableFuture<UserDataServiceOneResponse> callService(MdmMessage mdmMessage, String senderName, MdmMessageOutbox outbox) {
        UpdateUserDataServiceOneRequest request = buildRequest(mdmMessage, senderName);

        return CompletableFuture.supplyAsync(() -> userDataServiceOneClient.updatePhone(request),
                userDataIntegrationServiceExecutor);
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
