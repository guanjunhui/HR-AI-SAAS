package com.hrai.org.service.impl;

import com.hrai.common.context.TenantContext;
import com.hrai.org.entity.SysRole;
import com.hrai.org.entity.SysUser;
import com.hrai.org.mapper.SysRoleMapper;
import com.hrai.org.mapper.SysUserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        TenantContext.TenantInfo info = new TenantContext.TenantInfo();
        info.setTenantId("tenant_test");
        info.setUserId("1");
        info.setTraceId("trace");
        TenantContext.setTenantInfo(info);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void checkPermission_matchesWildcard() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setTenantId("tenant_test");
        user.setRoleId(10L);
        user.setStatus(1);
        user.setDeleted(0);

        SysRole role = new SysRole();
        role.setId(10L);
        role.setTenantId("tenant_test");
        role.setStatus(1);
        role.setDeleted(0);
        role.setPermissions("[\"hr:*\"]");

        when(userMapper.selectById(eq(1L))).thenReturn(user);
        when(roleMapper.selectById(eq(10L))).thenReturn(role);

        boolean allowed = permissionService.checkPermission(1L, "hr:employee:read");

        assertTrue(allowed);
    }

    @Test
    void listPermissions_includesRolePermissions() {
        SysRole role = new SysRole();
        role.setId(2L);
        role.setTenantId("tenant_test");
        role.setPermissions("[\"custom:read\"]");
        role.setDeleted(0);

        when(roleMapper.selectList(any())).thenReturn(List.of(role));

        List<String> permissions = permissionService.listPermissions();

        assertTrue(permissions.contains("custom:read"));
        assertTrue(permissions.contains("org:unit:read"));
    }
}
