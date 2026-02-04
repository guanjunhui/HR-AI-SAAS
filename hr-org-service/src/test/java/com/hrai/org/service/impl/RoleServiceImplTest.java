package com.hrai.org.service.impl;

import com.hrai.common.context.TenantContext;
import com.hrai.org.dto.RolePermissionsUpdateRequest;
import com.hrai.org.entity.SysRole;
import com.hrai.org.mapper.SysRoleMapper;
import com.hrai.org.mapper.SysUserMapper;
import com.hrai.org.service.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RoleServiceImpl roleService;

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
    void updatePermissions_writesJson() {
        SysRole role = new SysRole();
        role.setId(10L);
        role.setTenantId("tenant_test");

        when(roleMapper.selectById(eq(10L))).thenReturn(role);

        RolePermissionsUpdateRequest request = new RolePermissionsUpdateRequest();
        request.setPermissions(List.of("user:read", "user:write"));

        roleService.updatePermissions(10L, request);

        ArgumentCaptor<SysRole> captor = ArgumentCaptor.forClass(SysRole.class);
        verify(roleMapper).updateById(captor.capture());
        String permissions = captor.getValue().getPermissions();
        assertTrue(permissions.contains("user:read"));
        assertTrue(permissions.contains("user:write"));
    }
}
