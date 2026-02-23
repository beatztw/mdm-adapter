package ru.chugunov.mdmadapter.service.strategy;

import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;

import java.util.concurrent.CompletableFuture;

public interface MdmMessageOutboxStrategy {

    CompletableFuture<Void> send(MdmMessage mdmMessage, MdmMessageOutbox outbox);

    MdmMessageOutboxTarget getTarget();

}
