package ru.chugunov.mdmadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableFeignClients
@SpringBootApplication
public class MdmAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdmAdapterApplication.class, args);
    }

}
