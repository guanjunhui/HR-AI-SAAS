package com.hrai.business.controller;

import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.position.PositionCreateRequest;
import com.hrai.business.dto.position.PositionDetailResponse;
import com.hrai.business.dto.position.PositionQueryRequest;
import com.hrai.business.dto.position.PositionUpdateRequest;
import com.hrai.business.service.PositionService;
import com.hrai.common.dto.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位管理 Controller
 */
@RestController
@RequestMapping("/api/v1/hr/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    /**
     * 分页查询岗位列表
     */
    @GetMapping
    public Result<PageResponse<PositionDetailResponse>> list(PositionQueryRequest query) {
        return Result.success(positionService.list(query));
    }

    /**
     * 查询所有启用的岗位（下拉框用）
     */
    @GetMapping("/enabled")
    public Result<List<PositionDetailResponse>> listEnabled() {
        return Result.success(positionService.listEnabled());
    }

    /**
     * 根据ID获取岗位详情
     */
    @GetMapping("/{id}")
    public Result<PositionDetailResponse> getById(@PathVariable Long id) {
        return Result.success(positionService.getById(id));
    }

    /**
     * 创建岗位
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid PositionCreateRequest request) {
        return Result.success(positionService.create(request));
    }

    /**
     * 更新岗位
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid PositionUpdateRequest request) {
        positionService.update(id, request);
        return Result.success();
    }

    /**
     * 删除岗位
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return Result.success();
    }
}
