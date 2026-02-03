package com.hrai.org.util;

import com.hrai.org.config.JwtConfig;
import com.hrai.org.entity.SysUser;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 *
 * 用于生成和验证 JWT Token
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
     * 生成 Access Token
     *
     * @param user     用户信息
     * @param roleCode 角色编码
     * @return Access Token
     */
    public String generateAccessToken(SysUser user, String roleCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("tenantId", user.getTenantId());
        claims.put("roleCode", roleCode);
        claims.put("planType", user.getPlanType());

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成 Refresh Token
     *
     * @param user 用户信息
     * @return Refresh Token
     */
    public String generateRefreshToken(SysUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("tenantId", user.getTenantId());
        claims.put("type", "refresh");

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getRefreshExpiration());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
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
     * 验证是否是 Refresh Token
     *
     * @param token JWT Token
     * @return true 是 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
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
     * 获取 Token 过期时间 (秒)
     */
    public long getExpiresInSeconds() {
        return jwtConfig.getExpiration() / 1000;
    }
}
