package com.hrai.org.service.impl;

import com.hrai.common.context.TenantContext;
import com.hrai.org.dto.OrgUnitCreateRequest;
import com.hrai.org.entity.OrgUnit;
import com.hrai.org.mapper.OrgUnitMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrgUnitServiceImplTest {

    @Mock
    private OrgUnitMapper orgUnitMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private DataScopeService dataScopeService;

    @InjectMocks
    private OrgUnitServiceImpl orgUnitService;

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
    void createRootOrgUnit_setsPathAndLevel() {
        OrgUnitCreateRequest request = new OrgUnitCreateRequest();
        request.setParentId(0L);
        request.setName("总部");
        request.setCode("HQ");
        request.setType("company");
        request.setStatus(1);

        when(orgUnitMapper.selectByCode(eq("HQ"), eq("tenant_test"))).thenReturn(null);
        when(orgUnitMapper.insert(any(OrgUnit.class))).thenAnswer(invocation -> {
            OrgUnit unit = invocation.getArgument(0);
            unit.setId(10L);
            return 1;
        });

        orgUnitService.create(request);

        ArgumentCaptor<OrgUnit> captor = ArgumentCaptor.forClass(OrgUnit.class);
        verify(orgUnitMapper).updateById(captor.capture());
        OrgUnit updated = captor.getValue();
        assertEquals("/10/", updated.getPath());
        assertEquals(1, updated.getLevel());
    }
}
