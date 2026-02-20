package ru.chugunov.mdmadapter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mdm.executors")
public class MdmExecutorsProperty {

    private OutboxElastic outboxElastic = new OutboxElastic();
    private ResendMdmMessageOutbox resendMdmMessageOutbox = new ResendMdmMessageOutbox();
    private ExternalService externalService = new ExternalService();

    @Getter
    @Setter
    public static class OutboxElastic {
        private Integer threads;
        private Integer queueCapacity;
    }

    @Getter
    @Setter
    public static class ResendMdmMessageOutbox {
        private Integer threads;
        private Integer queueCapacity;
    }

    @Getter
    @Setter
    public static class ExternalService {
        private Integer threads;
        private Integer queueCapacity;
    }
}
