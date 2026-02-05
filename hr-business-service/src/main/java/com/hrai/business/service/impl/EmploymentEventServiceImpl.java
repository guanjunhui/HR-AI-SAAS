package com.hrai.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.employmentevent.EmploymentEventApproveRequest;
import com.hrai.business.dto.employmentevent.EmploymentEventCreateRequest;
import com.hrai.business.dto.employmentevent.EmploymentEventDetailResponse;
import com.hrai.business.dto.employmentevent.EmploymentEventQueryRequest;
import com.hrai.business.entity.Employee;
import com.hrai.business.entity.EmploymentEvent;
import com.hrai.business.entity.Position;
import com.hrai.business.enums.EmployeeStatus;
import com.hrai.business.enums.EmploymentEventStatus;
import com.hrai.business.enums.EmploymentEventType;
import com.hrai.business.mapper.EmployeeMapper;
import com.hrai.business.mapper.EmploymentEventMapper;
import com.hrai.business.mapper.PositionMapper;
import com.hrai.business.service.EmploymentEventService;
import com.hrai.business.statemachine.EmploymentEventStateMachine;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 任职事件服务实现
 */
@Service
public class EmploymentEventServiceImpl implements EmploymentEventService {

    private final EmploymentEventMapper eventMapper;
    private final EmployeeMapper employeeMapper;
    private final PositionMapper positionMapper;
    private final EmploymentEventStateMachine stateMachine;

    public EmploymentEventServiceImpl(EmploymentEventMapper eventMapper,
                                      EmployeeMapper employeeMapper,
                                      PositionMapper positionMapper,
                                      EmploymentEventStateMachine stateMachine) {
        this.eventMapper = eventMapper;
        this.employeeMapper = employeeMapper;
        this.positionMapper = positionMapper;
        this.stateMachine = stateMachine;
    }

    @Override
    public PageResponse<EmploymentEventDetailResponse> list(EmploymentEventQueryRequest query) {
        String tenantId = resolveTenantId();
        Page<EmploymentEvent> page = new Page<>(query.getPageNo(), query.getPageSize());

        LambdaQueryWrapper<EmploymentEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentEvent::getTenantId, tenantId);

