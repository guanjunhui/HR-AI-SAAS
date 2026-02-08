package com.hrai.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.headcount.HeadcountCreateRequest;
import com.hrai.business.dto.headcount.HeadcountDetailResponse;
import com.hrai.business.dto.headcount.HeadcountQueryRequest;
import com.hrai.business.dto.headcount.HeadcountUpdateRequest;
import com.hrai.business.entity.Headcount;
import com.hrai.business.entity.Position;
import com.hrai.business.mapper.HeadcountMapper;
import com.hrai.business.mapper.PositionMapper;
import com.hrai.business.service.HeadcountService;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 编制管理服务实现类
 */
@Service
public class HeadcountServiceImpl implements HeadcountService {

    private final HeadcountMapper headcountMapper;
    private final PositionMapper positionMapper;

    public HeadcountServiceImpl(HeadcountMapper headcountMapper, PositionMapper positionMapper) {
        this.headcountMapper = headcountMapper;
        this.positionMapper = positionMapper;
    }

    @Override
    public PageResponse<HeadcountDetailResponse> list(HeadcountQueryRequest query) {
        String tenantId = resolveTenantId();

        LambdaQueryWrapper<Headcount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Headcount::getTenantId, tenantId);
        if (query.getOrgUnitId() != null) {
            wrapper.eq(Headcount::getOrgUnitId, query.getOrgUnitId());
        }
        if (query.getPositionId() != null) {
            wrapper.eq(Headcount::getPositionId, query.getPositionId());
        }
        if (query.getYear() != null) {
            wrapper.eq(Headcount::getYear, query.getYear());
        }
        if (query.getQuarter() != null) {
            wrapper.eq(Headcount::getQuarter, query.getQuarter());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Headcount::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Headcount::getCreatedAt);

        Page<Headcount> page = new Page<>(query.getPageNo(), query.getPageSize());
        Page<Headcount> result = headcountMapper.selectPage(page, wrapper);

        List<HeadcountDetailResponse> records = result.getRecords().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                records,
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize());
    }

    @Override
    public HeadcountDetailResponse getById(Long id) {
        String tenantId = resolveTenantId();
        Headcount headcount = headcountMapper.selectById(id);
        if (headcount == null || !Objects.equals(headcount.getTenantId(), tenantId)) {
            throw new BizException(404, "编制不存在");
        }
        return toDetailResponse(headcount);
    }

    @Override
    @Transactional
    public Long create(HeadcountCreateRequest request) {
        String tenantId = resolveTenantId();

        // 检查是否已存在相同组织+岗位+年份+季度的编制
        LambdaQueryWrapper<Headcount> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Headcount::getTenantId, tenantId)
                .eq(Headcount::getOrgUnitId, request.getOrgUnitId())
                .eq(Headcount::getPositionId, request.getPositionId());
        if (request.getYear() != null) {
            existWrapper.eq(Headcount::getYear, request.getYear());
        }
        if (request.getQuarter() != null) {
            existWrapper.eq(Headcount::getQuarter, request.getQuarter());
        }
        Long count = headcountMapper.selectCount(existWrapper);
        if (count > 0) {
            throw new BizException(400, "该组织岗位的编制配置已存在");
        }

        Headcount headcount = new Headcount();
        headcount.setTenantId(tenantId);
        headcount.setOrgUnitId(request.getOrgUnitId());
        headcount.setPositionId(request.getPositionId());
        headcount.setBudgetCount(request.getBudgetCount());
        headcount.setActualCount(0);
        headcount.setYear(request.getYear());
        headcount.setQuarter(request.getQuarter());
        headcount.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        headcountMapper.insert(headcount);
        return headcount.getId();
    }

    @Override
    @Transactional
    public void update(Long id, HeadcountUpdateRequest request) {
        String tenantId = resolveTenantId();
        Headcount headcount = headcountMapper.selectById(id);
        if (headcount == null || !Objects.equals(headcount.getTenantId(), tenantId)) {
            throw new BizException(404, "编制不存在");
        }

        if (request.getBudgetCount() != null) {
            headcount.setBudgetCount(request.getBudgetCount());
        }
        if (request.getYear() != null) {
            headcount.setYear(request.getYear());
        }
        if (request.getQuarter() != null) {
            headcount.setQuarter(request.getQuarter());
        }
        if (request.getStatus() != null) {
            headcount.setStatus(request.getStatus());
        }

        headcountMapper.updateById(headcount);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        String tenantId = resolveTenantId();
        Headcount headcount = headcountMapper.selectById(id);
        if (headcount == null || !Objects.equals(headcount.getTenantId(), tenantId)) {
            throw new BizException(404, "编制不存在");
        }
        LambdaQueryWrapper<Headcount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Headcount::getId, id).eq(Headcount::getTenantId, tenantId);
        headcountMapper.delete(wrapper);
    }

    private HeadcountDetailResponse toDetailResponse(Headcount headcount) {
        HeadcountDetailResponse response = new HeadcountDetailResponse();
        response.setId(headcount.getId());
        response.setTenantId(headcount.getTenantId());
        response.setOrgUnitId(headcount.getOrgUnitId());
        response.setPositionId(headcount.getPositionId());
        response.setBudgetCount(headcount.getBudgetCount());
        response.setActualCount(headcount.getActualCount());
        response.setYear(headcount.getYear());
        response.setQuarter(headcount.getQuarter());
        response.setStatus(headcount.getStatus());
        response.setCreatedAt(headcount.getCreatedAt());
        response.setUpdatedAt(headcount.getUpdatedAt());

        // 获取岗位名称
        if (headcount.getPositionId() != null) {
            Position position = positionMapper.selectById(headcount.getPositionId());
            if (position != null) {
                response.setPositionName(position.getPositionName());
            }
        }

        // TODO: 获取组织名称（需要跨服务调用 hr-org-service）
        // 暂时不填充组织名称，后续可通过 Feign 调用

        return response;
    }

    private String resolveTenantId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? TenantConstants.DEFAULT_TENANT_ID : tenantId;
    }
}
