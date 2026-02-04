package com.hrai.org.dto;

/**
 * 用户列表查询
 */
public class UserQueryRequest {

    private long pageNo = 1;
    private long pageSize = 20;
    private String keyword;
    private Long orgUnitId;
    private Long roleId;
    private Integer status;

    public long getPageNo() { return pageNo; }
    public void setPageNo(long pageNo) { this.pageNo = pageNo; }

    public long getPageSize() { return pageSize; }
    public void setPageSize(long pageSize) { this.pageSize = pageSize; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Long getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(Long orgUnitId) { this.orgUnitId = orgUnitId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
