package com.hrai.common.constant;

/**
 * 租户相关常量
 *
 * @author HR AI Team
 */
public interface TenantConstants {

    /**
     * 租户ID请求头
     */
    String TENANT_ID_HEADER = "X-Tenant-Id";

    /**
     * 用户ID请求头
     */
    String USER_ID_HEADER = "X-User-Id";

    /**
     * 会话ID请求头
     */
    String SESSION_ID_HEADER = "X-Session-Id";

    /**
     * 默认租户ID (用于测试)
     */
    String DEFAULT_TENANT_ID = "tenant_default";

    /**
     * Redis Key 前缀
     */
    String REDIS_PREFIX = "hrai";

    /**
     * 会话上下文 Redis Key 前缀
     */
    String SESSION_PREFIX = REDIS_PREFIX + ":session:";
}
