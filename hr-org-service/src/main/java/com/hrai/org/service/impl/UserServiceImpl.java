package com.hrai.org.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import com.hrai.org.dto.PageResponse;
import com.hrai.org.dto.UserCreateRequest;
import com.hrai.org.dto.UserDetailResponse;
import com.hrai.org.dto.UserPasswordUpdateRequest;
import com.hrai.org.dto.UserQueryRequest;
import com.hrai.org.dto.UserRoleUpdateRequest;
import com.hrai.org.dto.UserStatusUpdateRequest;
import com.hrai.org.dto.UserUpdateRequest;
import com.hrai.org.entity.SysRole;
import com.hrai.org.entity.SysUser;
import com.hrai.org.mapper.SysRoleMapper;
import com.hrai.org.mapper.SysUserMapper;
import com.hrai.org.service.AuditLogService;
import com.hrai.org.service.DataScopeService;
import com.hrai.org.service.UserService;
import com.hrai.org.util.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final AuditLogService auditLogService;
    private final DataScopeService dataScopeService;

    public UserServiceImpl(SysUserMapper userMapper, SysRoleMapper roleMapper,
                           AuditLogService auditLogService, DataScopeService dataScopeService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.auditLogService = auditLogService;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public PageResponse<UserDetailResponse> list(UserQueryRequest query) {
        String tenantId = resolveTenantId();
        Page<SysUser> page = new Page<>(query.getPageNo(), query.getPageSize());
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId).eq("deleted", 0);

        if (query.getOrgUnitId() != null) {
            wrapper.eq("org_unit_id", query.getOrgUnitId());
        }
        if (query.getRoleId() != null) {
            wrapper.eq("role_id", query.getRoleId());
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus());
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like("username", keyword)
                    .or().like("real_name", keyword)
                    .or().like("email", keyword)
                    .or().like("phone", keyword));
        }
        dataScopeService.applyUserScope(wrapper);
        wrapper.orderByDesc("id");

        IPage<SysUser> result = userMapper.selectPage(page, wrapper);
        Map<Long, SysRole> roleMap = loadRoleMap(tenantId);

        List<UserDetailResponse> records = result.getRecords().stream()
                .map(user -> toDetail(user, roleMap.get(user.getRoleId())))
                .collect(Collectors.toList());

        return PageResponse.from(result, records);
    }

    @Override
    public UserDetailResponse getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1 || !Objects.equals(user.getTenantId(), resolveTenantId())) {
            throw new BizException(404, "用户不存在");
        }
        dataScopeService.assertUserAccessible(user);
        SysRole role = null;
        if (user.getRoleId() != null) {
            role = roleMapper.selectById(user.getRoleId());
        }
        return toDetail(user, role);
    }

    @Override
    @Transactional
    public Long create(UserCreateRequest request) {
        String tenantId = resolveTenantId();
        validateUserUnique(null, request.getUsername(), request.getEmail(), request.getPhone(), tenantId);
        SysRole role = roleMapper.selectById(request.getRoleId());
        if (role == null || isDeleted(role.getDeleted()) || !Objects.equals(role.getTenantId(), tenantId)) {
            throw new BizException(404, "角色不存在");
        }

        if (request.getOrgUnitId() != null) {
            dataScopeService.assertOrgUnitAccessible(request.getOrgUnitId());
        }

        SysUser user = new SysUser();
        user.setTenantId(tenantId);
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setOrgUnitId(request.getOrgUnitId());
        user.setRoleId(request.getRoleId());
        user.setStatus(request.getStatus());
        user.setPlanType(StrUtil.blankToDefault(request.getPlanType(), "free"));

        userMapper.insert(user);
        auditLogService.record("CREATE", "USER", String.valueOf(user.getId()), user.getUsername(), request);
        return user.getId();
    }

    @Override
    @Transactional
    public void update(Long id, UserUpdateRequest request) {
        String tenantId = resolveTenantId();
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1 || !Objects.equals(user.getTenantId(), tenantId)) {
            throw new BizException(404, "用户不存在");
        }

        dataScopeService.assertUserAccessible(user);

        validateUserUnique(id, request.getUsername(), request.getEmail(), request.getPhone(), tenantId);

        if (request.getOrgUnitId() != null) {
            dataScopeService.assertOrgUnitAccessible(request.getOrgUnitId());
        }

        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setOrgUnitId(request.getOrgUnitId());
        user.setStatus(request.getStatus());
        user.setPlanType(StrUtil.blankToDefault(request.getPlanType(), user.getPlanType()));

        userMapper.updateById(user);
        auditLogService.record("UPDATE", "USER", String.valueOf(user.getId()), user.getUsername(), request);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        String tenantId = resolveTenantId();
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1 || !Objects.equals(user.getTenantId(), tenantId)) {
            throw new BizException(404, "用户不存在");
        }
        dataScopeService.assertUserAccessible(user);
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("tenant_id", tenantId);
        userMapper.delete(wrapper);
        auditLogService.record("DELETE", "USER", String.valueOf(id), user.getUsername(), null);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, UserStatusUpdateRequest request) {
        SysUser user = getExistingUser(id);
        dataScopeService.assertUserAccessible(user);
        user.setStatus(request.getStatus());
        userMapper.updateById(user);
        auditLogService.record("UPDATE_STATUS", "USER", String.valueOf(id), user.getUsername(), request);
    }

    @Override
    @Transactional
    public void updatePassword(Long id, UserPasswordUpdateRequest request) {
        SysUser user = getExistingUser(id);
        dataScopeService.assertUserAccessible(user);
        user.setPassword(PasswordUtils.encode(request.getNewPassword()));
        userMapper.updateById(user);
        auditLogService.record("UPDATE_PASSWORD", "USER", String.valueOf(id), user.getUsername(), null);
    }

    @Override
    @Transactional
    public void updateRole(Long id, UserRoleUpdateRequest request) {
        String tenantId = resolveTenantId();
        SysUser user = getExistingUser(id);
        dataScopeService.assertUserAccessible(user);
        SysRole role = roleMapper.selectById(request.getRoleId());
        if (role == null || isDeleted(role.getDeleted()) || !Objects.equals(role.getTenantId(), tenantId)) {
            throw new BizException(404, "角色不存在");
        }
        user.setRoleId(request.getRoleId());
        userMapper.updateById(user);
        auditLogService.record("UPDATE_ROLE", "USER", String.valueOf(id), user.getUsername(), request);
    }

    private SysUser getExistingUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1 || !Objects.equals(user.getTenantId(), resolveTenantId())) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private void validateUserUnique(Long userId, String username, String email, String phone, String tenantId) {
        if (StrUtil.isNotBlank(username)) {
            SysUser exist = userMapper.selectByUsername(username, tenantId);
            if (exist != null && !Objects.equals(exist.getId(), userId)) {
                throw new BizException(409, "用户名已存在");
            }
        }
        if (StrUtil.isNotBlank(email)) {
            SysUser exist = userMapper.selectByEmail(email, tenantId);
            if (exist != null && !Objects.equals(exist.getId(), userId)) {
                throw new BizException(409, "邮箱已存在");
            }
        }
        if (StrUtil.isNotBlank(phone)) {
            SysUser exist = userMapper.selectByPhone(phone, tenantId);
            if (exist != null && !Objects.equals(exist.getId(), userId)) {
                throw new BizException(409, "手机号已存在");
            }
        }
    }

    private UserDetailResponse toDetail(SysUser user, SysRole role) {
        UserDetailResponse resp = new UserDetailResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(user.getRealName());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setAvatar(user.getAvatar());
        resp.setStatus(user.getStatus());
        resp.setRoleId(user.getRoleId());
        if (role != null) {
            resp.setRoleName(role.getName());
            resp.setRoleCode(role.getCode());
        }
        resp.setOrgUnitId(user.getOrgUnitId());
        resp.setPlanType(user.getPlanType());
        resp.setLastLoginTime(user.getLastLoginTime());
        resp.setLastLoginIp(user.getLastLoginIp());
        resp.setCreatedAt(user.getCreatedAt());
        resp.setUpdatedAt(user.getUpdatedAt());
        return resp;
    }

    private Map<Long, SysRole> loadRoleMap(String tenantId) {
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId).eq("deleted", 0);
        List<SysRole> roles = roleMapper.selectList(wrapper);
        return roles.stream().collect(Collectors.toMap(SysRole::getId, r -> r, (a, b) -> a));
    }

    private String resolveTenantId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? TenantConstants.DEFAULT_TENANT_ID : tenantId;
    }

    private boolean isDeleted(Integer deleted) {
        return deleted != null && deleted == 1;
    }
}
