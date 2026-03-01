package ru.chugunov.mdmadapter.service.strategy;

import ru.chugunov.mdmadapter.dto.common.CommonServiceResponseBody;
import ru.chugunov.mdmadapter.mapper.ServiceResponseMapper;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.property.MdmProperty;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


public abstract class AbstractClientServiceStrategy implements ClientServiceStrategy {

    protected final JsonUtils jsonUtils;
    protected final MdmProperty mdmProperty;
    protected final ServiceResponseMapper serviceResponseMapper;
    protected final ExecutorService userDataIntegrationServiceExecutor;
    protected final MdmMessageOutboxRepository mdmMessageOutboxRepository;

    protected AbstractClientServiceStrategy(
            JsonUtils jsonUtils,
            MdmProperty mdmProperty,
            ServiceResponseMapper serviceResponseMapper,
            ExecutorService userDataIntegrationServiceExecutor,
            MdmMessageOutboxRepository mdmMessageOutboxRepository) {
        this.jsonUtils = jsonUtils;
        this.mdmProperty = mdmProperty;
        this.serviceResponseMapper = serviceResponseMapper;
        this.mdmMessageOutboxRepository = mdmMessageOutboxRepository;
        this.userDataIntegrationServiceExecutor = userDataIntegrationServiceExecutor;
    }

    @Override
    public CompletableFuture<CommonServiceResponseBody> send(MdmMessage mdmMessage, MdmMessageOutbox outbox) {
        String senderName = mdmProperty.getSystem().getUsername();
        return callService(mdmMessage, senderName, outbox);
    }

    protected abstract CompletableFuture<CommonServiceResponseBody> callService(MdmMessage mdmMessage,
                                                                                String senderName,
                                                                                MdmMessageOutbox outbox);
}
