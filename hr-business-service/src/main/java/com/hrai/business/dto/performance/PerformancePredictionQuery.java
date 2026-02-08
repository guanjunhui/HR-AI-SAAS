package com.hrai.business.dto.performance;

/**
 * 绩效预测查询参数
 */
public class PerformancePredictionQuery {

    private Integer pageNo = 1;
    private Integer pageSize = 20;
    private Long orgUnitId;
    private String keyword;
    private String cycle;

    public Integer getPageNo() { return pageNo; }
    public void setPageNo(Integer pageNo) { this.pageNo = pageNo; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    public Long getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(Long orgUnitId) { this.orgUnitId = orgUnitId; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getCycle() { return cycle; }
    public void setCycle(String cycle) { this.cycle = cycle; }
}
