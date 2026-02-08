package com.hrai.business.dto.risk;

import java.util.List;

/**
 * 离职风险看板
 */
public class TurnoverRiskDashboard {

    private String generatedAt;
    private Integer totalEmployees;
    private List<TurnoverRiskDistribution> distribution;
    private List<TurnoverRiskItem> highRiskList;

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public Integer getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(Integer totalEmployees) { this.totalEmployees = totalEmployees; }

    public List<TurnoverRiskDistribution> getDistribution() { return distribution; }
    public void setDistribution(List<TurnoverRiskDistribution> distribution) { this.distribution = distribution; }

    public List<TurnoverRiskItem> getHighRiskList() { return highRiskList; }
    public void setHighRiskList(List<TurnoverRiskItem> highRiskList) { this.highRiskList = highRiskList; }
}
