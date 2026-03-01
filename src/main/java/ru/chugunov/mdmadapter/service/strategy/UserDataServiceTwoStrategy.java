package ru.chugunov.mdmadapter.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.chugunov.mdmadapter.dto.common.CommonServiceResponseBody;
import ru.chugunov.mdmadapter.dto.requests.UpdateUserDataServiceTwoRequest;
import ru.chugunov.mdmadapter.dto.requests.UserDataServiceTwoEvent;
import ru.chugunov.mdmadapter.dto.responses.UserDataServiceTwoResponse;
import ru.chugunov.mdmadapter.mapper.ServiceResponseMapper;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;
import ru.chugunov.mdmadapter.property.MdmProperty;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.service.client.UserDataServiceTwoClient;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class UserDataServiceTwoStrategy extends AbstractClientServiceStrategy {

    private final UserDataServiceTwoClient userDataServiceTwoClient;

    protected UserDataServiceTwoStrategy(JsonUtils jsonUtils,
                                         MdmProperty mdmProperty,
                                         ServiceResponseMapper serviceResponseMapper,
                                         ExecutorService userDataIntegrationServiceExecutor,
                                         MdmMessageOutboxRepository mdmMessageOutboxRepository,
                                         UserDataServiceTwoClient userDataServiceTwoClient) {
        super(jsonUtils, mdmProperty, serviceResponseMapper, userDataIntegrationServiceExecutor, mdmMessageOutboxRepository);
        this.userDataServiceTwoClient = userDataServiceTwoClient;
    }

    @Override
    public String getServiceName() {
        return "user-data-service-two";
    }

    @Override
    protected CompletableFuture<CommonServiceResponseBody> callService(MdmMessage mdmMessage,
                                                                        String senderName,
                                                                        MdmMessageOutbox outbox) {
        UpdateUserDataServiceTwoRequest request = buildRequest(mdmMessage, senderName);

        return CompletableFuture.supplyAsync(() -> {
                    UserDataServiceTwoResponse response = userDataServiceTwoClient.updatePhone(request);

                    return serviceResponseMapper.toCommonResponseBody(response.getBody());
                },
                userDataIntegrationServiceExecutor);
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
