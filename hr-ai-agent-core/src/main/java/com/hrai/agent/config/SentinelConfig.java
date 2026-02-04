package com.hrai.agent.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 限流熔断配置
 * 限流规则通过 Nacos 数据源动态加载（见 application-sentinel.yaml）
 */
@Configuration
public class SentinelConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentinelConfig.class);

    @PostConstruct
    public void init() {
        LOGGER.info("Sentinel 限流熔断配置初始化完成，规则通过 Nacos 数据源动态加载");
    }
}
