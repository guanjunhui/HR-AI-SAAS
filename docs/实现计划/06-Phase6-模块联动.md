# Phase 6: 模块联动与优化

> 模块间集成 + 系统优化

## 概述

Phase 6 实现各模块之间的联动集成，确保业务流程端到端贯通，并进行系统优化。

---

## 1. 模块联动设计

### 1.1 联动关系总览

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           HR AI SaaS 模块联动图                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌──────────┐    LK0001    ┌──────────┐    LK0002    ┌──────────┐     │
│   │ Core HR  │─────────────▶│  招聘    │─────────────▶│  入职    │     │
│   │ (M02)    │  编制调整    │  (M03)   │  Offer接受   │  (M04)   │     │
│   └────┬─────┘              └──────────┘              └────┬─────┘     │
│        │                                                    │           │
│        │ LK0003                                             │           │
│        │◀─────────────────────────────────────────────────────┘           │
│        │ 入职完成                                                         │
│        │                                                                 │
│        ▼                                                                 │
│   ┌──────────┐    LK0004    ┌──────────┐                               │
│   │  考勤    │─────────────▶│  薪酬    │                               │
│   │  (M05)   │  月度汇总    │  (M06)   │                               │
│   └──────────┘              └──────────┘                               │
│                                                                         │
│   ┌──────────┐    LK0005    ┌──────────┐                               │
│   │  绩效    │─────────────▶│ AI中心   │                               │
│   │  (M07)   │  结果归档    │  (M10)   │                               │
│   └──────────┘              └────┬─────┘                               │
│                                   │                                     │
│                    ┌──────────────┼──────────────┐                     │
│                    │ LK0006      │ LK0007      │ LK0008               │
│                    ▼              ▼              ▼                     │
│              ┌──────────┐  ┌──────────┐  ┌──────────┐                 │
│              │  招聘    │  │ Core HR  │  │  工单    │                 │
│              │  (M03)   │  │  (M02)   │  │  (M08)   │                 │
│              └──────────┘  └──────────┘  └──────────┘                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 联动详细设计

#### LK0001: 编制调整 -> 触发招聘

**触发条件**：
- 编制数增加
- 当前在编人数 < 编制数

**联动逻辑**：
```java
@Component
public class HeadcountRecruitingIntegration {

    @Autowired
    private JobRequisitionService jobRequisitionService;

    @Autowired
    private AISuggestionService aiSuggestionService;

    @EventListener
    public void onHeadcountChanged(HeadcountChangedEvent event) {
        Headcount headcount = event.getHeadcount();

        // 计算空缺
        int vacancy = headcount.getBudgetCount() - headcount.getCurrentCount();

        if (vacancy > 0 && event.isIncreased()) {
            // 生成AI建议：是否发起招聘
            AISuggestion suggestion = AISuggestion.builder()
                .tenantId(headcount.getTenantId())
                .suggestionType("recruitment")
                .title(String.format("建议为 %s 岗位发起招聘",
                    headcount.getPosition().getPositionName()))
                .description(String.format(
                    "部门 %s 的 %s 岗位编制增加至 %d 人，当前在编 %d 人，空缺 %d 人",
                    headcount.getOrgUnit().getUnitName(),
                    headcount.getPosition().getPositionName(),
                    headcount.getBudgetCount(),
                    headcount.getCurrentCount(),
                    vacancy))
                .suggestedActions(buildRecruitmentActions(headcount, vacancy))
                .impactLevel(vacancy >= 3 ? "high" : "medium")
                .build();

            aiSuggestionService.create(suggestion);
        }
    }

    private List<SuggestedAction> buildRecruitmentActions(Headcount headcount, int vacancy) {
        return List.of(
            SuggestedAction.builder()
                .actionType("create_requisition")
                .actionName("创建招聘需求")
                .params(Map.of(
                    "position_id", headcount.getPositionId(),
                    "org_unit_id", headcount.getOrgUnitId(),
                    "headcount", vacancy
                ))
                .build()
        );
    }
}
```

#### LK0002: Offer接受 -> 创建入职记录

