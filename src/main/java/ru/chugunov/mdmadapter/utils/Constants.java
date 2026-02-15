package ru.chugunov.mdmadapter.utils;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    public static final String DEFAULT_DB_USER = "mdm";

    /*
     * Константы для валидации
     */
    public static final String GUID_PATTERN = "^[0-9A-F]{32}$";
    public static final String RUS_PHONE_PATTERN = "^\\+7\\d{10}$";
    public static final String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

}
