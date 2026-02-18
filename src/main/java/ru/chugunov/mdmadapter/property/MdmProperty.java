package ru.chugunov.mdmadapter.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mdm")
public class MdmProperty {

    private System system = new System();
    private Service service = new Service();

    @Getter
    @Setter
    public static class System {
        private String username;
    }

    @Getter
    @Setter
    public static class Service {
        private UserDataOne userDataOne = new UserDataOne();
        private UserDataTwo userDataTwo = new UserDataTwo();

        @Getter
        @Setter
        public static class UserDataOne {
            private int responseTimeoutSeconds;
        }

        @Getter
        @Setter
        public static class UserDataTwo {
            private int responseTimeoutSeconds;
        }
    }
}
