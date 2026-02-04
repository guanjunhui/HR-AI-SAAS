package com.hrai.org.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import com.hrai.org.dto.RoleCreateRequest;
import com.hrai.org.dto.RoleDetailResponse;
import com.hrai.org.dto.RolePermissionsUpdateRequest;
import com.hrai.org.dto.RoleUpdateRequest;
import com.hrai.org.entity.SysMenu;
import com.hrai.org.entity.SysRole;
import com.hrai.org.entity.SysRolePermission;
import com.hrai.org.entity.SysUser;
import com.hrai.org.mapper.SysMenuMapper;
import com.hrai.org.mapper.SysRoleMapper;
import com.hrai.org.mapper.SysRolePermissionMapper;
import com.hrai.org.mapper.SysUserMapper;
import com.hrai.org.service.AuditLogService;
import com.hrai.org.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final AuditLogService auditLogService;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysMenuMapper menuMapper;

    public RoleServiceImpl(SysRoleMapper roleMapper,
                           SysUserMapper userMapper,
                           AuditLogService auditLogService,
                           SysRolePermissionMapper rolePermissionMapper,
                           SysMenuMapper menuMapper) {
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
        this.rolePermissionMapper = rolePermissionMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public List<RoleDetailResponse> list(String keyword, Integer status) {
        String tenantId = resolveTenantId();
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId).eq("deleted", 0);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("name", keyword).or().like("code", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByAsc("sort_order", "id");
        List<SysRole> roles = roleMapper.selectList(wrapper);
        return roles.stream().map(this::toDetail).collect(Collectors.toList());
    }

    @Override
    public RoleDetailResponse getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || isDeleted(role.getDeleted()) || !Objects.equals(role.getTenantId(), resolveTenantId())) {
            throw new BizException(404, "角色不存在");
        }
        return toDetail(role);
    }

    @Override
    @Transactional
    public Long create(RoleCreateRequest request) {
        String tenantId = resolveTenantId();
        SysRole exist = roleMapper.selectByCode(request.getCode(), tenantId);
        if (exist != null) {
            throw new BizException(409, "角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setTenantId(tenantId);
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setDataScope(request.getDataScope());
        role.setStatus(request.getStatus());
        role.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());

        roleMapper.insert(role);
        List<String> permissions = normalizePermissions(request.getPermissions());
        validatePermissions(tenantId, permissions);
        replaceRolePermissions(tenantId, role.getId(), permissions);
        auditLogService.record("CREATE", "ROLE", String.valueOf(role.getId()), role.getName(), request);
        return role.getId();
    }

    @Override
    @Transactional
    public void update(Long id, RoleUpdateRequest request) {
        String tenantId = resolveTenantId();
        SysRole role = roleMapper.selectById(id);
        if (role == null || isDeleted(role.getDeleted()) || !Objects.equals(role.getTenantId(), tenantId)) {
            throw new BizException(404, "角色不存在");
        }

        SysRole exist = roleMapper.selectByCode(request.getCode(), tenantId);
        if (exist != null && !Objects.equals(exist.getId(), id)) {
            throw new BizException(409, "角色编码已存在");
        }

        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setDataScope(request.getDataScope());
        role.setStatus(request.getStatus());
        role.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        roleMapper.updateById(role);
        List<String> permissions = normalizePermissions(request.getPermissions());
        validatePermissions(tenantId, permissions);
        replaceRolePermissions(tenantId, role.getId(), permissions);

        auditLogService.record("UPDATE", "ROLE", String.valueOf(role.getId()), role.getName(), request);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        String tenantId = resolveTenantId();
        SysRole role = roleMapper.selectById(id);
        if (role == null || isDeleted(role.getDeleted()) || !Objects.equals(role.getTenantId(), tenantId)) {
            throw new BizException(404, "角色不存在");
        }

        QueryWrapper<SysUser> userWrapper = new QueryWrapper<>();
        userWrapper.eq("role_id", id).eq("tenant_id", tenantId).eq("deleted", 0);
        Long count = userMapper.selectCount(userWrapper);
        if (count != null && count > 0) {
            throw new BizException(400, "角色已被用户使用，无法删除");
        }

        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("tenant_id", tenantId);
        roleMapper.delete(wrapper);

        auditLogService.record("DELETE", "ROLE", String.valueOf(id), role.getName(), null);
    }

    @Override
    @Transactional
    public void updatePermissions(Long id, RolePermissionsUpdateRequest request) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || isDeleted(role.getDeleted()) || !Objects.equals(role.getTenantId(), resolveTenantId())) {
            throw new BizException(404, "角色不存在");
        }
        List<String> permissions = normalizePermissions(request.getPermissions());
        validatePermissions(role.getTenantId(), permissions);
        replaceRolePermissions(role.getTenantId(), role.getId(), permissions);
        auditLogService.record("UPDATE_PERMISSIONS", "ROLE", String.valueOf(id), role.getName(), request);
    }

    private RoleDetailResponse toDetail(SysRole role) {
        RoleDetailResponse resp = new RoleDetailResponse();
        resp.setId(role.getId());
        resp.setName(role.getName());
        resp.setCode(role.getCode());
        resp.setDescription(role.getDescription());
        resp.setPermissions(rolePermissionMapper.selectCodesByRoleId(role.getTenantId(), role.getId()));
        resp.setDataScope(role.getDataScope());
        resp.setStatus(role.getStatus());
        resp.setSortOrder(role.getSortOrder());
        resp.setCreatedAt(role.getCreatedAt());
        resp.setUpdatedAt(role.getUpdatedAt());
        return resp;
    }

    private void replaceRolePermissions(String tenantId, Long roleId, List<String> permissions) {
        rolePermissionMapper.deleteByRoleId(tenantId, roleId);
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        for (String code : permissions) {
            SysRolePermission record = new SysRolePermission();
            record.setTenantId(tenantId);
            record.setRoleId(roleId);
            record.setPermissionCode(code);
            rolePermissionMapper.insert(record);
        }
    }

    private void validatePermissions(String tenantId, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        List<String> toCheck = permissions.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .filter(code -> !isWildcard(code))
                .collect(Collectors.toList());
        if (toCheck.isEmpty()) {
            return;
        }
        QueryWrapper<SysMenu> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId).eq("deleted", 0);
        List<SysMenu> menus = menuMapper.selectList(wrapper);
        Set<String> catalog = new LinkedHashSet<>();
        if (menus != null) {
            for (SysMenu menu : menus) {
                if (menu.getPermissionCode() != null && !menu.getPermissionCode().isBlank()) {
                    catalog.add(menu.getPermissionCode().trim());
                }
            }
        }
        List<String> unknown = toCheck.stream()
                .filter(code -> !catalog.contains(code))
                .collect(Collectors.toList());
        if (!unknown.isEmpty()) {
            throw new BizException(400, "权限编码不存在: " + String.join(",", unknown));
        }
    }

    private boolean isWildcard(String code) {
        if (code == null) {
            return false;
        }
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        return "*".equals(trimmed) || trimmed.endsWith("*");
    }

    private List<String> normalizePermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .collect(Collectors.toList());
    }

    private String resolveTenantId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? TenantConstants.DEFAULT_TENANT_ID : tenantId;
    }

    private boolean isDeleted(Integer deleted) {
        return deleted != null && deleted == 1;
    }
}
