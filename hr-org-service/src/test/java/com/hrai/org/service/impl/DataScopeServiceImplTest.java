package com.hrai.org.service.impl;

import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import com.hrai.org.entity.OrgUnit;
import com.hrai.org.entity.SysRole;
import com.hrai.org.entity.SysUser;
import com.hrai.org.mapper.OrgUnitMapper;
import com.hrai.org.mapper.SysRoleMapper;
import com.hrai.org.mapper.SysUserMapper;
import com.hrai.org.service.DataScopeContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataScopeServiceImplTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private OrgUnitMapper orgUnitMapper;

    @InjectMocks
    private DataScopeServiceImpl dataScopeService;

    @BeforeEach
    void setUp() {
        TenantContext.TenantInfo info = new TenantContext.TenantInfo();
        info.setTenantId("tenant_test");
        info.setUserId("100");
        info.setTraceId("trace");
        TenantContext.setTenantInfo(info);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getCurrentScope_deptAndChildren() {
        SysUser user = new SysUser();
        user.setId(100L);
        user.setTenantId("tenant_test");
        user.setRoleId(1L);
        user.setOrgUnitId(10L);
        user.setDeleted(0);

        SysRole role = new SysRole();
        role.setId(1L);
        role.setTenantId("tenant_test");
        role.setDataScope(2);
        role.setDeleted(0);

        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setId(10L);
        orgUnit.setTenantId("tenant_test");
        orgUnit.setPath("/10/");
        orgUnit.setDeleted(0);

        OrgUnit child = new OrgUnit();
        child.setId(11L);
        child.setTenantId("tenant_test");
        child.setPath("/10/11/");
        child.setDeleted(0);

        when(userMapper.selectById(eq(100L))).thenReturn(user);
        when(roleMapper.selectById(eq(1L))).thenReturn(role);
        when(orgUnitMapper.selectById(eq(10L))).thenReturn(orgUnit);
        when(orgUnitMapper.selectDescendants(eq("/10/"), eq("tenant_test")))
                .thenReturn(List.of(child));

        DataScopeContext context = dataScopeService.getCurrentScope();

        assertEquals(2, context.getDataScope());
        assertTrue(context.getOrgUnitIds().contains(10L));
        assertTrue(context.getOrgUnitIds().contains(11L));
    }

    @Test
    void assertUserAccessible_selfScopeDeniesOther() {
        SysUser user = new SysUser();
        user.setId(100L);
        user.setTenantId("tenant_test");
        user.setRoleId(1L);
        user.setOrgUnitId(10L);
        user.setDeleted(0);

        SysRole role = new SysRole();
        role.setId(1L);
        role.setTenantId("tenant_test");
        role.setDataScope(4);
        role.setDeleted(0);

        when(userMapper.selectById(eq(100L))).thenReturn(user);
        when(roleMapper.selectById(eq(1L))).thenReturn(role);

        SysUser target = new SysUser();
        target.setId(200L);
        target.setTenantId("tenant_test");
        target.setDeleted(0);

        BizException ex = assertThrows(BizException.class, () -> dataScopeService.assertUserAccessible(target));
        assertEquals(403, ex.getCode());
    }
}
