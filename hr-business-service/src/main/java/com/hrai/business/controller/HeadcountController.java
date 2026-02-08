package com.hrai.business.controller;

import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.headcount.HeadcountCreateRequest;
import com.hrai.business.dto.headcount.HeadcountDetailResponse;
import com.hrai.business.dto.headcount.HeadcountQueryRequest;
import com.hrai.business.dto.headcount.HeadcountUpdateRequest;
import com.hrai.business.service.HeadcountService;
import com.hrai.common.dto.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 编制管理 Controller
 */
@RestController
@RequestMapping("/api/v1/hr/headcounts")
public class HeadcountController {

    private final HeadcountService headcountService;

    public HeadcountController(HeadcountService headcountService) {
        this.headcountService = headcountService;
    }

    /**
     * 分页查询编制列表
     */
    @GetMapping
    public Result<PageResponse<HeadcountDetailResponse>> list(HeadcountQueryRequest query) {
        return Result.success(headcountService.list(query));
    }

    /**
     * 根据ID获取编制详情
     */
    @GetMapping("/{id}")
    public Result<HeadcountDetailResponse> getById(@PathVariable("id") Long id) {
        return Result.success(headcountService.getById(id));
    }

    /**
     * 创建编制
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid HeadcountCreateRequest request) {
        return Result.success(headcountService.create(request));
    }

    /**
     * 更新编制
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody @Valid HeadcountUpdateRequest request) {
        headcountService.update(id, request);
        return Result.success();
    }

    /**
     * 删除编制
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        headcountService.delete(id);
        return Result.success();
    }
}
