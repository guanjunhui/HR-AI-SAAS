package com.hrai.business.service.impl;

import com.hrai.business.dto.headcount.HeadcountCreateRequest;
import com.hrai.business.dto.headcount.HeadcountUpdateRequest;
import com.hrai.business.entity.Headcount;
import com.hrai.business.mapper.HeadcountMapper;
import com.hrai.business.mapper.PositionMapper;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeadcountServiceImplTest {

    @Mock
    private HeadcountMapper headcountMapper;

    @Mock
    private PositionMapper positionMapper;

    @InjectMocks
    private HeadcountServiceImpl headcountService;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldUseDefaultTenant_whenTenantContextMissing() {
        TenantContext.clear();

        HeadcountCreateRequest request = new HeadcountCreateRequest();
        request.setOrgUnitId(10L);
        request.setPositionId(20L);
        request.setBudgetCount(5);
        request.setYear(2026);
        request.setQuarter(1);
        request.setStatus(1);

        when(headcountMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            Headcount headcount = invocation.getArgument(0);
            headcount.setId(100L);
            return 1;
        }).when(headcountMapper).insert(any(Headcount.class));

        Long id = headcountService.create(request);

        assertEquals(100L, id);
        ArgumentCaptor<Headcount> captor = ArgumentCaptor.forClass(Headcount.class);
        verify(headcountMapper).insert(captor.capture());
        Headcount saved = captor.getValue();
        assertEquals(TenantConstants.DEFAULT_TENANT_ID, saved.getTenantId());
        assertEquals(0, saved.getActualCount());
    }

    @Test
    void getById_shouldThrow404_whenTenantMismatch() {
        TenantContext.set("tenant_a", "1", "session-1");
        Headcount headcount = new Headcount();
        headcount.setId(1L);
        headcount.setTenantId("tenant_b");
        when(headcountMapper.selectById(1L)).thenReturn(headcount);

        BizException ex = assertThrows(BizException.class, () -> headcountService.getById(1L));

        assertEquals(404, ex.getCode());
        assertEquals("编制不存在", ex.getMessage());
    }

    @Test
    void update_shouldThrow404_whenTenantMismatch() {
        TenantContext.set("tenant_a", "1", "session-1");
        Headcount headcount = new Headcount();
        headcount.setId(2L);
        headcount.setTenantId("tenant_b");
        when(headcountMapper.selectById(2L)).thenReturn(headcount);

        HeadcountUpdateRequest request = new HeadcountUpdateRequest();
        request.setBudgetCount(8);
        request.setStatus(1);

        BizException ex = assertThrows(BizException.class, () -> headcountService.update(2L, request));

        assertEquals(404, ex.getCode());
        verify(headcountMapper, never()).updateById(any(Headcount.class));
    }

    @Test
    void delete_shouldThrow404_whenTenantMismatch() {
        TenantContext.set("tenant_a", "1", "session-1");
        Headcount headcount = new Headcount();
        headcount.setId(3L);
        headcount.setTenantId("tenant_b");
        when(headcountMapper.selectById(3L)).thenReturn(headcount);

        BizException ex = assertThrows(BizException.class, () -> headcountService.delete(3L));

        assertEquals(404, ex.getCode());
        verify(headcountMapper, never()).delete(any());
    }
}
