package ru.chugunov.mdmadapter.kafka.listener;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.security.oauthbearer.internals.secured.ValidateException;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.chugunov.mdmadapter.dto.UpdatePhoneMdmEvent;
import ru.chugunov.mdmadapter.exeption.BusinessException;
import ru.chugunov.mdmadapter.service.MdmEventProcessor;
import ru.chugunov.mdmadapter.utils.JsonUtils;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mdm.kafka.mdm-event", name = "enabled", havingValue = "true")
public class MdmEventListener {

    private final JsonUtils jsonUtils;
    private final Validator validator;
    private final MdmEventProcessor mdmEventProcessor;

    @KafkaListener(topics = "${mdm.kafka.mdm-event.change-phone.topic-in}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUpdatePhoneEvent(ConsumerRecord<String, String> consumerRecord) {
        processMessage(consumerRecord, mdmEventProcessor::process, UpdatePhoneMdmEvent.class);
    }

    public <T> void processMessage(ConsumerRecord<String, String> consumerRecord,
                                   Consumer<T> processor,
                                   Class<T> requestClass) {
        enrichMdcContext(consumerRecord);

        if (!StringUtils.hasText(consumerRecord.value())) {
            log.info("Получено пустое сообщение от сервера");
        } else {
            log.info("Получено событие из kafka {}", consumerRecord);
        }

        try {
            T mdmEvent = jsonUtils.fromJson(consumerRecord.value(), requestClass);

            validateMessage(mdmEvent);

            processor.accept(mdmEvent);

        } catch (ValidateException e) {
            log.warn("Ошибка валидации сообщения из kafka: {}", e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("Бизнес исключение при обработке сообщения из kafka: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Непредвиденное исключение при обработке сообщения из kafka: {}", e.getMessage(), e);
        } finally {
            MDC.clear();
        }

    }

    private <T> void validateMessage(T event) {
        Set<ConstraintViolation<T>> validationErrors = validator.validate(event);

        if (CollectionUtils.isEmpty(validationErrors)) {
            return;
        }

        String validationErrorMessage = validationErrors.stream()
                .map(ve -> ve.getMessage() + " \"" + ve.getPropertyPath() + "\":\"" + ve.getInvalidValue() + "\"")
                .collect(Collectors.joining(", "));

        throw new ValidateException("[" + validationErrorMessage + "]");
    }

    private static void enrichMdcContext(ConsumerRecord<String, String> consumerRecord) {
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("messageKey", consumerRecord.key());
        MDC.put("topic", consumerRecord.topic());
        MDC.put("partition", String.valueOf(consumerRecord.partition()));
        MDC.put("offset", String.valueOf(consumerRecord.offset()));
    }

}
