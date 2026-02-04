package com.hrai.org.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import com.hrai.org.dto.AuditLogDetailResponse;
import com.hrai.org.dto.AuditLogQueryRequest;
import com.hrai.org.dto.PageResponse;
import com.hrai.org.entity.AuditLog;
import com.hrai.org.entity.SysUser;
import com.hrai.org.mapper.AuditLogMapper;
import com.hrai.org.mapper.SysUserMapper;
import com.hrai.org.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 审计日志服务实现
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;
    private final SysUserMapper sysUserMapper;

    public AuditLogServiceImpl(AuditLogMapper auditLogMapper, SysUserMapper sysUserMapper) {
        this.auditLogMapper = auditLogMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResponse<AuditLogDetailResponse> list(AuditLogQueryRequest query) {
        String tenantId = resolveTenantId();
        Page<AuditLog> page = new Page<>(query.getPageNo(), query.getPageSize());

        QueryWrapper<AuditLog> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId);
        if (query.getUserId() != null) {
            wrapper.eq("user_id", query.getUserId());
        }
        if (query.getAction() != null && !query.getAction().isBlank()) {
            wrapper.eq("action", query.getAction());
        }
        if (query.getResource() != null && !query.getResource().isBlank()) {
            wrapper.eq("resource", query.getResource());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.and(w -> w.like("resource_name", query.getKeyword())
                    .or().like("resource_id", query.getKeyword())
                    .or().like("username", query.getKeyword()));
        }
        if (query.getStartTime() != null && query.getEndTime() != null) {
            wrapper.between("created_at", query.getStartTime(), query.getEndTime());
        } else if (query.getStartTime() != null) {
            wrapper.ge("created_at", query.getStartTime());
        } else if (query.getEndTime() != null) {
            wrapper.le("created_at", query.getEndTime());
        }
        wrapper.orderByDesc("created_at");

        IPage<AuditLog> result = auditLogMapper.selectPage(page, wrapper);
        List<AuditLogDetailResponse> records = result.getRecords().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());

        return PageResponse.from(result, records);
    }

    @Override
    public AuditLogDetailResponse getById(Long id) {
        AuditLog log = auditLogMapper.selectById(id);
        if (log == null || !Objects.equals(log.getTenantId(), resolveTenantId())) {
            throw new BizException(404, "审计日志不存在");
        }
        return toDetail(log);
    }

    @Override
    @Transactional
    public void record(String action, String resource, String resourceId, String resourceName, Object detail) {
        AuditLog log = new AuditLog();
        log.setTenantId(resolveTenantId());

        Long userId = resolveUserId();
        log.setUserId(userId);
        log.setUsername(resolveUsername(userId));
        log.setAction(action);
        log.setResource(resource);
        log.setResourceId(resourceId);
        log.setResourceName(resourceName);
        if (detail != null) {
            log.setDetail(JSON.toJSONString(detail));
        }
        log.setResult("SUCCESS");
        log.setTraceId(TenantContext.getTraceId());

        auditLogMapper.insert(log);
    }

    private AuditLogDetailResponse toDetail(AuditLog log) {
        AuditLogDetailResponse resp = new AuditLogDetailResponse();
        resp.setId(log.getId());
        resp.setTenantId(log.getTenantId());
        resp.setUserId(log.getUserId());
        resp.setUsername(log.getUsername());
        resp.setAction(log.getAction());
        resp.setResource(log.getResource());
        resp.setResourceId(log.getResourceId());
        resp.setResourceName(log.getResourceName());
        resp.setDetail(log.getDetail());
        resp.setResult(log.getResult());
        resp.setErrorMessage(log.getErrorMessage());
        resp.setIp(log.getIp());
        resp.setUserAgent(log.getUserAgent());
        resp.setDuration(log.getDuration());
        resp.setTraceId(log.getTraceId());
        resp.setCreatedAt(log.getCreatedAt());
        return resp;
    }

    private String resolveTenantId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? TenantConstants.DEFAULT_TENANT_ID : tenantId;
    }

    private Long resolveUserId() {
        String userId = TenantContext.getUserId();
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getUsername() : null;
    }
}
