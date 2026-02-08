package com.hrai.business.dto.performance;

/**
 * 绩效校准请求
 */
public class PerformanceCalibrationRequest {

    private Integer calibratedScore;
    private String reason;

    public Integer getCalibratedScore() { return calibratedScore; }
    public void setCalibratedScore(Integer calibratedScore) { this.calibratedScore = calibratedScore; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
