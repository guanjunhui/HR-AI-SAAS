package com.hrai.business.service;

import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.employee.EmployeeCreateRequest;
import com.hrai.business.dto.employee.EmployeeDetailResponse;
import com.hrai.business.dto.employee.EmployeeQueryRequest;
import com.hrai.business.dto.employee.EmployeeUpdateRequest;

/**
 * 员工服务接口
 */
public interface EmployeeService {

    /**
     * 分页查询员工列表
     */
    PageResponse<EmployeeDetailResponse> list(EmployeeQueryRequest query);

    /**
     * 根据ID获取员工详情
     */
    EmployeeDetailResponse getById(Long id);

    /**
     * 创建员工
     */
    Long create(EmployeeCreateRequest request);

    /**
     * 更新员工信息
     */
    void update(Long id, EmployeeUpdateRequest request);

    /**
     * 删除员工
     */
    void delete(Long id);
}
