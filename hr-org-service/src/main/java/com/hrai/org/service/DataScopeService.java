package com.hrai.org.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hrai.org.entity.SysUser;

import java.util.List;

/**
 * Data scope service for row-level access control.
 */
public interface DataScopeService {

    DataScopeContext getCurrentScope();

    void applyUserScope(QueryWrapper<SysUser> wrapper);

    void assertUserAccessible(SysUser targetUser);

    void assertOrgUnitAccessible(Long orgUnitId);

    List<Long> getAccessibleOrgUnitIds();
}
