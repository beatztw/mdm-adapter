package ru.chugunov.mdmadapter.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MDMMessageOutboxTarget {

    @JsonProperty("user-data-service-one")
    USER_DATA_SERVICE_ONE,
    @JsonProperty("user-data-service-two")
    USER_DATA_SERVICE_TWO
}
