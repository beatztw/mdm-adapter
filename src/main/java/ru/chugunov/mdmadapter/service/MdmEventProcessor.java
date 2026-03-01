package ru.chugunov.mdmadapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chugunov.mdmadapter.dto.UpdatePhoneMdmEvent;
import ru.chugunov.mdmadapter.model.*;
import ru.chugunov.mdmadapter.repository.MdmMessageOutboxRepository;
import ru.chugunov.mdmadapter.repository.MdmMessageRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class MdmEventProcessor {

    private final MdmOutboxSender mdmOutboxSender;
    private final ExecutorService processOutboxEventExecutor;
    private final MdmMessageRepository mdmMessageRepository;
    private final MdmMessageOutboxRepository mdmMessageOutboxRepository;

    @Transactional
    public void processEvent(UpdatePhoneMdmEvent event) {
        UUID eventExternalId = UUID.fromString(event.getId());

        MdmMessage mdmMessage = saveMdmMessage(event, eventExternalId);

        List<MdmMessageOutbox> mdmMessageOutboxes = saveOutboxRecords(mdmMessage);

        CompletableFuture.runAsync(() -> {
                    List<CompletableFuture<Void>> completableFutures = mdmMessageOutboxes.stream()
                            .map(mdmOutboxSender::sendOutbox)
                            .toList();

                    completableFutures.forEach(CompletableFuture::join);
                },
                processOutboxEventExecutor);
    }

    private List<MdmMessageOutbox> saveOutboxRecords(MdmMessage mdmMessage) {
        List<MdmMessageOutboxTarget> mdmTargets = Arrays.asList(MdmMessageOutboxTarget.values());

        List<MdmMessageOutbox> mdmOutboxesRecords = mdmTargets.stream()
                .map(target -> buildOutboxRecord(mdmMessage, target))
                .toList();

        return mdmMessageOutboxRepository.saveAll(mdmOutboxesRecords);
    }

    private MdmMessage saveMdmMessage(UpdatePhoneMdmEvent event, UUID eventExternalId) {
        MdmMessage mdmMessage = MdmMessage.builder()
                .externalId(eventExternalId)
                .guid(event.getGuid())
                .type(event.getType())
                .payload(MdmMessagePayload.builder()
                        .phone(event.getPhone())
                        .build())
                .build();

        return mdmMessageRepository.save(mdmMessage);
    }

    private MdmMessageOutbox buildOutboxRecord(MdmMessage mdmMessage, MdmMessageOutboxTarget target) {
        return MdmMessageOutbox.builder()
                .mdmMessageId(mdmMessage.getId())
                .target(target)
                .status(MdmMessageOutboxStatus.NEW)
                .responseData(null)
                .build();
    }
}
