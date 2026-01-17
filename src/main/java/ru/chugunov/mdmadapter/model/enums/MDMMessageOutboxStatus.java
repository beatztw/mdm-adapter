package ru.chugunov.mdmadapter.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MDMMessageOutboxStatus {

    @JsonProperty("new")
    NEW,
    @JsonProperty("delivered")
    DELIVERED,
    @JsonProperty("error")
    ERROR,
    @JsonProperty("fatal-error")
    FATAL_ERROR
}
