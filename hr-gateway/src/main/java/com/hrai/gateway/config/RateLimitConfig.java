package com.hrai.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * 限流配置
 *
 * 基于 Redis 的请求限流，按 租户ID + IP 进行限流
 */
@Configuration
public class RateLimitConfig {

    /**
     * 按租户 ID 限流 (默认)
     * 如果请求头中没有 X-Tenant-Id，则使用 IP 地址
     */
    @Bean
    @Primary
    public KeyResolver tenantKeyResolver() {
        return exchange -> {
            String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
            if (tenantId != null && !tenantId.isEmpty()) {
                return Mono.just("tenant:" + tenantId);
            }

            // 回退到 IP 地址
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just("ip:" + ip);
        };
    }

    /**
     * 按 IP 地址限流
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just("ip:" + ip);
        };
    }

    /**
     * 按用户 ID 限流
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user:" + userId);
            }
            return Mono.just("anonymous");
        };
    }
}
