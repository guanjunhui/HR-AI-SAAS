package com.hrai.org.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求 DTO
 */
public class LoginRequest {

    private String tenantId = "tenant_default";

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String captcha;
    private String captchaKey;
    private Boolean rememberMe = false;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCaptcha() { return captcha; }
    public void setCaptcha(String captcha) { this.captcha = captcha; }

    public String getCaptchaKey() { return captchaKey; }
    public void setCaptchaKey(String captchaKey) { this.captchaKey = captchaKey; }

    public Boolean getRememberMe() { return rememberMe; }
    public void setRememberMe(Boolean rememberMe) { this.rememberMe = rememberMe; }
}
