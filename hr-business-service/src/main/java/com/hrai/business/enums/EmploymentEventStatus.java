package com.hrai.business.enums;

/**
 * 任职事件状态枚举
 */
public enum EmploymentEventStatus {
    DRAFT("draft", "草稿"),
    PENDING("pending", "待审批"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已驳回"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String desc;

    EmploymentEventStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EmploymentEventStatus fromCode(String code) {
        for (EmploymentEventStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
