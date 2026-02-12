package ru.chugunov.mdmadapter.exeption;

public class JsonConversionException extends BusinessException {
    public JsonConversionException() {
        super("Ошибка преобразования объекта в JSON");
    }
}
