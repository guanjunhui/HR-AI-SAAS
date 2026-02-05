package com.hrai.business.dto.employmentevent;

/**
 * 任职事件查询请求
 */
public class EmploymentEventQueryRequest {

    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private Long employeeId;
    private String eventType;
    private String status;

    public Integer getPageNo() { return pageNo; }
    public void setPageNo(Integer pageNo) { this.pageNo = pageNo; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
