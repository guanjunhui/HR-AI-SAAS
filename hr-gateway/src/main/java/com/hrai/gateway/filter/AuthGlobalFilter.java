package com.hrai.gateway.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.hrai.gateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局认证过滤器
 *
 * 功能：
 * - 验证 JWT Token
 * - 提取用户信息传递到下游服务
 * - 生成链路追踪 ID
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);

    private final JwtUtils jwtUtils;

    public AuthGlobalFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 白名单路径 (无需认证)
     */
    private static final List<String> WHITE_LIST = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/captcha",
            "/actuator",
            "/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 生成链路追踪 ID
        String traceId = IdUtil.fastSimpleUUID();

        // 检查是否在白名单中
        if (isWhiteListed(path)) {
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Trace-Id", traceId)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        // 获取 Authorization 请求头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "缺少认证信息");
        }

        // 提取 Token
        String token = authHeader.substring(7);

        // 验证 Token
        if (!jwtUtils.validateToken(token)) {
            return unauthorized(exchange, "Token 无效或已过期");
        }

        // 解析 Token 获取用户信息
        try {
            Claims claims = jwtUtils.parseToken(token);

            String tenantId = claims.get("tenantId", String.class);
            Object userIdObj = claims.get("userId");
            String userId = userIdObj != null ? userIdObj.toString() : null;
            String username = claims.getSubject();
            String roleCode = claims.get("roleCode", String.class);
            String planType = claims.get("planType", String.class);

            // 构建下游请求，传递用户信息
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Trace-Id", traceId)
                    .header("X-Tenant-Id", StrUtil.nullToDefault(tenantId, ""))
                    .header("X-User-Id", StrUtil.nullToDefault(userId, ""))
                    .header("X-Username", StrUtil.nullToDefault(username, ""))
                    .header("X-Role-Code", StrUtil.nullToDefault(roleCode, ""))
                    .header("X-Plan-Type", StrUtil.nullToDefault(planType, "free"))
                    .build();

            log.debug("请求通过认证: path={}, tenantId={}, userId={}, traceId={}",
                    path, tenantId, userId, traceId);

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("Token 解析异常: {}", e.getMessage());
            return unauthorized(exchange, "Token 解析失败");
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> {
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                return path.startsWith(prefix);
            }
            return path.startsWith(pattern);
        });
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                message, System.currentTimeMillis()
        );

        return response.writeWith(Mono.just(
                response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))
        ));
    }

    @Override
    public int getOrder() {
        // 认证过滤器优先级较高，但在某些系统过滤器之后
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