**触发条件**：
- Offer状态变为 `accepted`

**联动逻辑**：
```java
@Component
public class OfferOnboardingIntegration {

    @Autowired
    private OnboardingService onboardingService;

    @EventListener
    @Transactional
    public void onOfferAccepted(OfferAcceptedEvent event) {
        Offer offer = event.getOffer();

        // 1. 创建入职记录
        Onboarding onboarding = new Onboarding();
        onboarding.setTenantId(offer.getTenantId());
        onboarding.setOnboardingCode(generateOnboardingCode());
        onboarding.setOfferId(offer.getId());
        onboarding.setCandidateId(offer.getCandidateId());
        onboarding.setExpectedDate(offer.getExpectedOnboardDate());
        onboarding.setPositionId(offer.getPositionId());
        onboarding.setOrgUnitId(offer.getOrgUnitId());
        onboarding.setStatus("pending");

        onboardingService.create(onboarding);

        // 2. 初始化入职资料清单
        initializeMaterials(onboarding);

        // 3. 初始化入职任务
        initializeTasks(onboarding);

        // 4. 更新候选人状态
        candidateService.updateStatus(offer.getCandidateId(), "pending_onboard");

        // 5. 更新招聘需求计数
        jobRequisitionService.incrementOfferAccepted(offer.getJobReqId());

        // 6. 发送通知
        notifyRelevantParties(onboarding);
    }

    private void initializeMaterials(Onboarding onboarding) {
        List<String> requiredMaterials = List.of(
            "id_card", "photo", "diploma", "degree",
            "bank_card", "health_cert", "resignation_cert"
        );

        for (String materialType : requiredMaterials) {
            OnboardingMaterial material = new OnboardingMaterial();
            material.setOnboardingId(onboarding.getId());
            material.setMaterialType(materialType);
            material.setMaterialName(getMaterialName(materialType));
            material.setIsRequired(isRequired(materialType));
            onboardingMaterialService.create(material);
        }
    }

    private void initializeTasks(Onboarding onboarding) {
        List<OnboardingTask> tasks = List.of(
            createTask(onboarding, "document", "签署劳动合同", null, 0),
            createTask(onboarding, "equipment", "领取办公设备", null, 1),
            createTask(onboarding, "access", "开通系统账号", null, 2),
            createTask(onboarding, "training", "入职培训", null, 3),
            createTask(onboarding, "introduction", "部门介绍", onboarding.getDirectManagerId(), 4)
        );

        tasks.forEach(onboardingTaskService::create);
    }
}
```

#### LK0003: 入职完成 -> 创建员工

**触发条件**：
- 入职状态变为 `completed`
- 所有必须资料已审核

