package com.hrai.org.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.org.entity.SysRole;
import com.hrai.org.entity.SysUser;
import com.hrai.org.mapper.SysRoleMapper;
import com.hrai.org.mapper.SysUserMapper;
import com.hrai.org.service.PermissionService;
import com.hrai.org.util.PermissionCatalog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Permission service implementation.
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    public PermissionServiceImpl(SysUserMapper userMapper, SysRoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public List<String> listPermissions() {
        String tenantId = resolveTenantId();
        Set<String> codes = PermissionCatalog.defaultCodes();

        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId).eq("deleted", 0);
        List<SysRole> roles = roleMapper.selectList(wrapper);
        if (roles != null) {
            for (SysRole role : roles) {
                List<String> rolePerms = parsePermissions(role.getPermissions());
                codes.addAll(rolePerms);
            }
        }

        List<String> result = new ArrayList<>(codes);
        Collections.sort(result);
        return result;
    }

    @Override
    public boolean checkPermission(Long userId, String permission) {
        if (userId == null || permission == null || permission.isBlank()) {
            return false;
        }
        String tenantId = resolveTenantId();
        SysUser user = userMapper.selectById(userId);
        if (user == null || isDeleted(user.getDeleted()) || user.getStatus() == null || user.getStatus() != 1) {
            return false;
        }
        if (!Objects.equals(user.getTenantId(), tenantId)) {
            return false;
        }
        if (user.getRoleId() == null) {
            return false;
        }

        SysRole role = roleMapper.selectById(user.getRoleId());
        if (role == null || isDeleted(role.getDeleted()) || role.getStatus() == null || role.getStatus() != 1) {
            return false;
        }
        if (!Objects.equals(role.getTenantId(), tenantId)) {
            return false;
        }

        List<String> permissions = parsePermissions(role.getPermissions());
        for (String granted : permissions) {
            if (matchPermission(granted, permission)) {
                return true;
            }
        }
        return false;
    }

    private List<String> parsePermissions(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseArray(raw, String.class);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private boolean matchPermission(String granted, String required) {
        if (granted == null || required == null) {
            return false;
        }
        String grantedTrim = granted.trim();
        if (grantedTrim.isEmpty()) {
            return false;
        }
        if ("*".equals(grantedTrim)) {
            return true;
        }
        if (grantedTrim.endsWith("*")) {
            String prefix = grantedTrim.substring(0, grantedTrim.length() - 1);
            return required.startsWith(prefix);
        }
        return grantedTrim.equals(required);
    }

    private String resolveTenantId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? TenantConstants.DEFAULT_TENANT_ID : tenantId;
    }

    private boolean isDeleted(Integer deleted) {
        return deleted != null && deleted == 1;
    }
}
