package com.hrai.business.dto.headcount;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 编制创建请求 DTO
 */
public class HeadcountCreateRequest {

    @NotNull(message = "组织ID不能为空")
    private Long orgUnitId;

    @NotNull(message = "岗位ID不能为空")
    private Long positionId;

    @NotNull(message = "编制数不能为空")
    @Min(value = 1, message = "编制数至少为1")
    private Integer budgetCount;

    private Integer year;

    private Integer quarter;

    private Integer status = 1;

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(Long orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public Integer getBudgetCount() {
        return budgetCount;
    }

    public void setBudgetCount(Integer budgetCount) {
        this.budgetCount = budgetCount;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