**联动逻辑**：
```java
@Component
public class OnboardingEmployeeIntegration {

    @EventListener
    @Transactional
    public void onOnboardingCompleted(OnboardingCompletedEvent event) {
        Onboarding onboarding = event.getOnboarding();
        Candidate candidate = candidateService.getById(onboarding.getCandidateId());
        CandidateResume resume = candidateResumeService.getByCandidate(candidate.getId());

        // 1. 创建员工
        Employee employee = new Employee();
        employee.setTenantId(onboarding.getTenantId());
        employee.setEmployeeCode(generateEmployeeCode(onboarding.getTenantId()));
        employee.setRealName(candidate.getName());
        employee.setGender(candidate.getGender());
        employee.setPhone(candidate.getPhone());
        employee.setEmail(candidate.getEmail());
        employee.setOrgUnitId(onboarding.getOrgUnitId());
        employee.setPositionId(onboarding.getPositionId());
        employee.setDirectManagerId(onboarding.getDirectManagerId());
        employee.setEntryDate(onboarding.getActualDate());
        employee.setEmployeeStatus("trial");

        employeeService.create(employee);

        // 2. 创建员工详情（从简历提取）
        EmployeeDetail detail = new EmployeeDetail();
        detail.setEmployeeId(employee.getId());
        if (resume != null && resume.getEducations() != null) {
            // 取最高学历
            detail.setHighestEducation(extractHighestEducation(resume.getEducations()));
            detail.setGraduateSchool(extractSchool(resume.getEducations()));
            detail.setMajor(extractMajor(resume.getEducations()));
        }
        // 从入职资料提取银行信息
        detail.setBankName(extractBankName(onboarding.getId()));
        detail.setBankAccount(extractBankAccount(onboarding.getId()));

        employeeDetailService.create(detail);

        // 3. 创建系统用户
        SysUser user = new SysUser();
        user.setTenantId(onboarding.getTenantId());
        user.setUserCode(employee.getEmployeeCode());
        user.setUsername(generateUsername(employee));
        user.setPassword(generateInitialPassword());
        user.setRealName(employee.getRealName());
        user.setEmail(employee.getEmail());
        user.setPhone(employee.getPhone());
        user.setOrgUnitId(employee.getOrgUnitId());
        user.setStatus(1);

        userService.create(user);

        // 4. 关联员工和系统用户
        employee.setSysUserId(user.getId());
        employeeService.updateById(employee);

        // 5. 更新入职记录
        onboarding.setEmployeeId(employee.getId());
        onboardingService.updateById(onboarding);

        // 6. 更新候选人状态
        candidate.setStatus("onboarded");
        candidateService.updateById(candidate);

        // 7. 更新编制
        headcountService.incrementCurrentCount(
            onboarding.getOrgUnitId(),
            onboarding.getPositionId()
        );

        // 8. 更新招聘需求
        Offer offer = offerService.getById(onboarding.getOfferId());
        if (offer != null) {
            jobRequisitionService.incrementHired(offer.getJobReqId());
        }

        // 9. 发送欢迎通知
        sendWelcomeNotification(employee, user);
    }
}
```

#### LK0004: 考勤汇总 -> 算薪数据

**触发条件**：
- 算薪批次开始计算

**联动逻辑**：
```java
@Component
public class AttendancePayrollIntegration {

    /**
     * 获取算薪所需的考勤数据
     */
    public AttendanceDataForPayroll getAttendanceData(
            Long employeeId,
            Date periodStart,
            Date periodEnd) {

        AttendanceDataForPayroll data = new AttendanceDataForPayroll();

        // 1. 应出勤天数
        int workDays = attendanceService.countWorkdays(periodStart, periodEnd);
        data.setWorkDays(new BigDecimal(workDays));

        // 2. 实际出勤天数
        List<AttendanceRecord> records = attendanceService.getRecords(
            employeeId, periodStart, periodEnd);

        long actualWorkDays = records.stream()
            .filter(r -> List.of("normal", "late", "early", "late_early")
                .contains(r.getStatus()))
            .count();
        data.setActualWorkDays(new BigDecimal(actualWorkDays));

        // 3. 缺勤天数
        long absentDays = records.stream()
            .filter(r -> "absent".equals(r.getStatus()))
            .count();
        data.setAbsentDays(new BigDecimal(absentDays));

        // 4. 请假天数（按类型统计）
        Map<String, BigDecimal> leaveDaysByType = leaveRequestService
            .countLeaveDaysByType(employeeId, periodStart, periodEnd);
        data.setLeaveDaysByType(leaveDaysByType);

        BigDecimal totalLeaveDays = leaveDaysByType.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.setLeaveDays(totalLeaveDays);

        // 5. 加班时长（按类型统计）
        Map<String, BigDecimal> overtimeByType = overtimeService
            .countOvertimeHoursByType(employeeId, periodStart, periodEnd);
        data.setOvertimeHoursByType(overtimeByType);

        BigDecimal totalOvertime = overtimeByType.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.setOvertimeHours(totalOvertime);

        // 6. 迟到/早退次数（用于扣款）
        long lateCount = records.stream()
            .filter(r -> r.getLateMinutes() != null && r.getLateMinutes() > 0)
            .count();
        data.setLateCount((int) lateCount);

        long earlyCount = records.stream()
            .filter(r -> r.getEarlyMinutes() != null && r.getEarlyMinutes() > 0)
            .count();
        data.setEarlyCount((int) earlyCount);

        return data;
    }
}
```

