package ru.chugunov.mdmadapter.service.strategy;

import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;

public interface MdmMessageOutboxStrategy {

    void send(MdmMessage mdmMessage, MdmMessageOutbox outbox);

    MdmMessageOutboxTarget getTarget();

}
