package com.hrai.business.dto.risk;

import java.util.List;

/**
 * 离职风险条目
 */
public class TurnoverRiskItem {

    private Long riskId;
    private Long employeeId;
    private String employeeName;
    private String orgUnitName;
    private String level;
    private Integer score;
    private List<String> reasons;
    private String trend;
    private String updatedAt;

    public Long getRiskId() { return riskId; }
    public void setRiskId(Long riskId) { this.riskId = riskId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getOrgUnitName() { return orgUnitName; }
    public void setOrgUnitName(String orgUnitName) { this.orgUnitName = orgUnitName; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
