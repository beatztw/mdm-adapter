package ru.chugunov.mdmadapter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mdm")
public class MdmProperty {

    private System system = new System();

    @Getter
    @Setter
    public static class System {
        private String username;
    }
}
