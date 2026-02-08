package com.hrai.business.controller;

import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.performance.PerformanceCalibrationRequest;
import com.hrai.business.dto.performance.PerformancePredictionItem;
import com.hrai.business.dto.performance.PerformancePredictionQuery;
import com.hrai.common.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 绩效预测 Controller
 */
@RestController
@RequestMapping("/api/v1/performance")
public class PerformanceController {

    @GetMapping("/predictions")
    public Result<PageResponse<PerformancePredictionItem>> listPredictions(PerformancePredictionQuery query) {
        PerformancePredictionItem item = new PerformancePredictionItem();
        item.setPredictionId(1001L);
        item.setEmployeeId(2001L);
        item.setEmployeeName(query.getKeyword() != null && !query.getKeyword().isBlank() ? query.getKeyword() : "示例员工");
        item.setCycle(query.getCycle() != null && !query.getCycle().isBlank() ? query.getCycle() : "2026-Q1");
        item.setPredictedScore(86);
        item.setCalibratedScore(84);
        item.setConfidence(79);
        item.setFactors(Arrays.asList("目标达成", "协作效率", "学习成长"));
        item.setUpdatedAt(LocalDateTime.now().toString());
        List<PerformancePredictionItem> records = Arrays.asList(item);
        int pageNo = query.getPageNo() == null ? 1 : query.getPageNo();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        return Result.success(PageResponse.of(records, 1L, pageNo, pageSize));
    }

    @PostMapping("/predictions/{predictionId}/calibrate")
    public Result<Void> calibrate(@PathVariable("predictionId") Long predictionId,
                                  @RequestBody PerformanceCalibrationRequest request) {
        return Result.success();
    }
}
