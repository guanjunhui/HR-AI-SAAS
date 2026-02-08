package com.hrai.business.dto.recruiting;

/**
 * 简历解析请求
 */
public class ResumeParseRequest {

    private String sourceType;
    private String content;

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
