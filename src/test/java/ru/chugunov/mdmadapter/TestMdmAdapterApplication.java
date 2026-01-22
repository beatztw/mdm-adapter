package ru.chugunov.mdmadapter;

import org.springframework.boot.SpringApplication;

public class TestMdmAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.from(MdmAdapterApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
