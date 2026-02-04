package com.hrai.org.interceptor;

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
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    private static final String PLAN_TYPE_HEADER = "X-Plan-Type";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String tenantId = request.getHeader(TenantConstants.TENANT_ID_HEADER);
        String userId = request.getHeader(TenantConstants.USER_ID_HEADER);
        String sessionId = request.getHeader(TenantConstants.SESSION_ID_HEADER);
        String planType = request.getHeader(PLAN_TYPE_HEADER);
        String traceId = request.getHeader(TRACE_ID_HEADER);

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

        TenantContext.TenantInfo tenantInfo = new TenantContext.TenantInfo();
        tenantInfo.setTenantId(tenantId);
        tenantInfo.setUserId(userId);
        tenantInfo.setSessionId(sessionId);
        tenantInfo.setPlanType(planType);
        tenantInfo.setTraceId(traceId);
        tenantInfo.setRequestTimestamp(System.currentTimeMillis());
        TenantContext.setTenantInfo(tenantInfo);

        response.setHeader(TRACE_ID_HEADER, traceId);
        log.debug("租户上下文已设置: tenantId={}, userId={}, sessionId={}, planType={}, traceId={}",
                tenantId, userId, sessionId, planType, traceId);
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler,
                           ModelAndView modelAndView) {
        // no-op
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        TenantContext.clear();
        log.debug("租户上下文已清理");
    }
}
