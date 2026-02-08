package com.hrai.business.dto.onboarding;

import java.util.List;
import java.util.Map;

/**
 * 入职表单自动补全请求
 */
public class OnboardingAutofillRequest {

    private Long candidateId;
    private String resumeText;
    private List<String> attachments;
    private Map<String, String> manualInputs;

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }

    public Map<String, String> getManualInputs() { return manualInputs; }
    public void setManualInputs(Map<String, String> manualInputs) { this.manualInputs = manualInputs; }
}
