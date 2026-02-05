package com.hrai.business.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 员工创建请求
 */
public class EmployeeCreateRequest {

    @NotBlank(message = "工号不能为空")
    private String employeeCode;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private Integer gender;
    private String phone;
    private String email;
    private String idCard;

    @NotNull(message = "部门不能为空")
    private Long orgUnitId;

    private Long positionId;
    private Long directManagerId;
    private String entryDate;
    private String probationEndDate;
    private String workLocation;

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public Long getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(Long orgUnitId) { this.orgUnitId = orgUnitId; }

    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }

    public Long getDirectManagerId() { return directManagerId; }
    public void setDirectManagerId(Long directManagerId) { this.directManagerId = directManagerId; }

    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }

    public String getProbationEndDate() { return probationEndDate; }
    public void setProbationEndDate(String probationEndDate) { this.probationEndDate = probationEndDate; }

    public String getWorkLocation() { return workLocation; }
    public void setWorkLocation(String workLocation) { this.workLocation = workLocation; }
}
