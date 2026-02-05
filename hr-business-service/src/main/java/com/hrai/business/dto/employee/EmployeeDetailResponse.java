package com.hrai.business.dto.employee;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工详情响应
 */
public class EmployeeDetailResponse {

    private Long id;
    private String employeeCode;
    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private Long orgUnitId;
    private String orgUnitName;
    private Long positionId;
    private String positionName;
    private Long directManagerId;
    private String directManagerName;
    private LocalDate entryDate;
    private LocalDate probationEndDate;
    private LocalDate regularDate;
    private LocalDate resignationDate;
    private String employeeStatus;
    private String employeeStatusDesc;
    private String workLocation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Long getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(Long orgUnitId) { this.orgUnitId = orgUnitId; }

    public String getOrgUnitName() { return orgUnitName; }
    public void setOrgUnitName(String orgUnitName) { this.orgUnitName = orgUnitName; }

    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }

    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }

    public Long getDirectManagerId() { return directManagerId; }
    public void setDirectManagerId(Long directManagerId) { this.directManagerId = directManagerId; }

    public String getDirectManagerName() { return directManagerName; }
    public void setDirectManagerName(String directManagerName) { this.directManagerName = directManagerName; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public LocalDate getProbationEndDate() { return probationEndDate; }
    public void setProbationEndDate(LocalDate probationEndDate) { this.probationEndDate = probationEndDate; }

    public LocalDate getRegularDate() { return regularDate; }
    public void setRegularDate(LocalDate regularDate) { this.regularDate = regularDate; }

    public LocalDate getResignationDate() { return resignationDate; }
    public void setResignationDate(LocalDate resignationDate) { this.resignationDate = resignationDate; }

    public String getEmployeeStatus() { return employeeStatus; }
    public void setEmployeeStatus(String employeeStatus) { this.employeeStatus = employeeStatus; }

    public String getEmployeeStatusDesc() { return employeeStatusDesc; }
    public void setEmployeeStatusDesc(String employeeStatusDesc) { this.employeeStatusDesc = employeeStatusDesc; }

    public String getWorkLocation() { return workLocation; }
    public void setWorkLocation(String workLocation) { this.workLocation = workLocation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
