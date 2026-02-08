package com.hrai.business.dto.recruiting;

import java.util.Map;

/**
 * 候选人字段回填请求
 */
public class CandidatePatchRequest {

    private Map<String, String> fields;

    public Map<String, String> getFields() { return fields; }
    public void setFields(Map<String, String> fields) { this.fields = fields; }
}
