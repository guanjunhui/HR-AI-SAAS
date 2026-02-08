package com.hrai.business.controller;

import com.hrai.business.dto.onboarding.OnboardingAutofillRequest;
import com.hrai.business.dto.onboarding.OnboardingAutofillResponse;
import com.hrai.business.dto.onboarding.OnboardingDraftRequest;
import com.hrai.common.dto.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * 入职管理 Controller
 */
@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    @PostMapping("/forms/autofill")
    public Result<OnboardingAutofillResponse> autofill(@RequestBody(required = false) OnboardingAutofillRequest request) {
        OnboardingAutofillResponse response = new OnboardingAutofillResponse();
        response.setFullName("候选人" + (request != null && request.getCandidateId() != null ? request.getCandidateId() : ""));
        response.setGender("未知");
        response.setPhone("13800000000");
        response.setEmail("candidate@example.com");
        response.setExpectedOnboardDate(LocalDate.now().plusDays(7).toString());
        response.setWorkLocation("上海");
        response.setConfidenceScore(76);
        response.setUnresolvedFields(Arrays.asList("idCard", "orgUnitId", "positionId"));
        return Result.success(response);
    }

    @PostMapping
    public Result<Long> createDraft(@RequestBody OnboardingDraftRequest request) {
        long id = System.currentTimeMillis();
        return Result.success(id);
    }
}
