package com.hrai.business.dto.risk;

/**
 * 离职风险反馈请求
 */
public class TurnoverRiskFeedbackRequest {

    private String mark;
    private String note;

    public String getMark() { return mark; }
    public void setMark(String mark) { this.mark = mark; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
