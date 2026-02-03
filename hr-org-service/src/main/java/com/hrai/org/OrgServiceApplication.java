package com.hrai.org;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 组织鉴权服务启动类
 *
 * 功能：
 * - 用户认证 (登录/注册/Token 刷新)
 * - 用户管理
 * - 角色权限管理
 * - 组织架构管理
 * - 审计日志
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.hrai.org.mapper")
public class OrgServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrgServiceApplication.class, args);
    }
}
