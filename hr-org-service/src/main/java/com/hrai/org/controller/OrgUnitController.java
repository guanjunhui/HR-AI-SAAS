package com.hrai.org.controller;

import com.hrai.common.dto.Result;
import com.hrai.org.dto.OrgUnitCreateRequest;
import com.hrai.org.dto.OrgUnitDetailResponse;
import com.hrai.org.dto.OrgUnitTreeNode;
import com.hrai.org.dto.OrgUnitUpdateRequest;
import com.hrai.org.service.OrgUnitService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织管理接口
 */
@RestController
@RequestMapping("/api/v1/org/units")
public class OrgUnitController {

    private final OrgUnitService orgUnitService;

    public OrgUnitController(OrgUnitService orgUnitService) {
        this.orgUnitService = orgUnitService;
    }

    @GetMapping("/tree")
    public Result<List<OrgUnitTreeNode>> getTree() {
        return Result.success(orgUnitService.getTree());
    }

    @GetMapping("/{id}")
    public Result<OrgUnitDetailResponse> getById(@PathVariable("id") Long id) {
        return Result.success(orgUnitService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid OrgUnitCreateRequest request) {
        return Result.success(orgUnitService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody @Valid OrgUnitUpdateRequest request) {
        orgUnitService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        orgUnitService.delete(id);
        return Result.success();
    }
}
