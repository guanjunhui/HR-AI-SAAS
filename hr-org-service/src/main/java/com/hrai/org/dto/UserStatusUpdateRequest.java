package com.hrai.org.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 用户状态更新请求
 */
public class UserStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    private Integer status;

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