#### LK0005: 绩效结果 -> AI洞察

**触发条件**：
- 绩效周期完成（状态变为 `completed`）

**联动逻辑**：
```java
@Component
public class PerformanceAIIntegration {

    @EventListener
    public void onPerformanceCycleCompleted(PerformanceCycleCompletedEvent event) {
        PerformanceCycle cycle = event.getCycle();

        // 1. 生成绩效分析洞察
        generatePerformanceInsights(cycle);

        // 2. 识别高绩效员工，生成晋升/调薪建议
        generateHighPerformerSuggestions(cycle);

        // 3. 识别低绩效员工，生成改进建议
        generateLowPerformerSuggestions(cycle);

        // 4. 更新离职风险模型
        updateTurnoverRiskModel(cycle);
    }

    private void generateHighPerformerSuggestions(PerformanceCycle cycle) {
        List<PerformanceResult> highPerformers = performanceResultService
            .findByCycleAndRating(cycle.getId(), List.of("A", "exceeds"));

        for (PerformanceResult result : highPerformers) {
            Employee employee = employeeService.getById(result.getEmployeeId());

            // 检查是否符合晋升条件
            if (shouldSuggestPromotion(employee, result)) {
                AISuggestion suggestion = AISuggestion.builder()
                    .tenantId(cycle.getTenantId())
                    .suggestionType("performance")
                    .title(String.format("建议为 %s 考虑晋升", employee.getRealName()))
                    .description(String.format(
                        "%s 在本周期绩效评级为 %s，且已在当前职级工作 %d 个月，建议考虑晋升",
                        employee.getRealName(),
                        result.getFinalRating(),
                        getMonthsInCurrentLevel(employee)))
                    .affectedEmployees(List.of(employee.getId()))
                    .affectedCount(1)
                    .impactLevel("medium")
                    .confidenceScore(new BigDecimal("0.85"))
                    .suggestedActions(List.of(
                        buildAction("create_event", "发起晋升申请",
                            Map.of("employee_id", employee.getId(), "event_type", "promotion"))
                    ))
                    .build();

                aiSuggestionService.create(suggestion);
            }

            // 检查是否应该调薪
            if (shouldSuggestSalaryAdjustment(employee, result)) {
                // 生成调薪建议...
            }
        }
    }
}
```

#### LK0006-LK0008: AI建议 -> 执行行动

**触发条件**：
- AI建议被采纳（状态变为 `adopted`）

