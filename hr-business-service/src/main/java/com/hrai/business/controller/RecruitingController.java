package com.hrai.business.controller;

import com.hrai.business.dto.recruiting.CandidatePatchRequest;
import com.hrai.business.dto.recruiting.ParsedResumeField;
import com.hrai.business.dto.recruiting.ResumeParseRequest;
import com.hrai.business.dto.recruiting.ResumeParseResponse;
import com.hrai.common.dto.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * 招聘管理 Controller
 */
@RestController
@RequestMapping("/api/v1/recruiting/candidates")
public class RecruitingController {

    @PostMapping("/{candidateId}/parse-resume")
    public Result<ResumeParseResponse> parseResume(@PathVariable("candidateId") Long candidateId,
                                                   @RequestBody ResumeParseRequest request) {
        ResumeParseResponse response = new ResumeParseResponse();
        response.setCandidateId(candidateId);
        response.setSummary("简历解析完成，可进行字段回填。\n");
        response.setMatchScore(82);
        response.setFields(Arrays.asList(
                new ParsedResumeField("fullName", "姓名", "张三", 95),
                new ParsedResumeField("phone", "手机号", "13800000000", 88),
                new ParsedResumeField("email", "邮箱", "zhangsan@example.com", 86)
        ));
        response.setRawText(request != null ? request.getContent() : null);
        return Result.success(response);
    }

    @PutMapping("/{candidateId}")
    public Result<Void> patchCandidate(@PathVariable("candidateId") Long candidateId,
                                       @RequestBody CandidatePatchRequest request) {
        return Result.success();
    }
}
