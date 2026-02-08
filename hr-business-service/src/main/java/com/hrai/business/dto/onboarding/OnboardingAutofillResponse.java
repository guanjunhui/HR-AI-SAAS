package com.hrai.business.dto.onboarding;

import java.util.List;

/**
 * 入职表单自动补全响应
 */
public class OnboardingAutofillResponse {

    private String fullName;
    private String gender;
    private String phone;
    private String email;
    private String idCard;
    private String expectedOnboardDate;
    private Long orgUnitId;
    private Long positionId;
    private String workLocation;
    private Integer confidenceScore;
    private List<String> unresolvedFields;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public String getExpectedOnboardDate() { return expectedOnboardDate; }
    public void setExpectedOnboardDate(String expectedOnboardDate) { this.expectedOnboardDate = expectedOnboardDate; }

    public Long getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(Long orgUnitId) { this.orgUnitId = orgUnitId; }

    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }

    public String getWorkLocation() { return workLocation; }
    public void setWorkLocation(String workLocation) { this.workLocation = workLocation; }

    public Integer getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Integer confidenceScore) { this.confidenceScore = confidenceScore; }

    public List<String> getUnresolvedFields() { return unresolvedFields; }
    public void setUnresolvedFields(List<String> unresolvedFields) { this.unresolvedFields = unresolvedFields; }
}