**联动逻辑**：
```java
@Component
public class AIActionExecutor {

    @Autowired
    private Map<String, ActionHandler> actionHandlers;

    @EventListener
    @Transactional
    public void onSuggestionAdopted(SuggestionAdoptedEvent event) {
        AISuggestion suggestion = event.getSuggestion();

        for (SuggestedAction suggestedAction : suggestion.getSuggestedActions()) {
            // 创建行动记录
            AIAction action = AIAction.builder()
                .tenantId(suggestion.getTenantId())
                .actionCode(generateActionCode())
                .suggestionId(suggestion.getId())
                .triggerType("manual")
                .actionType(suggestedAction.getActionType())
                .actionParams(suggestedAction.getParams())
                .status("adopted")
                .build();

            aiActionService.create(action);
        }
    }

    /**
     * 执行AI行动
     */
    public void executeAction(Long actionId, Long executorId) {
        AIAction action = aiActionService.getById(actionId);

        // 更新状态为执行中
        action.setStatus("executing");
        action.setExecutorId(executorId);
        action.setExecuteTime(new Date());
        aiActionService.updateById(action);

        try {
            // 获取对应的处理器
            ActionHandler handler = actionHandlers.get(action.getActionType());
            if (handler == null) {
                throw new BizException("不支持的行动类型: " + action.getActionType());
            }

            // 执行行动
            ActionResult result = handler.execute(action);

            // 更新结果
            action.setStatus("executed");
            action.setResult(result.toJson());
            action.setCreatedEntityType(result.getCreatedEntityType());
            action.setCreatedEntityId(result.getCreatedEntityId());

        } catch (Exception e) {
            action.setStatus("failed");
            action.setErrorMessage(e.getMessage());
        }

        aiActionService.updateById(action);
    }
}

// 创建招聘需求的处理器
@Component("create_requisition")
public class CreateRequisitionHandler implements ActionHandler {

    @Autowired
    private JobRequisitionService jobRequisitionService;

    @Override
    public ActionResult execute(AIAction action) {
        Map<String, Object> params = action.getActionParams();

        JobRequisition req = new JobRequisition();
        req.setTenantId(action.getTenantId());
        req.setPositionId(((Number) params.get("position_id")).longValue());
        req.setOrgUnitId(((Number) params.get("org_unit_id")).longValue());
        req.setHeadcount((Integer) params.get("headcount"));
        req.setStatus("draft");
        req.setApplicantId(action.getExecutorId());

        jobRequisitionService.create(req);

        return ActionResult.builder()
            .success(true)
            .createdEntityType("job_requisition")
            .createdEntityId(req.getId())
            .message("招聘需求创建成功")
            .build();
    }
}

// 创建人事事件的处理器
@Component("create_event")
public class CreateEventHandler implements ActionHandler {

    @Autowired
    private EmploymentEventService employmentEventService;

    @Override
    public ActionResult execute(AIAction action) {
        Map<String, Object> params = action.getActionParams();

        EmploymentEvent event = new EmploymentEvent();
        event.setTenantId(action.getTenantId());
        event.setEventType((String) params.get("event_type"));
        event.setEmployeeId(((Number) params.get("employee_id")).longValue());
        event.setStatus("draft");
        event.setApplicantId(action.getExecutorId());

        // 根据事件类型设置变更内容
        event.setChangeContent(buildChangeContent(params));

        employmentEventService.create(event);

        return ActionResult.builder()
            .success(true)
            .createdEntityType("employment_event")
            .createdEntityId(event.getId())
            .message("人事事件创建成功")
            .build();
    }
}

// 创建工单的处理器
@Component("create_ticket")
public class CreateTicketHandler implements ActionHandler {

    @Override
    public ActionResult execute(AIAction action) {
        Map<String, Object> params = action.getActionParams();

        Ticket ticket = new Ticket();
        ticket.setTenantId(action.getTenantId());
        ticket.setTicketTypeId(((Number) params.get("ticket_type_id")).longValue());
        ticket.setTitle((String) params.get("title"));
        ticket.setDescription((String) params.get("description"));
        ticket.setSubmitterId(action.getExecutorId());
        ticket.setStatus("new");
        ticket.setAiRouted(true);

        ticketService.create(ticket);

        return ActionResult.builder()
            .success(true)
            .createdEntityType("ticket")
            .createdEntityId(ticket.getId())
            .message("工单创建成功")
            .build();
    }
}
```

---

## 2. 事件发布与监听架构

### 2.1 事件基类

```java
public abstract class DomainEvent {
    private final String eventId;
    private final String tenantId;
    private final Long operatorId;
    private final LocalDateTime occurredAt;

    protected DomainEvent(String tenantId, Long operatorId) {
        this.eventId = UUID.randomUUID().toString();
        this.tenantId = tenantId;
        this.operatorId = operatorId;
        this.occurredAt = LocalDateTime.now();
    }
}
```

### 2.2 事件定义

```java
// 编制变更事件
public class HeadcountChangedEvent extends DomainEvent {
    private final Headcount headcount;
    private final int previousBudgetCount;
    private final boolean increased;
}

// Offer接受事件
public class OfferAcceptedEvent extends DomainEvent {
    private final Offer offer;
    private final Candidate candidate;
}

// 入职完成事件
public class OnboardingCompletedEvent extends DomainEvent {
    private final Onboarding onboarding;
}

// 绩效周期完成事件
public class PerformanceCycleCompletedEvent extends DomainEvent {
    private final PerformanceCycle cycle;
}

// AI建议采纳事件
public class SuggestionAdoptedEvent extends DomainEvent {
    private final AISuggestion suggestion;
}
```

