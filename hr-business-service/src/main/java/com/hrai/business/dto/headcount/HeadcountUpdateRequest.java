package com.hrai.business.dto.headcount;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 编制更新请求 DTO
 */
public class HeadcountUpdateRequest {

    @NotNull(message = "编制数不能为空")
    @Min(value = 1, message = "编制数至少为1")
    private Integer budgetCount;

    private Integer year;

    private Integer quarter;

    private Integer status;

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
