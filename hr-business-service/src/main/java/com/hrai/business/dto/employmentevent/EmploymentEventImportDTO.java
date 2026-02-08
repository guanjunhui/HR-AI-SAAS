package com.hrai.business.dto.employmentevent;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 任职事件导入 DTO
 */
public class EmploymentEventImportDTO {

    @ExcelProperty("员工ID")
    private Long employeeId;

    @ExcelProperty("事件类型")
    private String eventType;

    @ExcelProperty("生效日期")
    private String eventDate;

    @ExcelProperty("原因")
    private String reason;

    @ExcelProperty("原组织ID")
    private Long fromOrgUnitId;

    @ExcelProperty("原职位ID")
    private Long fromPositionId;

    @ExcelProperty("原薪资等级")
    private String fromSalaryGrade;

    @ExcelProperty("新组织ID")
    private Long toOrgUnitId;

    @ExcelProperty("新职位ID")
    private Long toPositionId;

    @ExcelProperty("新薪资等级")
    private String toSalaryGrade;

    @ExcelProperty("备注")
    private String remark;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getFromOrgUnitId() { return fromOrgUnitId; }
    public void setFromOrgUnitId(Long fromOrgUnitId) { this.fromOrgUnitId = fromOrgUnitId; }

    public Long getFromPositionId() { return fromPositionId; }
    public void setFromPositionId(Long fromPositionId) { this.fromPositionId = fromPositionId; }

    public String getFromSalaryGrade() { return fromSalaryGrade; }
    public void setFromSalaryGrade(String fromSalaryGrade) { this.fromSalaryGrade = fromSalaryGrade; }

    public Long getToOrgUnitId() { return toOrgUnitId; }
    public void setToOrgUnitId(Long toOrgUnitId) { this.toOrgUnitId = toOrgUnitId; }

    public Long getToPositionId() { return toPositionId; }
    public void setToPositionId(Long toPositionId) { this.toPositionId = toPositionId; }

    public String getToSalaryGrade() { return toSalaryGrade; }
    public void setToSalaryGrade(String toSalaryGrade) { this.toSalaryGrade = toSalaryGrade; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
