package com.hrai.org.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 更新角色权限请求
 */
public class RolePermissionsUpdateRequest {

    @NotNull(message = "权限列表不能为空")
    private List<String> permissions;

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
