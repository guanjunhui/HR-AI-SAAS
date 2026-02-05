package com.hrai.business.service;

import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.employmentevent.EmploymentEventApproveRequest;
import com.hrai.business.dto.employmentevent.EmploymentEventCreateRequest;
import com.hrai.business.dto.employmentevent.EmploymentEventDetailResponse;
import com.hrai.business.dto.employmentevent.EmploymentEventQueryRequest;

/**
 * 任职事件服务接口
 */
public interface EmploymentEventService {

    /**
     * 分页查询任职事件列表
     */
    PageResponse<EmploymentEventDetailResponse> list(EmploymentEventQueryRequest query);

    /**
     * 根据ID获取任职事件详情
     */
    EmploymentEventDetailResponse getById(Long id);

    /**
     * 创建任职事件（草稿状态）
     */
    Long create(EmploymentEventCreateRequest request);

    /**
     * 提交审批（draft -> pending）
     */
    void submit(Long id);

    /**
     * 审批任职事件（pending -> approved/rejected）
     */
    void approve(Long id, EmploymentEventApproveRequest request);

    /**
     * 取消任职事件
     */
    void cancel(Long id);

    /**
     * 删除任职事件（仅草稿状态可删除）
     */
    void delete(Long id);
}
