package com.hrai.org.service;

import com.hrai.org.dto.OrgUnitCreateRequest;
import com.hrai.org.dto.OrgUnitDetailResponse;
import com.hrai.org.dto.OrgUnitTreeNode;
import com.hrai.org.dto.OrgUnitUpdateRequest;

import java.util.List;

/**
 * 组织单元服务
 */
public interface OrgUnitService {

    List<OrgUnitTreeNode> getTree();

    OrgUnitDetailResponse getById(Long id);

    Long create(OrgUnitCreateRequest request);

    void update(Long id, OrgUnitUpdateRequest request);

    void delete(Long id);
}
