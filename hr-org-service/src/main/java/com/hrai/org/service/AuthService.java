package com.hrai.org.service;

import com.hrai.org.dto.LoginRequest;
import com.hrai.org.dto.LoginResponse;
import com.hrai.org.dto.RefreshTokenRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param ip      客户端 IP
     * @param userAgent User-Agent
     * @return 登录响应 (Token + 用户信息)
     */
    LoginResponse login(LoginRequest request, String ip, String userAgent);

    /**
     * 刷新 Token
     *
     * @param request 刷新请求
     * @return 新的 Token
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * 用户登出
     *
     * @param accessToken Access Token
     */
    void logout(String accessToken);

    /**
     * 获取当前用户信息
     *
     * @param userId   用户 ID
     * @param tenantId 租户 ID
     * @return 用户信息
     */
    LoginResponse.UserInfo getCurrentUser(Long userId, String tenantId);
}
