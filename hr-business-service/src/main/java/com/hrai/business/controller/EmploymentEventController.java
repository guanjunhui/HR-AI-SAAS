package com.hrai.business.controller;

import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.employmentevent.EmploymentEventApproveRequest;
import com.hrai.business.dto.employmentevent.EmploymentEventCreateRequest;
import com.hrai.business.dto.employmentevent.EmploymentEventDetailResponse;
import com.hrai.business.dto.employmentevent.EmploymentEventQueryRequest;
import com.hrai.business.service.EmploymentEventService;
import com.hrai.common.dto.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 任职事件管理 Controller
 */
@RestController
@RequestMapping("/api/v1/hr/employment-events")
public class EmploymentEventController {

    private final EmploymentEventService eventService;

    public EmploymentEventController(EmploymentEventService eventService) {
        this.eventService = eventService;
    }

    /**
     * 分页查询任职事件列表
     */
    @GetMapping
    public Result<PageResponse<EmploymentEventDetailResponse>> list(EmploymentEventQueryRequest query) {
        return Result.success(eventService.list(query));
    }

    /**
     * 根据ID获取任职事件详情
     */
    @GetMapping("/{id}")
    public Result<EmploymentEventDetailResponse> getById(@PathVariable Long id) {
        return Result.success(eventService.getById(id));
    }

    /**
     * 创建任职事件（草稿状态）
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid EmploymentEventCreateRequest request) {
        return Result.success(eventService.create(request));
    }

    /**
     * 提交审批
     */
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        eventService.submit(id);
        return Result.success();
    }

    /**
     * 审批任职事件
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody EmploymentEventApproveRequest request) {
        eventService.approve(id, request);
        return Result.success();
    }

    /**
     * 取消任职事件
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        eventService.cancel(id);
        return Result.success();
    }

    /**
     * 删除任职事件（仅草稿状态可删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return Result.success();
    }
}
