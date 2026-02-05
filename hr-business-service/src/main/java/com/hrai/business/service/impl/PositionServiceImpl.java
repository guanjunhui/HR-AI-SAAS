package com.hrai.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.position.PositionCreateRequest;
import com.hrai.business.dto.position.PositionDetailResponse;
import com.hrai.business.dto.position.PositionQueryRequest;
import com.hrai.business.dto.position.PositionUpdateRequest;
import com.hrai.business.entity.Position;
import com.hrai.business.mapper.PositionMapper;
import com.hrai.business.service.PositionService;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 岗位服务实现
 */
@Service
public class PositionServiceImpl implements PositionService {

    private final PositionMapper positionMapper;

    public PositionServiceImpl(PositionMapper positionMapper) {
        this.positionMapper = positionMapper;
    }

    @Override
    public PageResponse<PositionDetailResponse> list(PositionQueryRequest query) {
        String tenantId = resolveTenantId();
        Page<Position> page = new Page<>(query.getPageNo(), query.getPageSize());

        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getTenantId, tenantId);

        if (query.getOrgUnitId() != null) {
            wrapper.eq(Position::getOrgUnitId, query.getOrgUnitId());
        }
        if (StrUtil.isNotBlank(query.getJobFamily())) {
            wrapper.eq(Position::getJobFamily, query.getJobFamily());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Position::getStatus, query.getStatus());
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Position::getPositionCode, keyword)
                    .or().like(Position::getPositionName, keyword));
        }
        wrapper.orderByDesc(Position::getId);

        IPage<Position> result = positionMapper.selectPage(page, wrapper);

        List<PositionDetailResponse> records = result.getRecords().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), query.getPageNo(), query.getPageSize());
    }

    @Override
    public List<PositionDetailResponse> listEnabled() {
        String tenantId = resolveTenantId();
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getTenantId, tenantId)
                .eq(Position::getStatus, 1)
                .orderByAsc(Position::getPositionLevel);

        return positionMapper.selectList(wrapper).stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    @Override
    public PositionDetailResponse getById(Long id) {
        Position position = positionMapper.selectById(id);
        if (position == null || !Objects.equals(position.getTenantId(), resolveTenantId())) {
            throw new BizException(404, "岗位不存在");
        }
        return toDetail(position);
    }

    @Override
    @Transactional
    public Long create(PositionCreateRequest request) {
        String tenantId = resolveTenantId();

        // 校验编码唯一性
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getTenantId, tenantId)
                .eq(Position::getPositionCode, request.getPositionCode());
        if (positionMapper.selectCount(wrapper) > 0) {
            throw new BizException(409, "岗位编码已存在");
        }

        Position position = new Position();
        position.setTenantId(tenantId);
        position.setPositionCode(request.getPositionCode());
        position.setPositionName(request.getPositionName());
        position.setPositionLevel(request.getPositionLevel());
        position.setJobFamily(request.getJobFamily());
        position.setOrgUnitId(request.getOrgUnitId());
        position.setDescription(request.getDescription());
        position.setRequirements(request.getRequirements());
        position.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        positionMapper.insert(position);
        return position.getId();
    }

    @Override
    @Transactional
    public void update(Long id, PositionUpdateRequest request) {
        String tenantId = resolveTenantId();
        Position position = positionMapper.selectById(id);
        if (position == null || !Objects.equals(position.getTenantId(), tenantId)) {
            throw new BizException(404, "岗位不存在");
        }

        position.setPositionName(request.getPositionName());
        position.setPositionLevel(request.getPositionLevel());
        position.setJobFamily(request.getJobFamily());
        position.setOrgUnitId(request.getOrgUnitId());
        position.setDescription(request.getDescription());
        position.setRequirements(request.getRequirements());
        if (request.getStatus() != null) {
            position.setStatus(request.getStatus());
        }

        positionMapper.updateById(position);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        String tenantId = resolveTenantId();
        Position position = positionMapper.selectById(id);
        if (position == null || !Objects.equals(position.getTenantId(), tenantId)) {
            throw new BizException(404, "岗位不存在");
        }

        // 使用带租户条件的删除，避免误删其他租户数据
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getId, id).eq(Position::getTenantId, tenantId);
        positionMapper.delete(wrapper);
    }

    private PositionDetailResponse toDetail(Position position) {
        PositionDetailResponse resp = new PositionDetailResponse();
        resp.setId(position.getId());
        resp.setPositionCode(position.getPositionCode());
        resp.setPositionName(position.getPositionName());
        resp.setPositionLevel(position.getPositionLevel());
        resp.setJobFamily(position.getJobFamily());
        resp.setOrgUnitId(position.getOrgUnitId());
        resp.setDescription(position.getDescription());
        resp.setRequirements(position.getRequirements());
        resp.setStatus(position.getStatus());
        resp.setCreatedAt(position.getCreatedAt());
        resp.setUpdatedAt(position.getUpdatedAt());
        return resp;
    }

    private String resolveTenantId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? TenantConstants.DEFAULT_TENANT_ID : tenantId;
    }
}
