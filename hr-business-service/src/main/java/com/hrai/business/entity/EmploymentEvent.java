package com.hrai.business.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任职事件实体
 */
@TableName("employment_events")
public class EmploymentEvent {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tenantId;
    private Long employeeId;
    private String eventType;
    private LocalDate eventDate;
    private String reason;
    private Long fromOrgUnitId;
    private Long fromPositionId;
    private String fromSalaryGrade;
    private Long toOrgUnitId;
    private Long toPositionId;
    private String toSalaryGrade;
    private String status;
    private Long applicantId;
    private Long approverId;
    private LocalDateTime approvedAt;
    private String rejectReason;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

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

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
