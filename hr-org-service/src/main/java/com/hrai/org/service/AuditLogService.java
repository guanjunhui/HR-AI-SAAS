package com.hrai.org.service;

import com.hrai.org.dto.AuditLogDetailResponse;
import com.hrai.org.dto.AuditLogQueryRequest;
import com.hrai.org.dto.PageResponse;

/**
 * 审计日志服务
 */
public interface AuditLogService {

    PageResponse<AuditLogDetailResponse> list(AuditLogQueryRequest query);

    AuditLogDetailResponse getById(Long id);

    void record(String action, String resource, String resourceId, String resourceName, Object detail);
}
