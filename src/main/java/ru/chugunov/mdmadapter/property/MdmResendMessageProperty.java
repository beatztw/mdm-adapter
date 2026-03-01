package ru.chugunov.mdmadapter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mdm.scheduler.resend-mdm-message-outbox")
public class MdmResendMessageProperty {

    private long updateTimeToMinutes;
    private long updateTimeFromMinutes;
    private int pageSize;

}
