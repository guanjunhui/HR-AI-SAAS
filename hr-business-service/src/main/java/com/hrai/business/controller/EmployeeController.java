package com.hrai.business.controller;

import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.employee.EmployeeCreateRequest;
import com.hrai.business.dto.employee.EmployeeDetailResponse;
import com.hrai.business.dto.employee.EmployeeQueryRequest;
import com.hrai.business.dto.employee.EmployeeUpdateRequest;
import com.hrai.business.service.EmployeeService;
import com.hrai.common.dto.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理 Controller
 */
@RestController
@RequestMapping("/api/v1/hr/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * 分页查询员工列表
     */
    @GetMapping
    public Result<PageResponse<EmployeeDetailResponse>> list(EmployeeQueryRequest query) {
        return Result.success(employeeService.list(query));
    }

    /**
     * 根据ID获取员工详情
     */
    @GetMapping("/{id}")
    public Result<EmployeeDetailResponse> getById(@PathVariable("id") Long id) {
        return Result.success(employeeService.getById(id));
    }

    /**
     * 创建员工
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid EmployeeCreateRequest request) {
        return Result.success(employeeService.create(request));
    }

    /**
     * 更新员工信息
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody @Valid EmployeeUpdateRequest request) {
        employeeService.update(id, request);
        return Result.success();
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        employeeService.delete(id);
        return Result.success();
    }
}
