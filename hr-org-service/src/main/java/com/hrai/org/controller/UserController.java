package com.hrai.org.controller;

import com.hrai.common.dto.Result;
import com.hrai.org.dto.PageResponse;
import com.hrai.org.dto.UserCreateRequest;
import com.hrai.org.dto.UserDetailResponse;
import com.hrai.org.dto.UserPasswordUpdateRequest;
import com.hrai.org.dto.UserQueryRequest;
import com.hrai.org.dto.UserRoleUpdateRequest;
import com.hrai.org.dto.UserStatusUpdateRequest;
import com.hrai.org.dto.UserUpdateRequest;
import com.hrai.org.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口
 */
@RestController
@RequestMapping("/api/v1/org/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<PageResponse<UserDetailResponse>> list(@ModelAttribute UserQueryRequest query) {
        return Result.success(userService.list(query));
    }

    @GetMapping("/{id}")
    public Result<UserDetailResponse> getById(@PathVariable("id") Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid UserCreateRequest request) {
        return Result.success(userService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody @Valid UserUpdateRequest request) {
        userService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id,
                                     @RequestBody @Valid UserStatusUpdateRequest request) {
        userService.updateStatus(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(@PathVariable("id") Long id,
                                       @RequestBody @Valid UserPasswordUpdateRequest request) {
        userService.updatePassword(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    public Result<Void> updateRole(@PathVariable("id") Long id,
                                   @RequestBody @Valid UserRoleUpdateRequest request) {
        userService.updateRole(id, request);
        return Result.success();
    }
}
