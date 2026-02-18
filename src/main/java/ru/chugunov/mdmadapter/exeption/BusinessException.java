package ru.chugunov.mdmadapter.exeption;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, String location) {
        super(String.format(message, location));
    }
}
