package ru.chugunov.mdmadapter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mdm.executors")
public class MdmExecutorsProperty {

    private ProcessOutboxEvent processOutboxEvent = new ProcessOutboxEvent();
    private ScheduledResendMdmMessage scheduledResendMdmMessage = new ScheduledResendMdmMessage();
    private UserDataIntegrationService userDataIntegrationService = new UserDataIntegrationService();

    @Getter
    @Setter
    public static class ProcessOutboxEvent {
        private Integer threads;
        private Integer queueCapacity;
    }

    @Getter
    @Setter
    public static class ScheduledResendMdmMessage {
        private Integer threads;
        private Integer queueCapacity;
    }

    @Getter
    @Setter
    public static class UserDataIntegrationService {
        private Integer threads;
        private Integer queueCapacity;
    }
}
