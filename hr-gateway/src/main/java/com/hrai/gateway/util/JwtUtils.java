package com.hrai.gateway.util;

import com.hrai.gateway.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 工具类
 *
 * 用于验证和解析 JWT Token
 */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final JwtConfig jwtConfig;

    public JwtUtils(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 解析 Token 获取 Claims
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true 有效, false 无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token 已过期: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("JWT Token 无效: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT Token 解析异常", e);
            return false;
        }
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return Long.valueOf(userId.toString());
    }

    /**
     * 从 Token 中获取租户 ID
     */
    public String getTenantId(String token) {
        Claims claims = parseToken(token);
        return claims.get("tenantId", String.class);
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从 Token 中获取角色编码
     */
    public String getRoleCode(String token) {
        Claims claims = parseToken(token);
        return claims.get("roleCode", String.class);
    }

    /**
     * 从 Token 中获取套餐类型
     */
    public String getPlanType(String token) {
        Claims claims = parseToken(token);
        return claims.get("planType", String.class);
    }
}
