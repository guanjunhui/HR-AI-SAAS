package com.hrai.business.dto.risk;

/**
 * 离职风险分布项
 */
public class TurnoverRiskDistribution {

    private String level;
    private Integer count;

    public TurnoverRiskDistribution() {}

    public TurnoverRiskDistribution(String level, Integer count) {
        this.level = level;
        this.count = count;
    }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
