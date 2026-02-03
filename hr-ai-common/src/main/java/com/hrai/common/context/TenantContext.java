package com.hrai.common.context;


/**
 * 租户上下文
 * 使用 ThreadLocal 存储当前请求的租户信息
 * 支持完整的 SaaS 多租户模式
 *
 * @author HR AI Team
 */
public class TenantContext {

    private static final ThreadLocal<TenantInfo> CONTEXT = new ThreadLocal<>();

    /**
     * 租户信息
     */
    public static class TenantInfo {
        private String tenantId;
        private String userId;
        private String sessionId;
        private String planType;
        private String traceId;
        private Long requestTimestamp;

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getPlanType() {
            return planType;
        }

        public void setPlanType(String planType) {
            this.planType = planType;
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public Long getRequestTimestamp() {
            return requestTimestamp;
        }

        public void setRequestTimestamp(Long requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
        }
    }

    /**
     * 设置租户信息
     */
    public static void setTenantInfo(TenantInfo tenantInfo) {
        CONTEXT.set(tenantInfo);
    }

    /**
     * 获取租户信息
     */
    public static TenantInfo getTenantInfo() {
        return CONTEXT.get();
    }

    /**
     * 获取租户 ID
     */
    public static String getTenantId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.getTenantId() : null;
    }

    /**
     * 获取用户 ID
     */
    public static String getUserId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.getUserId() : null;
    }

    /**
     * 获取会话 ID
     */
    public static String getSessionId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.getSessionId() : null;
    }

    /**
     * 获取套餐类型
     */
    public static String getPlanType() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.getPlanType() : "free";
    }

    /**
     * 获取请求追踪 ID
     */
    public static String getTraceId() {
        TenantInfo info = CONTEXT.get();
        return info != null ? info.getTraceId() : null;
    }

    /**
     * 清除租户信息 (请求结束时调用)
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 快速设置租户信息
     */
    public static void set(String tenantId, String userId, String sessionId) {
        TenantInfo info = new TenantInfo();
        info.setTenantId(tenantId);
        info.setUserId(userId);
        info.setSessionId(sessionId);
        info.setRequestTimestamp(System.currentTimeMillis());
        CONTEXT.set(info);
    }

    /**
     * 判断当前是否有租户上下文
     */
    public static boolean hasContext() {
        return CONTEXT.get() != null;
    }

    /**
     * 判断是否为企业版租户
     */
    public static boolean isEnterprise() {
        return "enterprise".equals(getPlanType());
    }

    /**
     * 判断是否为专业版租户
     */
    public static boolean isPro() {
        String planType = getPlanType();
        return "pro".equals(planType) || "enterprise".equals(planType);
    }
}
