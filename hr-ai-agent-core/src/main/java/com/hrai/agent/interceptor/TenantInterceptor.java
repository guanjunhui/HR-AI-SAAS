package com.hrai.agent.interceptor;

import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * 租户拦截器
 * 从请求头中提取租户信息并设置到 TenantContext
 *
 * @author HR AI Team
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    /**
     * 套餐类型请求头
     */
    private static final String PLAN_TYPE_HEADER = "X-Plan-Type";

    /**
     * 请求追踪 ID 请求头
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        // 提取租户信息
        String tenantId = request.getHeader(TenantConstants.TENANT_ID_HEADER);
        String userId = request.getHeader(TenantConstants.USER_ID_HEADER);
        String sessionId = request.getHeader(TenantConstants.SESSION_ID_HEADER);
        String planType = request.getHeader(PLAN_TYPE_HEADER);
        String traceId = request.getHeader(TRACE_ID_HEADER);

        // 使用默认值
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = TenantConstants.DEFAULT_TENANT_ID;
        }
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        if (planType == null || planType.isEmpty()) {
            planType = "free";
        }

        // 构建租户信息
        TenantContext.TenantInfo tenantInfo = new TenantContext.TenantInfo();
        tenantInfo.setTenantId(tenantId);
        tenantInfo.setUserId(userId);
        tenantInfo.setSessionId(sessionId);
        tenantInfo.setPlanType(planType);
        tenantInfo.setTraceId(traceId);
        tenantInfo.setRequestTimestamp(System.currentTimeMillis());

        // 设置到上下文
        TenantContext.setTenantInfo(tenantInfo);

        // 设置响应头，便于追踪
        response.setHeader(TRACE_ID_HEADER, traceId);

        log.debug("租户上下文已设置: tenantId={}, userId={}, sessionId={}, planType={}, traceId={}",
                tenantId, userId, sessionId, planType, traceId);

        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler,
                           ModelAndView modelAndView) throws Exception {
        // 请求处理完成后的操作（如果需要）
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) throws Exception {
        // 清理租户上下文，防止内存泄漏
        TenantContext.clear();
        log.debug("租户上下文已清理");
    }
}
