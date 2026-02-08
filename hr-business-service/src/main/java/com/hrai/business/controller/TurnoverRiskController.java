package com.hrai.business.controller;

import com.hrai.business.dto.risk.TurnoverRiskDashboard;
import com.hrai.business.dto.risk.TurnoverRiskDistribution;
import com.hrai.business.dto.risk.TurnoverRiskFeedbackRequest;
import com.hrai.business.dto.risk.TurnoverRiskItem;
import com.hrai.common.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 离职风险 Controller
 */
@RestController
@RequestMapping("/api/v1/ai/risk/turnover")
public class TurnoverRiskController {

    @GetMapping("/dashboard")
    public Result<TurnoverRiskDashboard> dashboard() {
        TurnoverRiskItem item = new TurnoverRiskItem();
        item.setRiskId(3001L);
        item.setEmployeeId(2001L);
        item.setEmployeeName("示例员工");
        item.setOrgUnitName("技术部");
        item.setLevel("high");
        item.setScore(91);
        item.setReasons(Arrays.asList("近期加班高", "绩效波动", "满意度下降"));
        item.setTrend("up");
        item.setUpdatedAt(LocalDateTime.now().toString());

        TurnoverRiskDashboard dashboard = new TurnoverRiskDashboard();
        dashboard.setGeneratedAt(LocalDateTime.now().toString());
        dashboard.setTotalEmployees(1);
        dashboard.setDistribution(Arrays.asList(
                new TurnoverRiskDistribution("high", 1),
                new TurnoverRiskDistribution("medium", 0),
                new TurnoverRiskDistribution("low", 0)
        ));
        dashboard.setHighRiskList(Arrays.asList(item));
        return Result.success(dashboard);
    }

    @PostMapping("/{riskId}/feedback")
    public Result<Void> feedback(@PathVariable("riskId") Long riskId,
                                 @RequestBody TurnoverRiskFeedbackRequest request) {
        return Result.success();
    }
}
