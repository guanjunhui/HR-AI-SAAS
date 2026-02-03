package com.hrai.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置类
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT 密钥 (至少 256 位 / 32 字符)
     */
    private String secret;

    /**
     * Access Token 过期时间 (毫秒)
     * 默认 8 小时
     */
    private Long expiration = 28800000L;

    /**
     * 签发者
     */
    private String issuer = "hr-ai-saas";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
