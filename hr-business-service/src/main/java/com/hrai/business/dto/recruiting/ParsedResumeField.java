package com.hrai.business.dto.recruiting;

/**
 * 简历字段解析项
 */
public class ParsedResumeField {

    private String key;
    private String label;
    private String value;
    private Integer confidence;

    public ParsedResumeField() {}

    public ParsedResumeField(String key, String label, String value, Integer confidence) {
        this.key = key;
        this.label = label;
        this.value = value;
        this.confidence = confidence;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Integer getConfidence() { return confidence; }
    public void setConfidence(Integer confidence) { this.confidence = confidence; }
}
