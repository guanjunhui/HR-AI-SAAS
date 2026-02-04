package com.hrai.org.controller;

import com.hrai.common.dto.Result;
import com.hrai.org.dto.AuditLogDetailResponse;
import com.hrai.org.dto.AuditLogQueryRequest;
import com.hrai.org.dto.PageResponse;
import com.hrai.org.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志接口
 */
@RestController
@RequestMapping("/api/v1/org/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public Result<PageResponse<AuditLogDetailResponse>> list(@ModelAttribute AuditLogQueryRequest query) {
        return Result.success(auditLogService.list(query));
    }

    @GetMapping("/{id}")
    public Result<AuditLogDetailResponse> getById(@PathVariable("id") Long id) {
        return Result.success(auditLogService.getById(id));
    }
}
