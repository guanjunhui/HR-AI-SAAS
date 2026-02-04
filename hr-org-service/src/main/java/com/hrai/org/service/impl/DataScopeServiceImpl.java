package com.hrai.org.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import com.hrai.org.entity.OrgUnit;
import com.hrai.org.entity.SysRole;
import com.hrai.org.entity.SysUser;
import com.hrai.org.mapper.OrgUnitMapper;
import com.hrai.org.mapper.SysRoleMapper;
import com.hrai.org.mapper.SysUserMapper;
import com.hrai.org.service.DataScopeContext;
import com.hrai.org.service.DataScopeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Default data scope service implementation.
 */
@Service
public class DataScopeServiceImpl implements DataScopeService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final OrgUnitMapper orgUnitMapper;

    public DataScopeServiceImpl(SysUserMapper userMapper, SysRoleMapper roleMapper, OrgUnitMapper orgUnitMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.orgUnitMapper = orgUnitMapper;
    }

    @Override
    public DataScopeContext getCurrentScope() {
        String tenantId = resolveTenantId();
        Long userId = resolveUserId();
        if (userId == null) {
            return new DataScopeContext(1, null, null, Collections.emptyList(), false);
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null || isDeleted(user.getDeleted()) || !Objects.equals(user.getTenantId(), tenantId)) {
            throw new BizException(403, "无权限");
        }

        Integer dataScope = 4;
        if (user.getRoleId() != null) {
            SysRole role = roleMapper.selectById(user.getRoleId());
            if (role != null && !isDeleted(role.getDeleted()) && Objects.equals(role.getTenantId(), tenantId)) {
                if (role.getDataScope() != null) {
                    dataScope = role.getDataScope();
                }
            }
        }

        Long orgUnitId = user.getOrgUnitId();
        List<Long> orgUnitIds = resolveOrgUnitIds(dataScope, orgUnitId, tenantId);
        return new DataScopeContext(dataScope, userId, orgUnitId, orgUnitIds, true);
    }

    @Override
    public void applyUserScope(QueryWrapper<SysUser> wrapper) {
        DataScopeContext scope = getCurrentScope();
        if (!scope.isEnforced() || scope.isAllScope()) {
            return;
        }
        if (scope.isSelfScope()) {
            if (scope.getUserId() == null) {
                wrapper.eq("id", -1L);
            } else {
                wrapper.eq("id", scope.getUserId());
            }
            return;
        }

        List<Long> orgUnitIds = scope.getOrgUnitIds();
        if (orgUnitIds == null || orgUnitIds.isEmpty()) {
            wrapper.eq("id", -1L);
            return;
        }
        wrapper.in("org_unit_id", orgUnitIds);
    }

    @Override
    public void assertUserAccessible(SysUser targetUser) {
        if (targetUser == null) {
            throw new BizException(404, "用户不存在");
        }
        DataScopeContext scope = getCurrentScope();
        if (!scope.isEnforced() || scope.isAllScope()) {
            return;
        }
        if (scope.isSelfScope()) {
            if (!Objects.equals(targetUser.getId(), scope.getUserId())) {
                throw new BizException(403, "无权限");
            }
            return;
        }

        Long targetOrgUnitId = targetUser.getOrgUnitId();
        if (targetOrgUnitId == null || scope.getOrgUnitIds().isEmpty() || !scope.getOrgUnitIds().contains(targetOrgUnitId)) {
            throw new BizException(403, "无权限");
        }
    }

    @Override
    public void assertOrgUnitAccessible(Long orgUnitId) {
        DataScopeContext scope = getCurrentScope();
        if (!scope.isEnforced() || scope.isAllScope()) {
            return;
        }
        if (orgUnitId == null) {
            throw new BizException(403, "无权限");
        }
        if (scope.getOrgUnitIds().isEmpty() || !scope.getOrgUnitIds().contains(orgUnitId)) {
            throw new BizException(403, "无权限");
        }
    }

    @Override
    public List<Long> getAccessibleOrgUnitIds() {
        DataScopeContext scope = getCurrentScope();
        if (!scope.isEnforced() || scope.isAllScope()) {
            return Collections.emptyList();
        }
        return scope.getOrgUnitIds();
    }

    private List<Long> resolveOrgUnitIds(Integer dataScope, Long orgUnitId, String tenantId) {
        if (dataScope == null || dataScope == 1) {
            return Collections.emptyList();
        }
        if (orgUnitId == null) {
            return Collections.emptyList();
        }
        if (dataScope == 3 || dataScope == 4) {
            return List.of(orgUnitId);
        }
        if (dataScope != 2) {
            return Collections.emptyList();
        }

        OrgUnit orgUnit = orgUnitMapper.selectById(orgUnitId);
        if (orgUnit == null || orgUnit.getDeleted() == 1 || !Objects.equals(orgUnit.getTenantId(), tenantId)) {
            return List.of(orgUnitId);
        }
        String path = orgUnit.getPath();
        if (path == null || path.isBlank()) {
            return List.of(orgUnitId);
        }

        List<OrgUnit> descendants = orgUnitMapper.selectDescendants(path, tenantId);
        if (descendants == null || descendants.isEmpty()) {
            return List.of(orgUnitId);
        }
        List<Long> ids = descendants.stream()
                .map(OrgUnit::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        if (!ids.contains(orgUnitId)) {
            ids.add(orgUnitId);
        }
        return ids;
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
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isDeleted(Integer deleted) {
        return deleted != null && deleted == 1;
    }
}
