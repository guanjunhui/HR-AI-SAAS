package com.hrai.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的源 (生产环境需要配置具体域名)
        config.addAllowedOriginPattern("*");

        // 允许的 HTTP 方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 允许的请求头
        config.addAllowedHeader("*");

        // 允许发送凭证 (Cookie)
        config.setAllowCredentials(true);

        // 预检请求缓存时间 (秒)
        config.setMaxAge(3600L);

        // 暴露的响应头
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Tenant-Id",
            "X-User-Id",
            "X-Trace-Id",
            "X-Request-Id"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
