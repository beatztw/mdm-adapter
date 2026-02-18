package ru.chugunov.mdmadapter.exeption;

public class MdmMessageNotFoundException extends BusinessException {
    public MdmMessageNotFoundException() {
        super("Не удалось найти mdm сообщение");
    }
}
