package ru.chugunov.mdmadapter.exeption;

public class MdmEventException extends BusinessException {

    public MdmEventException() {
        super("Тип mdm события не определен");
    }
}
