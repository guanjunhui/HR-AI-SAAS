package com.hrai.business.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hrai.business.dto.employmentevent.EmploymentEventImportDTO;
import com.hrai.business.dto.employmentevent.EmploymentEventStatisticsDTO;
import com.hrai.business.entity.EmploymentEvent;
import com.hrai.business.mapper.EmployeeMapper;
import com.hrai.business.mapper.EmploymentEventMapper;
import com.hrai.business.mapper.PositionMapper;
import com.hrai.business.statemachine.EmploymentEventStateMachine;
import com.hrai.common.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmploymentEventServiceImplTest {

    @Mock
    private EmploymentEventMapper eventMapper;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private PositionMapper positionMapper;

    @Mock
    private EmploymentEventStateMachine stateMachine;

    @InjectMocks
    private EmploymentEventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        TenantContext.TenantInfo info = new TenantContext.TenantInfo();
        info.setTenantId("tenant_test");
        info.setUserId("1");
        TenantContext.setTenantInfo(info);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void importEvents_shouldParseAndInsert() {
        // Prepare Excel file
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<EmploymentEventImportDTO> data = new ArrayList<>();
        EmploymentEventImportDTO dto = new EmploymentEventImportDTO();
        dto.setEmployeeId(1L);
        dto.setEventType("PROMOTION");
        dto.setEventDate("2023-01-01");
        data.add(dto);

        EasyExcel.write(out, EmploymentEventImportDTO.class).sheet("Sheet1").doWrite(data);
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());

        // Execute
        eventService.importEvents(file);

        // Verify
        verify(eventMapper, times(1)).insert(any(EmploymentEvent.class));
    }

    @Test
    void getStatistics_shouldReturnAggregatedData() {
        // Mock DB response
        List<Map<String, Object>> typeStats = new ArrayList<>();
        Map<String, Object> typeMap = new HashMap<>();
        typeMap.put("key", "PROMOTION");
        typeMap.put("count", 5); // MyBatis returns Integer or Long depending on driver, let's assume Integer here or cast carefully in service
        typeStats.add(typeMap);

        List<Map<String, Object>> statusStats = new ArrayList<>();
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("key", "APPROVED");
        statusMap.put("count", 3);
        statusStats.add(statusMap);

        when(eventMapper.selectMaps(any(QueryWrapper.class)))
                .thenReturn(typeStats)
                .thenReturn(statusStats);

        // Execute
        EmploymentEventStatisticsDTO result = eventService.getStatistics();

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getByType().size());
        assertEquals("PROMOTION", result.getByType().get(0).getKey());
        assertEquals(5L, result.getByType().get(0).getCount());

        assertEquals(1, result.getByStatus().size());
        assertEquals("APPROVED", result.getByStatus().get(0).getKey());
        assertEquals(3L, result.getByStatus().get(0).getCount());
    }
}
