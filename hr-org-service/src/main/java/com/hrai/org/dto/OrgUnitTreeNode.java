package com.hrai.org.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织树节点
 */
public class OrgUnitTreeNode {

    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private String type;
    private Long leaderId;
    private Integer sortOrder;
    private Integer status;
    private Integer level;
    private String path;
    private List<OrgUnitTreeNode> children = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<OrgUnitTreeNode> getChildren() { return children; }
    public void setChildren(List<OrgUnitTreeNode> children) { this.children = children; }
}
