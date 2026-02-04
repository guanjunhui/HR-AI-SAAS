package com.hrai.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建组织单元请求
 */
public class OrgUnitCreateRequest {

    private Long parentId;

    @NotBlank(message = "组织名称不能为空")
    private String name;

    @NotBlank(message = "组织编码不能为空")
    private String code;

    @NotBlank(message = "组织类型不能为空")
    private String type;

    private Long leaderId;

    private Integer sortOrder;

    @NotNull(message = "状态不能为空")
    private Integer status;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getLeaderId() { return leaderId; }
    public void setLeaderId(Long leaderId) { this.leaderId = leaderId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
