package com.hrai.org.controller;

import com.hrai.common.dto.Result;
import com.hrai.org.dto.RoleCreateRequest;
import com.hrai.org.dto.RoleDetailResponse;
import com.hrai.org.dto.RolePermissionsUpdateRequest;
import com.hrai.org.dto.RoleUpdateRequest;
import com.hrai.org.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口
 */
@RestController
@RequestMapping("/api/v1/org/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public Result<List<RoleDetailResponse>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                 @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(roleService.list(keyword, status));
    }

    @GetMapping("/{id}")
    public Result<RoleDetailResponse> getById(@PathVariable("id") Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid RoleCreateRequest request) {
        return Result.success(roleService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody @Valid RoleUpdateRequest request) {
        roleService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> updatePermissions(@PathVariable("id") Long id,
                                          @RequestBody @Valid RolePermissionsUpdateRequest request) {
        roleService.updatePermissions(id, request);
        return Result.success();
    }
}