### 2.3 事件发布

```java
@Service
public class OfferService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public void acceptOffer(Long offerId) {
        Offer offer = getById(offerId);

        // 更新状态
        offer.setStatus("accepted");
        offer.setCandidateResponse("accepted");
        offer.setResponseDate(new Date());
        updateById(offer);

        // 发布事件
        Candidate candidate = candidateService.getById(offer.getCandidateId());
        eventPublisher.publishEvent(new OfferAcceptedEvent(
            offer.getTenantId(),
            TenantContext.getUserId(),
            offer,
            candidate
        ));
    }
}
```

---

## 3. 状态机汇总

### 3.1 状态机配置

| 状态机 | 状态数 | 事件数 | 实现类 |
|--------|--------|--------|--------|
| EmploymentEvent | 5 | 4 | EmploymentEventStateMachine |
| JobRequisition | 5 | 7 | JobRequisitionStateMachine |
| Candidate | 7 | 6 | CandidateStateMachine |
| LeaveRequest | 5 | 4 | LeaveRequestStateMachine |
| OvertimeRequest | 4 | 4 | OvertimeRequestStateMachine |
| PayrollRun | 6 | 6 | PayrollRunStateMachine |
| PerformanceCycle | 7 | 6 | PerformanceCycleStateMachine |
| Ticket | 7 | 8 | TicketStateMachine |
| AIAction | 5 | 4 | AIActionStateMachine |

### 3.2 状态机通用配置

```java
@Configuration
@EnableStateMachine
public class StateMachineConfig {

    @Bean
    public StateMachinePersister<String, String, String> persister() {
        return new DefaultStateMachinePersister<>(
            new StateMachinePersist<String, String, String>() {
                @Override
                public void write(StateMachineContext<String, String> context, String contextObj) {
                    // 持久化到Redis
                    redisTemplate.opsForValue().set(
                        "sm:" + contextObj,
                        context,
                        1, TimeUnit.HOURS
                    );
                }

                @Override
                public StateMachineContext<String, String> read(String contextObj) {
                    return redisTemplate.opsForValue().get("sm:" + contextObj);
                }
            }
        );
    }
}
```

---

## 4. 数据一致性保障

### 4.1 分布式事务处理

对于跨服务的联动操作，采用最终一致性方案：

```java
@Service
public class OnboardingCompletionService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 入职完成 - 发送消息触发后续流程
     */
    @Transactional
    public void completeOnboarding(Long onboardingId) {
        Onboarding onboarding = onboardingService.getById(onboardingId);

        // 1. 更新入职状态（本地事务）
        onboarding.setStatus("completed");
        onboarding.setActualDate(new Date());
        onboardingService.updateById(onboarding);

        // 2. 发送消息到Kafka（最终一致性）
        OnboardingCompletedMessage message = new OnboardingCompletedMessage();
        message.setOnboardingId(onboardingId);
        message.setTenantId(onboarding.getTenantId());
        message.setCandidateId(onboarding.getCandidateId());

        kafkaTemplate.send(
            KafkaConfig.Topics.WORKFLOW_EVENT,
            onboardingId.toString(),
            message
        );
    }
}

@Component
public class OnboardingCompletedConsumer {

    @KafkaListener(topics = KafkaConfig.Topics.WORKFLOW_EVENT)
    public void handleOnboardingCompleted(OnboardingCompletedMessage message) {
        // 幂等性检查
        if (isProcessed(message.getOnboardingId())) {
            return;
        }

        try {
            // 执行后续逻辑
            employeeCreationService.createEmployee(message);

            // 标记已处理
            markAsProcessed(message.getOnboardingId());
        } catch (Exception e) {
            // 记录失败，等待重试
            log.error("处理入职完成消息失败: {}", message, e);
            throw e; // 触发重试
        }
    }
}
```

### 4.2 幂等性保障

