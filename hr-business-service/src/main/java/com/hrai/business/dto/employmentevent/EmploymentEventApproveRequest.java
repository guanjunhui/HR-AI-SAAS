package com.hrai.business.dto.employmentevent;

/**
 * 任职事件审批请求
 */
public class EmploymentEventApproveRequest {

    private Boolean approved;
    private String rejectReason;

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
