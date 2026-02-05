package com.hrai.business.service;

import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.position.PositionCreateRequest;
import com.hrai.business.dto.position.PositionDetailResponse;
import com.hrai.business.dto.position.PositionQueryRequest;
import com.hrai.business.dto.position.PositionUpdateRequest;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface PositionService {

    /**
     * 分页查询岗位列表
     */
    PageResponse<PositionDetailResponse> list(PositionQueryRequest query);

    /**
     * 查询所有启用的岗位（下拉框用）
     */
    List<PositionDetailResponse> listEnabled();

    /**
     * 根据ID获取岗位详情
     */
    PositionDetailResponse getById(Long id);

    /**
     * 创建岗位
     */
    Long create(PositionCreateRequest request);

    /**
     * 更新岗位
     */
    void update(Long id, PositionUpdateRequest request);

    /**
     * 删除岗位
     */
    void delete(Long id);
}
