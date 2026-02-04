package com.hrai.org.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import com.hrai.org.dto.OrgUnitCreateRequest;
import com.hrai.org.dto.OrgUnitDetailResponse;
import com.hrai.org.dto.OrgUnitTreeNode;
import com.hrai.org.dto.OrgUnitUpdateRequest;
import com.hrai.org.entity.OrgUnit;
import com.hrai.org.mapper.OrgUnitMapper;
import com.hrai.org.service.AuditLogService;
import com.hrai.org.service.DataScopeContext;
import com.hrai.org.service.DataScopeService;
import com.hrai.org.service.OrgUnitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 组织单元服务实现
 */
@Service
public class OrgUnitServiceImpl implements OrgUnitService {

    private final OrgUnitMapper orgUnitMapper;
    private final AuditLogService auditLogService;
    private final DataScopeService dataScopeService;

    public OrgUnitServiceImpl(OrgUnitMapper orgUnitMapper, AuditLogService auditLogService,
                              DataScopeService dataScopeService) {
        this.orgUnitMapper = orgUnitMapper;
        this.auditLogService = auditLogService;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public List<OrgUnitTreeNode> getTree() {
        String tenantId = resolveTenantId();
        QueryWrapper<OrgUnit> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId).eq("deleted", 0).orderByAsc("sort_order", "id");
        List<OrgUnit> units = orgUnitMapper.selectList(wrapper);

        DataScopeContext scope = dataScopeService.getCurrentScope();
        if (scope.isRestricted()) {
            List<Long> allowedOrgIds = scope.getOrgUnitIds();
            if (allowedOrgIds.isEmpty()) {
                return new ArrayList<>();
            }
            units = units.stream()
                    .filter(unit -> allowedOrgIds.contains(unit.getId()))
                    .toList();
        }

        Map<Long, OrgUnitTreeNode> map = new HashMap<>();
        for (OrgUnit unit : units) {
            map.put(unit.getId(), toTreeNode(unit));
        }

        List<OrgUnitTreeNode> roots = new ArrayList<>();
        for (OrgUnit unit : units) {
            OrgUnitTreeNode node = map.get(unit.getId());
            if (unit.getParentId() == null || unit.getParentId() == 0) {
                roots.add(node);
                continue;
            }
            OrgUnitTreeNode parent = map.get(unit.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }

        sortTree(roots);
        return roots;
    }

    @Override
    public OrgUnitDetailResponse getById(Long id) {
        OrgUnit unit = orgUnitMapper.selectById(id);
        if (unit == null || unit.getDeleted() == 1 || !Objects.equals(unit.getTenantId(), resolveTenantId())) {
            throw new BizException(404, "组织单元不存在");
        }
        dataScopeService.assertOrgUnitAccessible(id);
        return toDetail(unit);
    }

    @Override
    @Transactional
    public Long create(OrgUnitCreateRequest request) {
        String tenantId = resolveTenantId();

        if (request.getParentId() == null || request.getParentId() == 0L) {
            dataScopeService.assertOrgUnitAccessible(0L);
        } else {
            dataScopeService.assertOrgUnitAccessible(request.getParentId());
        }

        OrgUnit exists = orgUnitMapper.selectByCode(request.getCode(), tenantId);
        if (exists != null) {
            throw new BizException(409, "组织编码已存在");
        }

        Long parentId = request.getParentId() == null ? 0L : request.getParentId();
        OrgUnit parent = null;
        int parentLevel = 0;
        String parentPath = "/";
        if (parentId > 0) {
            parent = orgUnitMapper.selectById(parentId);
            if (parent == null || parent.getDeleted() == 1 || !Objects.equals(parent.getTenantId(), tenantId)) {
                throw new BizException(404, "父级组织不存在");
            }
            parentLevel = parent.getLevel() == null ? 0 : parent.getLevel();
            parentPath = parent.getPath() == null ? "/" : parent.getPath();
        }

        OrgUnit unit = new OrgUnit();
        unit.setTenantId(tenantId);
        unit.setParentId(parentId);
        unit.setName(request.getName());
        unit.setCode(request.getCode());
        unit.setType(request.getType());
        unit.setLeaderId(request.getLeaderId());
        unit.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        unit.setStatus(request.getStatus());

        orgUnitMapper.insert(unit);

        String path = parentPath + unit.getId() + "/";
        int level = parentLevel + 1;
        unit.setPath(path);
        unit.setLevel(level);
        orgUnitMapper.updateById(unit);

        auditLogService.record("CREATE", "ORG_UNIT", String.valueOf(unit.getId()), unit.getName(), request);

        return unit.getId();
    }

    @Override
    @Transactional
    public void update(Long id, OrgUnitUpdateRequest request) {
        String tenantId = resolveTenantId();
        OrgUnit unit = orgUnitMapper.selectById(id);
        if (unit == null || unit.getDeleted() == 1 || !Objects.equals(unit.getTenantId(), tenantId)) {
            throw new BizException(404, "组织单元不存在");
        }
        dataScopeService.assertOrgUnitAccessible(id);

        OrgUnit codeExists = orgUnitMapper.selectByCode(request.getCode(), tenantId);
        if (codeExists != null && !Objects.equals(codeExists.getId(), id)) {
            throw new BizException(409, "组织编码已存在");
        }

        Long newParentId = request.getParentId();
        if (newParentId == null) {
            newParentId = 0L;
        }
        if (newParentId == 0L) {
            dataScopeService.assertOrgUnitAccessible(0L);
        } else {
            dataScopeService.assertOrgUnitAccessible(newParentId);
        }
        if (Objects.equals(newParentId, id)) {
            throw new BizException(400, "父级不能为自身");
        }

        String oldPath = unit.getPath();
        Integer oldLevel = unit.getLevel();
        boolean parentChanged = !Objects.equals(unit.getParentId(), newParentId);

        if (parentChanged) {
            if (newParentId > 0) {
                OrgUnit newParent = orgUnitMapper.selectById(newParentId);
                if (newParent == null || newParent.getDeleted() == 1 || !Objects.equals(newParent.getTenantId(), tenantId)) {
                    throw new BizException(404, "父级组织不存在");
                }
                if (oldPath != null && newParent.getPath() != null && newParent.getPath().startsWith(oldPath)) {
                    throw new BizException(400, "不能移动到自身子级");
                }
            }
        }

        unit.setParentId(newParentId);
        unit.setName(request.getName());
        unit.setCode(request.getCode());
        unit.setType(request.getType());
        unit.setLeaderId(request.getLeaderId());
        unit.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        unit.setStatus(request.getStatus());

        if (parentChanged) {
            String parentPath = "/";
            int parentLevel = 0;
            if (newParentId > 0) {
                OrgUnit newParent = orgUnitMapper.selectById(newParentId);
                parentPath = newParent.getPath() == null ? "/" : newParent.getPath();
                parentLevel = newParent.getLevel() == null ? 0 : newParent.getLevel();
            }
            int newLevel = parentLevel + 1;
            String newPath = parentPath + unit.getId() + "/";
            unit.setLevel(newLevel);
            unit.setPath(newPath);
            orgUnitMapper.updateById(unit);

            if (oldPath != null && oldLevel != null) {
                List<OrgUnit> descendants = orgUnitMapper.selectDescendants(oldPath, tenantId);
                for (OrgUnit descendant : descendants) {
                    if (Objects.equals(descendant.getId(), unit.getId())) {
                        continue;
                    }
                    String descPath = descendant.getPath();
                    if (descPath != null && descPath.startsWith(oldPath)) {
                        descendant.setPath(newPath + descPath.substring(oldPath.length()));
                    }
                    if (descendant.getLevel() != null) {
                        descendant.setLevel(descendant.getLevel() - oldLevel + newLevel);
                    }
                    orgUnitMapper.updateById(descendant);
                }
            }
        } else {
            orgUnitMapper.updateById(unit);
        }

        auditLogService.record("UPDATE", "ORG_UNIT", String.valueOf(unit.getId()), unit.getName(), request);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        String tenantId = resolveTenantId();
        OrgUnit unit = orgUnitMapper.selectById(id);
        if (unit == null || unit.getDeleted() == 1 || !Objects.equals(unit.getTenantId(), tenantId)) {
            throw new BizException(404, "组织单元不存在");
        }
        dataScopeService.assertOrgUnitAccessible(id);

        List<OrgUnit> children = orgUnitMapper.selectChildren(id, tenantId);
        if (children != null && !children.isEmpty()) {
            throw new BizException(400, "存在子组织，无法删除");
        }

        QueryWrapper<OrgUnit> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("tenant_id", tenantId);
        orgUnitMapper.delete(wrapper);

        auditLogService.record("DELETE", "ORG_UNIT", String.valueOf(id), unit.getName(), null);
    }

    private OrgUnitTreeNode toTreeNode(OrgUnit unit) {
        OrgUnitTreeNode node = new OrgUnitTreeNode();
        node.setId(unit.getId());
        node.setParentId(unit.getParentId());
        node.setName(unit.getName());
        node.setCode(unit.getCode());
        node.setType(unit.getType());
        node.setLeaderId(unit.getLeaderId());
        node.setSortOrder(unit.getSortOrder());
        node.setStatus(unit.getStatus());
        node.setLevel(unit.getLevel());
        node.setPath(unit.getPath());
        return node;
    }

    private OrgUnitDetailResponse toDetail(OrgUnit unit) {
        OrgUnitDetailResponse resp = new OrgUnitDetailResponse();
        resp.setId(unit.getId());
        resp.setParentId(unit.getParentId());
        resp.setName(unit.getName());
        resp.setCode(unit.getCode());
        resp.setType(unit.getType());
        resp.setPath(unit.getPath());
        resp.setLevel(unit.getLevel());
        resp.setLeaderId(unit.getLeaderId());
        resp.setSortOrder(unit.getSortOrder());
        resp.setStatus(unit.getStatus());
        resp.setCreatedAt(unit.getCreatedAt());
        resp.setUpdatedAt(unit.getUpdatedAt());
        return resp;
    }

    private void sortTree(List<OrgUnitTreeNode> nodes) {
        nodes.sort(Comparator
                .comparing(OrgUnitTreeNode::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(OrgUnitTreeNode::getId));
        for (OrgUnitTreeNode node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    private String resolveTenantId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? TenantConstants.DEFAULT_TENANT_ID : tenantId;
    }
}
