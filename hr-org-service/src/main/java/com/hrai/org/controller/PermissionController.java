package com.hrai.org.controller;

import com.hrai.common.dto.Result;
import com.hrai.org.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Permission endpoints.
 */
@RestController
@RequestMapping("/api/v1/org/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public Result<List<String>> listPermissions() {
        return Result.success(permissionService.listPermissions());
    }

    @GetMapping("/check")
    public Result<Boolean> checkPermission(@RequestParam("userId") Long userId,
                                           @RequestParam("permission") String permission) {
        return Result.success(permissionService.checkPermission(userId, permission));
    }
}
