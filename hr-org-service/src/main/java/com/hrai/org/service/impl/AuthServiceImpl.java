package com.hrai.org.service.impl;

import cn.hutool.core.util.StrUtil;
import com.hrai.common.exception.BizException;
import com.hrai.org.dto.LoginRequest;
import com.hrai.org.dto.LoginResponse;
import com.hrai.org.dto.RefreshTokenRequest;
import com.hrai.org.entity.AuditLog;
import com.hrai.org.entity.SysRole;
import com.hrai.org.entity.SysUser;
import com.hrai.org.mapper.AuditLogMapper;
import com.hrai.org.mapper.SysRoleMapper;
import com.hrai.org.mapper.SysUserMapper;
import com.hrai.org.service.AuthService;
import com.hrai.org.util.JwtUtils;
import com.hrai.org.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final AuditLogMapper auditLogMapper;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(SysUserMapper userMapper, SysRoleMapper roleMapper,
                           AuditLogMapper auditLogMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.auditLogMapper = auditLogMapper;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        String tenantId = StrUtil.blankToDefault(request.getTenantId(), "tenant_default");

        // 查询用户
        SysUser user = userMapper.selectByUsername(request.getUsername(), tenantId);
        if (user == null) {
            log.warn("登录失败: 用户不存在, username={}, tenantId={}", request.getUsername(), tenantId);
            recordLoginLog(null, request.getUsername(), tenantId, "FAILURE", "用户不存在", ip, userAgent);
            throw new BizException(401, "用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            log.warn("登录失败: 用户已禁用, userId={}", user.getId());
            recordLoginLog(user.getId(), request.getUsername(), tenantId, "FAILURE", "用户已禁用", ip, userAgent);
            throw new BizException(401, "用户已被禁用");
        }

        // 验证密码
        if (!PasswordUtils.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败: 密码错误, userId={}", user.getId());
            recordLoginLog(user.getId(), request.getUsername(), tenantId, "FAILURE", "密码错误", ip, userAgent);
            throw new BizException(401, "用户名或密码错误");
        }

        // 查询角色
        SysRole role = null;
        String roleCode = "user";
        String roleName = "普通用户";
        if (user.getRoleId() != null) {
            role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                roleCode = role.getCode();
                roleName = role.getName();
            }
        }

        // 生成 Token
        String accessToken = jwtUtils.generateAccessToken(user, roleCode);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        // 记录登录日志
        recordLoginLog(user.getId(), request.getUsername(), tenantId, "SUCCESS", null, ip, userAgent);

        log.info("用户登录成功: userId={}, username={}, tenantId={}", user.getId(), user.getUsername(), tenantId);

        // 构建响应
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .tenantId(user.getTenantId())
                .roleCode(roleCode)
                .roleName(roleName)
                .planType(user.getPlanType())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiresInSeconds())
                .userInfo(userInfo)
                .build();
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 验证 Refresh Token
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BizException(401, "Refresh Token 无效或已过期");
        }

        if (!jwtUtils.isRefreshToken(refreshToken)) {
            throw new BizException(401, "无效的 Refresh Token");
        }

        // 获取用户信息
        Long userId = jwtUtils.getUserId(refreshToken);
        String tenantId = jwtUtils.getTenantId(refreshToken);

        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BizException(401, "用户不存在");
        }

        if (user.getStatus() != 1) {
            throw new BizException(401, "用户已被禁用");
        }

        // 查询角色
        String roleCode = "user";
        String roleName = "普通用户";
        if (user.getRoleId() != null) {
            SysRole role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                roleCode = role.getCode();
                roleName = role.getName();
            }
        }

        // 生成新 Token
        String newAccessToken = jwtUtils.generateAccessToken(user, roleCode);
        String newRefreshToken = jwtUtils.generateRefreshToken(user);

        log.info("Token 刷新成功: userId={}", userId);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .tenantId(user.getTenantId())
                .roleCode(roleCode)
                .roleName(roleName)
                .planType(user.getPlanType())
                .build();

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiresInSeconds())
                .userInfo(userInfo)
                .build();
    }

    @Override
    public void logout(String accessToken) {
        // TODO: 可以将 Token 加入黑名单 (Redis) 实现 Token 失效
        log.info("用户登出");
    }

    @Override
    public LoginResponse.UserInfo getCurrentUser(Long userId, String tenantId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BizException(404, "用户不存在");
        }

        String roleCode = "user";
        String roleName = "普通用户";
        if (user.getRoleId() != null) {
            SysRole role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                roleCode = role.getCode();
                roleName = role.getName();
            }
        }

        return LoginResponse.UserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .tenantId(user.getTenantId())
                .roleCode(roleCode)
                .roleName(roleName)
                .planType(user.getPlanType())
                .build();
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, String username, String tenantId,
                                 String result, String errorMessage, String ip, String userAgent) {
        AuditLog auditLog = new AuditLog();
        auditLog.setTenantId(tenantId);
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setAction("LOGIN");
        auditLog.setResource("USER");
        auditLog.setResourceId(userId != null ? userId.toString() : null);
        auditLog.setResult(result);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setIp(ip);
        auditLog.setUserAgent(userAgent);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogMapper.insert(auditLog);
    }
}
