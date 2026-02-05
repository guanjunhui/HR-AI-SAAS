package com.hrai.business.statemachine;

import com.hrai.business.entity.EmploymentEvent;
import com.hrai.business.enums.EmploymentEventStatus;
import com.hrai.common.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 任职事件状态机
 *
 * 状态流转：
 * draft -> pending (提交审批)
 * pending -> approved (审批通过)
 * pending -> rejected (审批驳回)
 * draft/pending -> cancelled (取消)
 */
@Component
public class EmploymentEventStateMachine {

    private static final Logger log = LoggerFactory.getLogger(EmploymentEventStateMachine.class);

    /**
     * 提交审批 (draft -> pending)
     */
    public void submit(EmploymentEvent event) {
        String currentStatus = event.getStatus();
        if (!EmploymentEventStatus.DRAFT.getCode().equals(currentStatus)) {
            throw new BizException(400, "只有草稿状态可以提交审批，当前状态: " + getStatusDesc(currentStatus));
        }

        event.setStatus(EmploymentEventStatus.PENDING.getCode());
        log.info("任职事件[{}]状态变更: {} -> {}", event.getId(), currentStatus, event.getStatus());
    }

    /**
     * 审批通过 (pending -> approved)
     */
    public void approve(EmploymentEvent event, Long approverId) {
        String currentStatus = event.getStatus();
        if (!EmploymentEventStatus.PENDING.getCode().equals(currentStatus)) {
            throw new BizException(400, "只有待审批状态可以通过，当前状态: " + getStatusDesc(currentStatus));
        }

        event.setStatus(EmploymentEventStatus.APPROVED.getCode());
        log.info("任职事件[{}]状态变更: {} -> {}, 审批人: {}", event.getId(), currentStatus, event.getStatus(), approverId);
    }

    /**
     * 驳回 (pending -> rejected)
     */
    public void reject(EmploymentEvent event, String reason) {
        String currentStatus = event.getStatus();
        if (!EmploymentEventStatus.PENDING.getCode().equals(currentStatus)) {
            throw new BizException(400, "只有待审批状态可以驳回，当前状态: " + getStatusDesc(currentStatus));
        }

        event.setStatus(EmploymentEventStatus.REJECTED.getCode());
        event.setRejectReason(reason);
        log.info("任职事件[{}]状态变更: {} -> {}, 驳回原因: {}", event.getId(), currentStatus, event.getStatus(), reason);
    }

    /**
     * 取消 (draft/pending -> cancelled)
     */
    public void cancel(EmploymentEvent event) {
        String currentStatus = event.getStatus();
        if (!EmploymentEventStatus.DRAFT.getCode().equals(currentStatus)
                && !EmploymentEventStatus.PENDING.getCode().equals(currentStatus)) {
            throw new BizException(400, "只有草稿或待审批状态可以取消，当前状态: " + getStatusDesc(currentStatus));
        }

        event.setStatus(EmploymentEventStatus.CANCELLED.getCode());
        log.info("任职事件[{}]状态变更: {} -> {}", event.getId(), currentStatus, event.getStatus());
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(String statusCode) {
        EmploymentEventStatus status = EmploymentEventStatus.fromCode(statusCode);
        return status != null ? status.getDesc() : statusCode;
    }
}