```java
@Service
public class IdempotentService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 检查操作是否已执行
     */
    public boolean isProcessed(String operationKey) {
        return Boolean.TRUE.equals(
            redisTemplate.hasKey("idempotent:" + operationKey)
        );
    }

    /**
     * 标记操作已执行
     */
    public void markAsProcessed(String operationKey) {
        redisTemplate.opsForValue().set(
            "idempotent:" + operationKey,
            "1",
            7, TimeUnit.DAYS
        );
    }

    /**
     * 带幂等性检查的执行
     */
    public <T> T executeIdempotent(String operationKey, Supplier<T> operation) {
        // 尝试获取锁
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(
            "lock:" + operationKey,
            "1",
            30, TimeUnit.SECONDS
        );

        if (!Boolean.TRUE.equals(locked)) {
            throw new ConcurrentOperationException("操作正在进行中");
        }

        try {
            if (isProcessed(operationKey)) {
                log.info("操作已执行，跳过: {}", operationKey);
                return null;
            }

            T result = operation.get();

            markAsProcessed(operationKey);

            return result;
        } finally {
            redisTemplate.delete("lock:" + operationKey);
        }
    }
}
```

---

## 5. 系统优化

### 5.1 性能优化

#### 5.1.1 数据库优化

```sql
-- 添加复合索引优化查询
ALTER TABLE employees ADD INDEX idx_tenant_org_status (tenant_id, org_unit_id, employee_status);
ALTER TABLE attendance_records ADD INDEX idx_tenant_employee_date (tenant_id, employee_id, attendance_date);
ALTER TABLE payroll_details ADD INDEX idx_run_employee (payroll_run_id, employee_id);

-- 分区表（按月分区考勤记录）
ALTER TABLE attendance_records PARTITION BY RANGE (YEAR(attendance_date) * 100 + MONTH(attendance_date)) (
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    -- ...
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

#### 5.1.2 缓存策略

```java
@Service
public class EmployeeCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "employee:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @Cacheable(value = "employees", key = "#employeeId")
    public Employee getById(Long employeeId) {
        return employeeMapper.selectById(employeeId);
    }

    @CacheEvict(value = "employees", key = "#employee.id")
    public void update(Employee employee) {
        employeeMapper.updateById(employee);
    }

    /**
     * 批量获取（减少Redis往返）
     */
    public Map<Long, Employee> batchGet(List<Long> employeeIds) {
        List<String> keys = employeeIds.stream()
            .map(id -> CACHE_PREFIX + id)
            .collect(Collectors.toList());

        List<Object> cached = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, Employee> result = new HashMap<>();
        List<Long> missedIds = new ArrayList<>();

        for (int i = 0; i < employeeIds.size(); i++) {
            if (cached.get(i) != null) {
                result.put(employeeIds.get(i), (Employee) cached.get(i));
            } else {
                missedIds.add(employeeIds.get(i));
            }
        }

        // 批量查询未命中的
        if (!missedIds.isEmpty()) {
            List<Employee> fromDb = employeeMapper.selectBatchIds(missedIds);
            Map<String, Employee> toCache = new HashMap<>();

            for (Employee emp : fromDb) {
                result.put(emp.getId(), emp);
                toCache.put(CACHE_PREFIX + emp.getId(), emp);
            }

            // 批量写入缓存
            redisTemplate.opsForValue().multiSet(toCache);
        }

        return result;
    }
}
```

#### 5.1.3 AI响应优化

```java
@Service
public class AICacheService {

    /**
     * 常见问题缓存
     */
    @Cacheable(value = "ai:faq", key = "#question.hashCode()", unless = "#result == null")
    public String getCachedAnswer(String question) {
        // 计算问题的向量
        float[] vector = embeddingService.embed(question);

        // 从Milvus查找相似问题
        List<SearchResult> similar = milvusClient.search(
            "faq_cache",
            vector,
            1,  // Top-1
            0.95  // 高相似度阈值
        );

        if (!similar.isEmpty() && similar.get(0).getScore() > 0.95) {
            return similar.get(0).getAnswer();
        }

        return null;
    }

