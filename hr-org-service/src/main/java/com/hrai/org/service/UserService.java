package com.hrai.org.service;

import com.hrai.org.dto.PageResponse;
import com.hrai.org.dto.UserCreateRequest;
import com.hrai.org.dto.UserDetailResponse;
import com.hrai.org.dto.UserQueryRequest;
import com.hrai.org.dto.UserRoleUpdateRequest;
import com.hrai.org.dto.UserStatusUpdateRequest;
import com.hrai.org.dto.UserPasswordUpdateRequest;
import com.hrai.org.dto.UserUpdateRequest;

/**
 * 用户服务
 */
public interface UserService {

    PageResponse<UserDetailResponse> list(UserQueryRequest query);

    UserDetailResponse getById(Long id);

    Long create(UserCreateRequest request);

    void update(Long id, UserUpdateRequest request);

    void delete(Long id);

    void updateStatus(Long id, UserStatusUpdateRequest request);

    void updatePassword(Long id, UserPasswordUpdateRequest request);

    void updateRole(Long id, UserRoleUpdateRequest request);
}
