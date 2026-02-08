package com.hrai.org.controller;

import cn.hutool.core.util.StrUtil;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.dto.Result;
import com.hrai.common.exception.BizException;
import com.hrai.org.dto.LoginRequest;
import com.hrai.org.dto.LoginResponse;
import com.hrai.org.dto.RefreshTokenRequest;
import com.hrai.org.service.AuthService;
import com.hrai.org.util.JwtUtils;
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
    private final JwtUtils jwtUtils;

    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
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
            @RequestHeader(value = TenantConstants.USER_ID_HEADER, required = false) Long userId,
            @RequestHeader(value = TenantConstants.TENANT_ID_HEADER, required = false) String tenantId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long resolvedUserId = userId;
        String resolvedTenantId = tenantId;

        if ((resolvedUserId == null || StrUtil.isBlank(resolvedTenantId))
                && StrUtil.isNotBlank(authorization)
                && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            if (jwtUtils.validateToken(token)) {
                if (resolvedUserId == null) {
                    resolvedUserId = jwtUtils.getUserId(token);
                }
                if (StrUtil.isBlank(resolvedTenantId)) {
                    resolvedTenantId = jwtUtils.getTenantId(token);
                }
            }
        }

        if (resolvedUserId == null || StrUtil.isBlank(resolvedTenantId)) {
            throw new BizException(401, "未登录或登录态已失效");
        }

        LoginResponse.UserInfo userInfo = authService.getCurrentUser(resolvedUserId, resolvedTenantId);
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