    /**
     * 缓存AI回答
     */
    public void cacheAnswer(String question, String answer) {
        float[] vector = embeddingService.embed(question);

        milvusClient.insert("faq_cache", Map.of(
            "question", question,
            "answer", answer,
            "vector", vector,
            "created_at", System.currentTimeMillis()
        ));
    }
}
```

### 5.2 可观测性

#### 5.2.1 指标收集

```java
@Component
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    private final Counter employeeCreatedCounter;
    private final Counter ticketCreatedCounter;
    private final Timer aiResponseTimer;
    private final Gauge activeEmployeeGauge;

    public BusinessMetrics(MeterRegistry meterRegistry, EmployeeService employeeService) {
        this.meterRegistry = meterRegistry;

        this.employeeCreatedCounter = Counter.builder("hr.employee.created")
            .description("Number of employees created")
            .register(meterRegistry);

        this.ticketCreatedCounter = Counter.builder("hr.ticket.created")
            .description("Number of tickets created")
            .tag("type", "all")
            .register(meterRegistry);

        this.aiResponseTimer = Timer.builder("hr.ai.response")
            .description("AI response time")
            .register(meterRegistry);

        this.activeEmployeeGauge = Gauge.builder("hr.employee.active",
                employeeService, es -> es.countActive())
            .description("Number of active employees")
            .register(meterRegistry);
    }

    public void recordEmployeeCreated() {
        employeeCreatedCounter.increment();
    }

    public void recordAiResponse(long durationMs) {
        aiResponseTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
}
```

#### 5.2.2 链路追踪

```java
@Aspect
@Component
public class TracingAspect {

    @Autowired
    private Tracer tracer;

    @Around("@annotation(traced)")
    public Object trace(ProceedingJoinPoint pjp, Traced traced) throws Throwable {
        Span span = tracer.spanBuilder(traced.value())
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("method", pjp.getSignature().getName());
            span.setAttribute("tenant_id", TenantContext.getTenantId());

            Object result = pjp.proceed();

            span.setStatus(StatusCode.OK);
            return result;

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

---

## 6. 验证清单

### 6.1 联动测试用例

| 测试场景 | 前置条件 | 操作 | 预期结果 |
|----------|----------|------|----------|
| LK0001 | 有编制记录 | 增加编制数 | 生成招聘建议 |
| LK0002 | 有待接受Offer | Offer接受 | 创建入职记录、资料清单、任务 |
| LK0003 | 入职资料已审核 | 完成入职 | 创建员工、系统用户、更新编制 |
| LK0004 | 有考勤记录 | 执行算薪 | 正确获取考勤数据 |
| LK0005 | 绩效周期进行中 | 完成周期 | 生成洞察和建议 |
| LK0006 | 有招聘建议 | 采纳执行 | 创建招聘需求 |
| LK0007 | 有人事建议 | 采纳执行 | 创建人事事件 |
| LK0008 | 有工单建议 | 采纳执行 | 创建工单 |

### 6.2 端到端测试

```
完整招聘到入职流程：
1. 创建岗位编制 (headcount)
2. 发起招聘需求 (job_requisition)
3. 添加候选人 (candidate)
4. 安排面试 (interview)
5. 提交面试反馈 (interview_feedback)
6. 发出Offer (offer)
7. 候选人接受Offer -> 自动创建入职记录
8. 上传入职资料 (onboarding_materials)
9. 审核资料
10. 完成入职 -> 自动创建员工、系统用户
11. 验证编制计数更新
12. 验证招聘需求计数更新
```

---

## 7. 交付物清单

| 类型 | 数量 | 说明 |
|------|------|------|
| Integration类 | 8个 | 模块联动集成类 |
| Event类 | 10个 | 领域事件类 |
| EventListener | 8个 | 事件监听器 |
| ActionHandler | 5个 | AI行动处理器 |
| 配置优化 | 多项 | 索引、缓存、分区等 |
| 监控指标 | 10+ | Prometheus指标 |

---

*Phase 6 完成后，系统各模块将实现无缝联动，业务流程端到端贯通，并具备完善的监控和优化能力。*
