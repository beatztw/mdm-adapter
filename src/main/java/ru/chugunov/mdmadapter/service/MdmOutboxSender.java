package ru.chugunov.mdmadapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chugunov.mdmadapter.exeption.MdmMessageNotFoundException;
import ru.chugunov.mdmadapter.model.MdmMessage;
import ru.chugunov.mdmadapter.model.MdmMessageOutbox;
import ru.chugunov.mdmadapter.model.MdmMessageOutboxTarget;
import ru.chugunov.mdmadapter.repository.MdmMessageRepository;
import ru.chugunov.mdmadapter.service.strategy.MdmMessageOutboxStrategy;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MdmOutboxSender {

    private final MdmMessageRepository mdmMessageRepository;
    private final Map<MdmMessageOutboxTarget, MdmMessageOutboxStrategy> mdmMessageOutboxMap;

    public CompletableFuture<Void> sendOutbox(MdmMessageOutbox outbox) {
        MdmMessage mdmMessage = mdmMessageRepository.findById(outbox.getMdmMessageId())
                .orElseThrow(MdmMessageNotFoundException::new);

        MdmMessageOutboxStrategy messageOutboxStrategy = mdmMessageOutboxMap.get(outbox.getTarget());

        return messageOutboxStrategy.send(mdmMessage, outbox);

    }
}
