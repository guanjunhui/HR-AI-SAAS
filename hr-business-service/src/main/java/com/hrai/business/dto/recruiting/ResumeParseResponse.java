package com.hrai.business.dto.recruiting;

import java.util.List;

/**
 * 简历解析响应
 */
public class ResumeParseResponse {

    private Long candidateId;
    private String summary;
    private Integer matchScore;
    private List<ParsedResumeField> fields;
    private String rawText;

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Integer getMatchScore() { return matchScore; }
    public void setMatchScore(Integer matchScore) { this.matchScore = matchScore; }

    public List<ParsedResumeField> getFields() { return fields; }
    public void setFields(List<ParsedResumeField> fields) { this.fields = fields; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
