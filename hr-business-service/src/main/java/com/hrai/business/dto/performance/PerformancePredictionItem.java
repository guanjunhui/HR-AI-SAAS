package com.hrai.business.dto.performance;

import java.util.List;

/**
 * 绩效预测条目
 */
public class PerformancePredictionItem {

    private Long predictionId;
    private Long employeeId;
    private String employeeName;
    private String cycle;
    private Integer predictedScore;
    private Integer calibratedScore;
    private Integer confidence;
    private List<String> factors;
    private String updatedAt;

    public Long getPredictionId() { return predictionId; }
    public void setPredictionId(Long predictionId) { this.predictionId = predictionId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getCycle() { return cycle; }
    public void setCycle(String cycle) { this.cycle = cycle; }

    public Integer getPredictedScore() { return predictedScore; }
    public void setPredictedScore(Integer predictedScore) { this.predictedScore = predictedScore; }

    public Integer getCalibratedScore() { return calibratedScore; }
    public void setCalibratedScore(Integer calibratedScore) { this.calibratedScore = calibratedScore; }

    public Integer getConfidence() { return confidence; }
    public void setConfidence(Integer confidence) { this.confidence = confidence; }

    public List<String> getFactors() { return factors; }
    public void setFactors(List<String> factors) { this.factors = factors; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
