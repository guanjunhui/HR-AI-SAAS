package com.hrai.business.enums;

/**
 * 任职事件类型枚举
 */
public enum EmploymentEventType {
    ENTRY("entry", "入职"),
    REGULAR("regular", "转正"),
    TRANSFER("transfer", "调岗"),
    PROMOTION("promotion", "晋升"),
    DEMOTION("demotion", "降级"),
    RESIGNATION("resignation", "离职");

    private final String code;
    private final String desc;

    EmploymentEventType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EmploymentEventType fromCode(String code) {
        for (EmploymentEventType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
