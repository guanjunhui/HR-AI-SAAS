package com.hrai.org.service;

import com.hrai.org.dto.RoleCreateRequest;
import com.hrai.org.dto.RoleDetailResponse;
import com.hrai.org.dto.RolePermissionsUpdateRequest;
import com.hrai.org.dto.RoleUpdateRequest;

import java.util.List;

/**
 * 角色服务
 */
public interface RoleService {

    List<RoleDetailResponse> list(String keyword, Integer status);

    RoleDetailResponse getById(Long id);

    Long create(RoleCreateRequest request);

    void update(Long id, RoleUpdateRequest request);

    void delete(Long id);

    void updatePermissions(Long id, RolePermissionsUpdateRequest request);
}
