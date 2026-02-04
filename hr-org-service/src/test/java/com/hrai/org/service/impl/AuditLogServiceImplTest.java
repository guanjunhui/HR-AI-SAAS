package com.hrai.org.service.impl;

import com.hrai.common.context.TenantContext;
import com.hrai.org.entity.AuditLog;
import com.hrai.org.entity.SysUser;
import com.hrai.org.mapper.AuditLogMapper;
import com.hrai.org.mapper.SysUserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogMapper auditLogMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @BeforeEach
    void setUp() {
        TenantContext.TenantInfo info = new TenantContext.TenantInfo();
        info.setTenantId("tenant_test");
        info.setUserId("2");
        info.setTraceId("trace");
        TenantContext.setTenantInfo(info);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void record_setsTenantAndUsername() {
        SysUser user = new SysUser();
        user.setId(2L);
        user.setUsername("tester");
        when(sysUserMapper.selectById(eq(2L))).thenReturn(user);

        auditLogService.record("CREATE", "ORG_UNIT", "1", "总部", null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals("tenant_test", log.getTenantId());
        assertEquals("tester", log.getUsername());
        assertEquals("ORG_UNIT", log.getResource());
    }
}
