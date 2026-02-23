package ru.chugunov.mdmadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.chugunov.mdmadapter.property.MdmExecutorsProperty;
import ru.chugunov.mdmadapter.property.MdmProperty;
import ru.chugunov.mdmadapter.property.MdmResendMessageProperty;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication
@EnableConfigurationProperties(value = {
        MdmProperty.class,
        MdmExecutorsProperty.class,
        MdmResendMessageProperty.class
})
public class MdmAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdmAdapterApplication.class, args);
    }

}
