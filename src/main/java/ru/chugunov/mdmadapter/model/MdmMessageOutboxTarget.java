package ru.chugunov.mdmadapter.model;

import java.util.EnumSet;
import java.util.Set;

public enum MdmMessageOutboxTarget {

    USER_DATA_SERVICE_ONE,
    USER_DATA_SERVICE_TWO;

    public static Set<MdmMessageOutboxTarget> getAllTargets() {
        return EnumSet.allOf(MdmMessageOutboxTarget.class);
    }
}
