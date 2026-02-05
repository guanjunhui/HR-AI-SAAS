package com.hrai.business.dto.employmentevent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 任职事件创建请求
 */
public class EmploymentEventCreateRequest {

    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @NotBlank(message = "事件类型不能为空")
    private String eventType;

    @NotBlank(message = "生效日期不能为空")
    private String eventDate;

    private String reason;
    private Long fromOrgUnitId;
    private Long fromPositionId;
    private String fromSalaryGrade;
    private Long toOrgUnitId;
    private Long toPositionId;
    private String toSalaryGrade;
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
