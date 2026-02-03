package com.hrai.org.controller;

import cn.hutool.core.util.StrUtil;
import com.hrai.common.dto.Result;
import com.hrai.org.dto.LoginRequest;
import com.hrai.org.dto.LoginResponse;
import com.hrai.org.dto.RefreshTokenRequest;
import com.hrai.org.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * 处理用户登录、登出、Token 刷新等认证相关请求
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request,
                                        HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        log.info("用户登录请求: username={}, tenantId={}, ip={}",
                request.getUsername(), request.getTenantId(), ip);

        LoginResponse response = authService.login(request, ip, userAgent);
        return Result.success(response);
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (StrUtil.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token);
        }
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<LoginResponse.UserInfo> getCurrentUser(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        LoginResponse.UserInfo userInfo = authService.getCurrentUser(userId, tenantId);
        return Result.success(userInfo);
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多个代理时，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
