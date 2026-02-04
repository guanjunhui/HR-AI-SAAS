package com.hrai.org.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 用户角色更新请求
 */
public class UserRoleUpdateRequest {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
