package ru.chugunov.mdmadapter.model;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum MdmMessageType {

    USER_PHONE_CHANGE;

    public static Optional<MdmMessageType> fromString(String type) {
        if (!StringUtils.hasText(type)) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(type.trim()))
                .findFirst();
    }
}
