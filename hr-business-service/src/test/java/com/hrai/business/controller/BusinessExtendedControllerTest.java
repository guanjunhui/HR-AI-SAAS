package com.hrai.business.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BusinessExtendedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void onboardingAutofill_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/onboarding/forms/autofill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"candidateId\":1,\"resumeText\":\"test resume\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.confidenceScore").exists());
    }

    @Test
    void recruitingParse_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/recruiting/candidates/1/parse-resume")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceType\":\"text\",\"content\":\"resume\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.candidateId").value(1));
    }

    @Test
    void performanceList_shouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/performance/predictions")
                        .param("pageNo", "1")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void riskDashboard_shouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/ai/risk/turnover/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.highRiskList").isArray());
    }

    @Test
    void riskFeedback_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/ai/risk/turnover/3001/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mark\":\"followed\",\"note\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void performanceCalibrate_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/performance/predictions/1001/calibrate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calibratedScore\":90,\"reason\":\"manual\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void recruitingPatch_shouldReturnSuccess() throws Exception {
        mockMvc.perform(put("/api/v1/recruiting/candidates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fields\":{\"fullName\":\"张三\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