        if (query.getEmployeeId() != null) {
            wrapper.eq(EmploymentEvent::getEmployeeId, query.getEmployeeId());
        }
        if (StrUtil.isNotBlank(query.getEventType())) {
            wrapper.eq(EmploymentEvent::getEventType, query.getEventType());
        }
        if (StrUtil.isNotBlank(query.getStatus())) {
            wrapper.eq(EmploymentEvent::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(EmploymentEvent::getId);

        IPage<EmploymentEvent> result = eventMapper.selectPage(page, wrapper);

        // 批量加载关联数据
        Map<Long, Employee> employeeMap = loadEmployeeMap(tenantId);
        Map<Long, Position> positionMap = loadPositionMap(tenantId);

        List<EmploymentEventDetailResponse> records = result.getRecords().stream()
                .map(event -> toDetail(event, employeeMap, positionMap))
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), query.getPageNo(), query.getPageSize());
    }

    @Override
    public EmploymentEventDetailResponse getById(Long id) {
        String tenantId = resolveTenantId();
        EmploymentEvent event = eventMapper.selectById(id);
        if (event == null || !Objects.equals(event.getTenantId(), tenantId)) {
            throw new BizException(404, "任职事件不存在");
        }

        Map<Long, Employee> employeeMap = loadEmployeeMap(tenantId);
        Map<Long, Position> positionMap = loadPositionMap(tenantId);

        return toDetail(event, employeeMap, positionMap);
    }

    @Override
    @Transactional
    public Long create(EmploymentEventCreateRequest request) {
        String tenantId = resolveTenantId();

        // 校验员工存在
        Employee employee = employeeMapper.selectById(request.getEmployeeId());
        if (employee == null || !Objects.equals(employee.getTenantId(), tenantId)) {
            throw new BizException(404, "员工不存在");
        }

        EmploymentEvent event = new EmploymentEvent();
        event.setTenantId(tenantId);
        event.setEmployeeId(request.getEmployeeId());
        event.setEventType(request.getEventType());
        event.setEventDate(LocalDate.parse(request.getEventDate(), DateTimeFormatter.ISO_LOCAL_DATE));
        event.setReason(request.getReason());

        // 变动前信息（从当前员工信息获取）
        event.setFromOrgUnitId(employee.getOrgUnitId());
        event.setFromPositionId(employee.getPositionId());
        event.setFromSalaryGrade(request.getFromSalaryGrade());

        // 变动后信息
        event.setToOrgUnitId(request.getToOrgUnitId());
        event.setToPositionId(request.getToPositionId());
        event.setToSalaryGrade(request.getToSalaryGrade());

        event.setRemark(request.getRemark());

        // 初始状态为草稿
        event.setStatus(EmploymentEventStatus.DRAFT.getCode());

        // 设置申请人（当前登录用户）
        String userIdStr = TenantContext.getUserId();
        if (userIdStr != null && !userIdStr.isBlank()) {
            event.setApplicantId(Long.valueOf(userIdStr));
        }

        eventMapper.insert(event);
        return event.getId();
    }

    @Override
    @Transactional
    public void submit(Long id) {
        String tenantId = resolveTenantId();
        EmploymentEvent event = eventMapper.selectById(id);
        if (event == null || !Objects.equals(event.getTenantId(), tenantId)) {
            throw new BizException(404, "任职事件不存在");
        }

        // 使用状态机执行状态转移
        stateMachine.submit(event);

        eventMapper.updateById(event);
    }

    @Override
    @Transactional
    public void approve(Long id, EmploymentEventApproveRequest request) {
        String tenantId = resolveTenantId();
        EmploymentEvent event = eventMapper.selectById(id);
        if (event == null || !Objects.equals(event.getTenantId(), tenantId)) {
            throw new BizException(404, "任职事件不存在");
        }

        String userIdStr = TenantContext.getUserId();
        Long currentUserId = (userIdStr != null && !userIdStr.isBlank()) ? Long.valueOf(userIdStr) : null;

        if (Boolean.TRUE.equals(request.getApproved())) {
            // 通过审批
            stateMachine.approve(event, currentUserId);

            // 审批通过后，更新员工信息
            applyEventToEmployee(event);
        } else {
            // 驳回
            stateMachine.reject(event, request.getRejectReason());
        }

        event.setApproverId(currentUserId);
        event.setApprovedAt(LocalDateTime.now());
        eventMapper.updateById(event);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        String tenantId = resolveTenantId();
        EmploymentEvent event = eventMapper.selectById(id);
        if (event == null || !Objects.equals(event.getTenantId(), tenantId)) {
            throw new BizException(404, "任职事件不存在");
        }

        stateMachine.cancel(event);
        eventMapper.updateById(event);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        String tenantId = resolveTenantId();
        EmploymentEvent event = eventMapper.selectById(id);
        if (event == null || !Objects.equals(event.getTenantId(), tenantId)) {
            throw new BizException(404, "任职事件不存在");
        }

        // 只有草稿状态可以删除
        if (!EmploymentEventStatus.DRAFT.getCode().equals(event.getStatus())) {
            throw new BizException(400, "只有草稿状态的任职事件可以删除");
        }

        LambdaQueryWrapper<EmploymentEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentEvent::getId, id).eq(EmploymentEvent::getTenantId, tenantId);
        eventMapper.delete(wrapper);
    }

    /**
     * 审批通过后，将事件应用到员工信息
     */
    private void applyEventToEmployee(EmploymentEvent event) {
        Employee employee = employeeMapper.selectById(event.getEmployeeId());
        if (employee == null) {
            return;
        }

        String eventType = event.getEventType();
        EmploymentEventType type = EmploymentEventType.fromCode(eventType);

        if (type == null) {
            return;
        }

        switch (type) {
            case ENTRY:
                // 入职：设置入职日期和状态
                employee.setEntryDate(event.getEventDate());
                employee.setEmployeeStatus(EmployeeStatus.TRIAL.getCode());
                break;

            case REGULAR:
                // 转正：更新状态和转正日期
                employee.setRegularDate(event.getEventDate());
                employee.setEmployeeStatus(EmployeeStatus.REGULAR.getCode());
                break;

            case TRANSFER:
            case PROMOTION:
            case DEMOTION:
                // 调岗/晋升/降级：更新部门和岗位
                if (event.getToOrgUnitId() != null) {
                    employee.setOrgUnitId(event.getToOrgUnitId());
                }
                if (event.getToPositionId() != null) {
                    employee.setPositionId(event.getToPositionId());
                }
                break;

            case RESIGNATION:
                // 离职：更新状态和离职日期
                employee.setResignationDate(event.getEventDate());
                employee.setEmployeeStatus(EmployeeStatus.RESIGNED.getCode());
                break;

            default:
                break;
        }

        employeeMapper.updateById(employee);
    }

    private EmploymentEventDetailResponse toDetail(EmploymentEvent event,
                                                   Map<Long, Employee> employeeMap,
                                                   Map<Long, Position> positionMap) {
        EmploymentEventDetailResponse resp = new EmploymentEventDetailResponse();
        resp.setId(event.getId());
        resp.setEmployeeId(event.getEmployeeId());

        Employee employee = employeeMap.get(event.getEmployeeId());
        if (employee != null) {
            resp.setEmployeeName(employee.getRealName());
            resp.setEmployeeCode(employee.getEmployeeCode());
        }

        resp.setEventType(event.getEventType());
        EmploymentEventType eventType = EmploymentEventType.fromCode(event.getEventType());
        if (eventType != null) {
            resp.setEventTypeDesc(eventType.getDesc());
        }

        resp.setEventDate(event.getEventDate());
        resp.setReason(event.getReason());

        // 变动前信息
        resp.setFromOrgUnitId(event.getFromOrgUnitId());
        resp.setFromPositionId(event.getFromPositionId());
        if (event.getFromPositionId() != null) {
            Position fromPosition = positionMap.get(event.getFromPositionId());
            if (fromPosition != null) {
                resp.setFromPositionName(fromPosition.getPositionName());
            }
        }
        resp.setFromSalaryGrade(event.getFromSalaryGrade());

        // 变动后信息
        resp.setToOrgUnitId(event.getToOrgUnitId());
        resp.setToPositionId(event.getToPositionId());
        if (event.getToPositionId() != null) {
            Position toPosition = positionMap.get(event.getToPositionId());
            if (toPosition != null) {
                resp.setToPositionName(toPosition.getPositionName());
            }
        }
        resp.setToSalaryGrade(event.getToSalaryGrade());

        // 状态信息
        resp.setStatus(event.getStatus());
        EmploymentEventStatus status = EmploymentEventStatus.fromCode(event.getStatus());
        if (status != null) {
            resp.setStatusDesc(status.getDesc());
        }

        // 审批信息
        resp.setApplicantId(event.getApplicantId());
        if (event.getApplicantId() != null) {
            Employee applicant = employeeMap.get(event.getApplicantId());
            if (applicant != null) {
                resp.setApplicantName(applicant.getRealName());
            }
        }
        resp.setApproverId(event.getApproverId());
        if (event.getApproverId() != null) {
            Employee approver = employeeMap.get(event.getApproverId());
            if (approver != null) {
                resp.setApproverName(approver.getRealName());
            }
        }
        resp.setApprovedAt(event.getApprovedAt());
        resp.setRejectReason(event.getRejectReason());
        resp.setRemark(event.getRemark());
        resp.setCreatedAt(event.getCreatedAt());
        resp.setUpdatedAt(event.getUpdatedAt());

        return resp;
    }

    private Map<Long, Employee> loadEmployeeMap(String tenantId) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getTenantId, tenantId);
        List<Employee> employees = employeeMapper.selectList(wrapper);
        return employees.stream().collect(Collectors.toMap(Employee::getId, e -> e, (a, b) -> a));
    }

    private Map<Long, Position> loadPositionMap(String tenantId) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getTenantId, tenantId);
        List<Position> positions = positionMapper.selectList(wrapper);
        return positions.stream().collect(Collectors.toMap(Position::getId, p -> p, (a, b) -> a));
    }

    private String resolveTenantId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? TenantConstants.DEFAULT_TENANT_ID : tenantId;
    }
}
