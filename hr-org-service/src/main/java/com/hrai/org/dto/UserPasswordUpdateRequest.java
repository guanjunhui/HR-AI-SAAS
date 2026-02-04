package com.hrai.org.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户密码更新请求
 */
public class UserPasswordUpdateRequest {

    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
