package com.hrai.business.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 假期类型实体
 */
@TableName("leave_types")
public class LeaveType {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tenantId;
    private String typeCode;
    private String typeName;
    private String unit;
    private Integer isPaid;
    private BigDecimal annualQuota;
    private BigDecimal minApplyDays;
    private BigDecimal maxApplyDays;
    private Integer needProof;
    private BigDecimal proofThresholdDays;
    private String description;
    private Integer status;

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

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Integer getIsPaid() { return isPaid; }
    public void setIsPaid(Integer isPaid) { this.isPaid = isPaid; }

    public BigDecimal getAnnualQuota() { return annualQuota; }
    public void setAnnualQuota(BigDecimal annualQuota) { this.annualQuota = annualQuota; }

    public BigDecimal getMinApplyDays() { return minApplyDays; }
    public void setMinApplyDays(BigDecimal minApplyDays) { this.minApplyDays = minApplyDays; }

    public BigDecimal getMaxApplyDays() { return maxApplyDays; }
    public void setMaxApplyDays(BigDecimal maxApplyDays) { this.maxApplyDays = maxApplyDays; }

    public Integer getNeedProof() { return needProof; }
    public void setNeedProof(Integer needProof) { this.needProof = needProof; }

    public BigDecimal getProofThresholdDays() { return proofThresholdDays; }
    public void setProofThresholdDays(BigDecimal proofThresholdDays) { this.proofThresholdDays = proofThresholdDays; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
