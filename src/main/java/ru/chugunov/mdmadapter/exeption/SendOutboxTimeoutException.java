package ru.chugunov.mdmadapter.exeption;

public class SendOutboxTimeoutException extends BusinessException {

    public SendOutboxTimeoutException(String serviceName) {
        super("Таймаут ожидания ответа от клиента %s", serviceName);
    }
}
