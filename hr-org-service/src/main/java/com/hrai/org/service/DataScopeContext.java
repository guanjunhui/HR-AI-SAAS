package com.hrai.org.service;

import java.util.Collections;
import java.util.List;

/**
 * Data scope evaluation result for current request.
 */
public class DataScopeContext {

    private final Integer dataScope;
    private final Long userId;
    private final Long orgUnitId;
    private final List<Long> orgUnitIds;
    private final boolean enforced;

    public DataScopeContext(Integer dataScope, Long userId, Long orgUnitId, List<Long> orgUnitIds, boolean enforced) {
        this.dataScope = dataScope;
        this.userId = userId;
        this.orgUnitId = orgUnitId;
        this.orgUnitIds = orgUnitIds == null ? Collections.emptyList() : orgUnitIds;
        this.enforced = enforced;
    }

    public Integer getDataScope() {
        return dataScope;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrgUnitId() {
        return orgUnitId;
    }

    public List<Long> getOrgUnitIds() {
        return orgUnitIds;
    }

    public boolean isEnforced() {
        return enforced;
    }

    public boolean isAllScope() {
        return enforced && dataScope != null && dataScope == 1;
    }

    public boolean isDeptAndChildrenScope() {
        return enforced && dataScope != null && dataScope == 2;
    }

    public boolean isDeptOnlyScope() {
        return enforced && dataScope != null && dataScope == 3;
    }

    public boolean isSelfScope() {
        return enforced && dataScope != null && dataScope == 4;
    }

    public boolean isRestricted() {
        return enforced && dataScope != null && dataScope != 1;
    }
}
