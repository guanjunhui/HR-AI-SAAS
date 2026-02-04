package com.hrai.agent.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 限流熔断配置
 * 限流规则通过 Nacos 数据源动态加载（见 application-sentinel.yaml）
 */
@Slf4j
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void init() {
        log.info("Sentinel 限流熔断配置初始化完成，规则通过 Nacos 数据源动态加载");
    }
}
