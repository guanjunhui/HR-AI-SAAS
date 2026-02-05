package com.hrai.business.dto.employmentevent;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任职事件详情响应
 */
public class EmploymentEventDetailResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String eventType;
    private String eventTypeDesc;
    private LocalDate eventDate;
    private String reason;
    private Long fromOrgUnitId;
    private String fromOrgUnitName;
    private Long fromPositionId;
    private String fromPositionName;
    private String fromSalaryGrade;
    private Long toOrgUnitId;
    private String toOrgUnitName;
    private Long toPositionId;
    private String toPositionName;
    private String toSalaryGrade;
    private String status;
    private String statusDesc;
    private Long applicantId;
    private String applicantName;
    private Long approverId;
    private String approverName;
    private LocalDateTime approvedAt;
    private String rejectReason;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventTypeDesc() { return eventTypeDesc; }
    public void setEventTypeDesc(String eventTypeDesc) { this.eventTypeDesc = eventTypeDesc; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getFromOrgUnitId() { return fromOrgUnitId; }
    public void setFromOrgUnitId(Long fromOrgUnitId) { this.fromOrgUnitId = fromOrgUnitId; }

    public String getFromOrgUnitName() { return fromOrgUnitName; }
    public void setFromOrgUnitName(String fromOrgUnitName) { this.fromOrgUnitName = fromOrgUnitName; }

    public Long getFromPositionId() { return fromPositionId; }
    public void setFromPositionId(Long fromPositionId) { this.fromPositionId = fromPositionId; }

    public String getFromPositionName() { return fromPositionName; }
    public void setFromPositionName(String fromPositionName) { this.fromPositionName = fromPositionName; }

    public String getFromSalaryGrade() { return fromSalaryGrade; }
    public void setFromSalaryGrade(String fromSalaryGrade) { this.fromSalaryGrade = fromSalaryGrade; }

    public Long getToOrgUnitId() { return toOrgUnitId; }
    public void setToOrgUnitId(Long toOrgUnitId) { this.toOrgUnitId = toOrgUnitId; }

    public String getToOrgUnitName() { return toOrgUnitName; }
    public void setToOrgUnitName(String toOrgUnitName) { this.toOrgUnitName = toOrgUnitName; }

    public Long getToPositionId() { return toPositionId; }
    public void setToPositionId(Long toPositionId) { this.toPositionId = toPositionId; }

    public String getToPositionName() { return toPositionName; }
    public void setToPositionName(String toPositionName) { this.toPositionName = toPositionName; }

    public String getToSalaryGrade() { return toSalaryGrade; }
    public void setToSalaryGrade(String toSalaryGrade) { this.toSalaryGrade = toSalaryGrade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusDesc() { return statusDesc; }
    public void setStatusDesc(String statusDesc) { this.statusDesc = statusDesc; }

    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
