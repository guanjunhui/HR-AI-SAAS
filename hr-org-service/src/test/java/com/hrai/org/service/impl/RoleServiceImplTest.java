package com.hrai.org.service.impl;

import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import com.hrai.org.dto.RolePermissionsUpdateRequest;
import com.hrai.org.entity.SysMenu;
import com.hrai.org.entity.SysRole;
import com.hrai.org.entity.SysRolePermission;
import com.hrai.org.mapper.SysMenuMapper;
import com.hrai.org.mapper.SysRoleMapper;
import com.hrai.org.mapper.SysRolePermissionMapper;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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

    @Mock
    private SysRolePermissionMapper rolePermissionMapper;

    @Mock
    private SysMenuMapper menuMapper;

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
    void updatePermissions_replacesRolePermissions() {
        SysRole role = new SysRole();
        role.setId(10L);
        role.setTenantId("tenant_test");

        when(roleMapper.selectById(eq(10L))).thenReturn(role);
        SysMenu menu1 = new SysMenu();
        menu1.setPermissionCode("user:read");
        SysMenu menu2 = new SysMenu();
        menu2.setPermissionCode("user:write");
        when(menuMapper.selectList(any())).thenReturn(List.of(menu1, menu2));

        RolePermissionsUpdateRequest request = new RolePermissionsUpdateRequest();
        request.setPermissions(List.of("user:read", "user:write"));

        roleService.updatePermissions(10L, request);

        verify(rolePermissionMapper).deleteByRoleId(eq("tenant_test"), eq(10L));
        ArgumentCaptor<SysRolePermission> captor = ArgumentCaptor.forClass(SysRolePermission.class);
        verify(rolePermissionMapper, times(2)).insert(captor.capture());
        List<String> codes = captor.getAllValues().stream()
                .map(SysRolePermission::getPermissionCode)
                .collect(Collectors.toList());
        assertTrue(codes.contains("user:read"));
        assertTrue(codes.contains("user:write"));
    }

    @Test
    void updatePermissions_rejectsUnknownPermission() {
        SysRole role = new SysRole();
        role.setId(11L);
        role.setTenantId("tenant_test");

        when(roleMapper.selectById(eq(11L))).thenReturn(role);
        SysMenu menu = new SysMenu();
        menu.setPermissionCode("user:read");
        when(menuMapper.selectList(any())).thenReturn(List.of(menu));

        RolePermissionsUpdateRequest request = new RolePermissionsUpdateRequest();
        request.setPermissions(List.of("user:read", "unknown:write"));

        BizException ex = assertThrows(BizException.class, () -> roleService.updatePermissions(11L, request));
        assertTrue(ex.getMessage().contains("权限编码不存在"));
    }
}
