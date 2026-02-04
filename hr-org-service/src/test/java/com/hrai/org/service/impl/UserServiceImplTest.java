package com.hrai.org.service.impl;

import com.hrai.common.context.TenantContext;
import com.hrai.org.dto.UserCreateRequest;
import com.hrai.org.entity.SysRole;
import com.hrai.org.entity.SysUser;
import com.hrai.org.mapper.SysRoleMapper;
import com.hrai.org.mapper.SysUserMapper;
import com.hrai.org.service.AuditLogService;
import com.hrai.org.service.DataScopeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private DataScopeService dataScopeService;

    @InjectMocks
    private UserServiceImpl userService;

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
    void createUser_encodesPassword() {
        SysRole role = new SysRole();
        role.setId(5L);
        role.setTenantId("tenant_test");

        when(userMapper.selectByUsername(eq("alice"), eq("tenant_test"))).thenReturn(null);
        when(userMapper.selectByEmail(eq("alice@hrai.com"), eq("tenant_test"))).thenReturn(null);
        when(userMapper.selectByPhone(eq("13800000000"), eq("tenant_test"))).thenReturn(null);
        when(roleMapper.selectById(eq(5L))).thenReturn(role);
        when(userMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(99L);
            return 1;
        });

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("alice");
        request.setPassword("plainPassword");
        request.setEmail("alice@hrai.com");
        request.setPhone("13800000000");
        request.setRoleId(5L);
        request.setStatus(1);

        userService.create(request);

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insert(captor.capture());
        SysUser saved = captor.getValue();
        assertNotEquals("plainPassword", saved.getPassword());
        assertEquals("free", saved.getPlanType());
    }
}
