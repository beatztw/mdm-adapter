package ru.chugunov.mdmadapter.service.strategy;

import ru.chugunov.mdmadapter.dto.common.CommonServiceResponseBody;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;

import java.util.concurrent.CompletableFuture;

public interface ClientServiceStrategy {

    String getServiceName();

    MdmMessageOutboxTarget getTarget();

    CompletableFuture<CommonServiceResponseBody> send(MdmMessage mdmMessage, MdmMessageOutbox outbox);
}
