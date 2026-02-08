package com.hrai.business.service;

import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.headcount.HeadcountCreateRequest;
import com.hrai.business.dto.headcount.HeadcountDetailResponse;
import com.hrai.business.dto.headcount.HeadcountQueryRequest;
import com.hrai.business.dto.headcount.HeadcountUpdateRequest;

/**
 * 编制管理服务接口
 */
public interface HeadcountService {

    /**
     * 分页查询编制列表
     */
    PageResponse<HeadcountDetailResponse> list(HeadcountQueryRequest query);

    /**
     * 根据ID获取编制详情
     */
    HeadcountDetailResponse getById(Long id);

    /**
     * 创建编制
     */
    Long create(HeadcountCreateRequest request);

    /**
     * 更新编制
     */
    void update(Long id, HeadcountUpdateRequest request);

    /**
     * 删除编制
     */
    void delete(Long id);
}
