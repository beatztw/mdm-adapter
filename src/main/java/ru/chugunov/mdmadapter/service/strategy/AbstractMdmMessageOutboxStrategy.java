package ru.chugunov.mdmadapter.service.strategy;

import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.property.MdmProperty;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


public abstract class AbstractMdmMessageOutboxStrategy<T> implements MdmMessageOutboxStrategy<T> {

    protected final JsonUtils jsonUtils;
    protected final MdmProperty mdmProperty;
    protected final ExecutorService userDataIntegrationServiceExecutor;
    protected final MdmMessageOutboxRepository mdmMessageOutboxRepository;

    protected AbstractMdmMessageOutboxStrategy(
            JsonUtils jsonUtils,
            MdmProperty mdmProperty,
            ExecutorService userDataIntegrationServiceExecutor,
            MdmMessageOutboxRepository mdmMessageOutboxRepository) {
        this.jsonUtils = jsonUtils;
        this.mdmProperty = mdmProperty;
        this.mdmMessageOutboxRepository = mdmMessageOutboxRepository;
        this.userDataIntegrationServiceExecutor = userDataIntegrationServiceExecutor;
    }

    @Override
    public CompletableFuture<T> send(MdmMessage mdmMessage, MdmMessageOutbox outbox) {
        String senderName = mdmProperty.getSystem().getUsername();
        return callService(mdmMessage, senderName, outbox);
    }

    protected abstract CompletableFuture<T> callService(MdmMessage mdmMessage, String senderName, MdmMessageOutbox outbox);
}
