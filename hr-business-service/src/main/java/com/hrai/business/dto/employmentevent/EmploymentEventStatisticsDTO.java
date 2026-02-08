package com.hrai.business.dto.employmentevent;

import java.util.List;

/**
 * 任职事件统计 DTO
 */
public class EmploymentEventStatisticsDTO {

    private List<StatItem> byType;
    private List<StatItem> byStatus;

    public List<StatItem> getByType() { return byType; }
    public void setByType(List<StatItem> byType) { this.byType = byType; }

    public List<StatItem> getByStatus() { return byStatus; }
    public void setByStatus(List<StatItem> byStatus) { this.byStatus = byStatus; }

    public EmploymentEventStatisticsDTO() {}
    public EmploymentEventStatisticsDTO(List<StatItem> byType, List<StatItem> byStatus) {
        this.byType = byType;
        this.byStatus = byStatus;
    }

    public static class StatItem {
        private String key;
        private Long count;

        public StatItem() {}
        public StatItem(String key, Long count) {
            this.key = key;
            this.count = count;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}
