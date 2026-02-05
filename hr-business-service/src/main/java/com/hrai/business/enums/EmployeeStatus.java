package com.hrai.business.enums;

/**
 * 员工状态枚举
 */
public enum EmployeeStatus {
    TRIAL("trial", "试用期"),
    REGULAR("regular", "正式"),
    RESIGNED("resigned", "离职");

    private final String code;
    private final String desc;

    EmployeeStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EmployeeStatus fromCode(String code) {
        for (EmployeeStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
