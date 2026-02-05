package com.hrai.business.dto.position;

import jakarta.validation.constraints.NotBlank;

/**
 * 岗位创建请求
 */
public class PositionCreateRequest {

    @NotBlank(message = "岗位编码不能为空")
    private String positionCode;

    @NotBlank(message = "岗位名称不能为空")
    private String positionName;

    private Integer positionLevel;
    private String jobFamily;
    private Long orgUnitId;
    private String description;
    private String requirements;
    private Integer status = 1;

    public String getPositionCode() { return positionCode; }
    public void setPositionCode(String positionCode) { this.positionCode = positionCode; }

    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }

    public Integer getPositionLevel() { return positionLevel; }
    public void setPositionLevel(Integer positionLevel) { this.positionLevel = positionLevel; }

    public String getJobFamily() { return jobFamily; }
    public void setJobFamily(String jobFamily) { this.jobFamily = jobFamily; }

    public Long getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(Long orgUnitId) { this.orgUnitId = orgUnitId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
