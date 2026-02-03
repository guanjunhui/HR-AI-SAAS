# Phase 5: AI能力中心与报表

> AI能力中心（M10）+ 报表与分析（M09）

## 概述

Phase 5 实现系统的AI核心能力和报表分析功能，包括：
- AI对话助手（政策问答、自然语言查询）
- AI洞察分析（团队概览、稳定性分析、离职风险）
- AI建议池与行动执行
- AI输出审计
- What-if模拟
- 报表配置与看板

---

## 1. 数据库表设计（7张表）

### 1.1 AI能力中心模块（M10）- 5张表

#### 1.1.1 AI建议表 (ai_suggestions)

```sql
-- AI建议表
CREATE TABLE ai_suggestions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    suggestion_code VARCHAR(64) NOT NULL UNIQUE COMMENT '建议编号',
    suggestion_type VARCHAR(50) COMMENT '建议类型：turnover_risk/headcount/salary_adjustment/performance/recruitment',
    title VARCHAR(200) NOT NULL COMMENT '建议标题',
    description TEXT COMMENT '建议描述',
    reasoning TEXT COMMENT '推理依据',
    impact_level VARCHAR(10) COMMENT '影响级别：high/medium/low',
    affected_employees JSON COMMENT '涉及员工ID列表(JSON数组)',
    affected_count INT COMMENT '涉及人数',
    suggested_actions JSON COMMENT '建议行动(JSON数组)',
    data_sources JSON COMMENT '数据来源(JSON数组)',
    confidence_score DECIMAL(3,2) COMMENT '置信度(0-1)',
    status VARCHAR(20) DEFAULT 'generated' COMMENT '状态：generated/reviewed/adopted/dismissed/executed/expired',
    reviewed_by BIGINT COMMENT '审核人ID',
    review_time TIMESTAMP COMMENT '审核时间',
    review_comment TEXT COMMENT '审核意见',
    executed_by BIGINT COMMENT '执行人ID',
    execute_time TIMESTAMP COMMENT '执行时间',
    execution_result TEXT COMMENT '执行结果',
    expires_at TIMESTAMP COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_type (tenant_id, suggestion_type),
    INDEX idx_status (status),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI建议表';
```

**建议类型枚举**：
- `turnover_risk`: 离职风险预警
- `headcount`: 编制建议
- `salary_adjustment`: 薪资调整建议
- `performance`: 绩效改进建议
- `recruitment`: 招聘建议
- `training`: 培训建议
- `workload`: 工作量平衡建议

**suggested_actions JSON 结构**：
```json
[
  {
    "action_type": "create_ticket",
    "action_name": "发起1对1沟通",
    "params": {
      "ticket_type": "hr_communication",
      "employee_id": 123
    }
  },
  {
    "action_type": "salary_change",
    "action_name": "调薪申请",
    "params": {
      "employee_id": 123,
      "suggested_amount": 2000
    }
  }
]
```

#### 1.1.2 AI行动表 (ai_actions)

```sql
-- AI行动表
CREATE TABLE ai_actions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    action_code VARCHAR(64) NOT NULL UNIQUE COMMENT '行动编号',
    suggestion_id BIGINT COMMENT '关联建议ID',
    trigger_type VARCHAR(20) COMMENT '触发类型：manual-手动/auto-自动/scheduled-定时',
    action_type VARCHAR(50) NOT NULL COMMENT '行动类型',
    action_params JSON COMMENT '行动参数(JSON)',
    status VARCHAR(20) DEFAULT 'generated' COMMENT '状态：generated/adopted/executing/executed/failed/cancelled',
    executor_id BIGINT COMMENT '执行人ID',
    execute_time TIMESTAMP COMMENT '执行时间',
    result JSON COMMENT '执行结果(JSON)',
    error_message TEXT COMMENT '错误信息',
    created_entity_type VARCHAR(50) COMMENT '创建的实体类型',
    created_entity_id BIGINT COMMENT '创建的实体ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant (tenant_id),
    INDEX idx_suggestion (suggestion_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI行动表';
```

**行动类型枚举**：
- `create_ticket`: 创建工单
- `create_event`: 创建人事事件
- `send_notification`: 发送通知
- `schedule_meeting`: 安排会议
- `create_requisition`: 创建招聘需求
- `generate_report`: 生成报告

#### 1.1.3 AI输出审计日志表 (ai_audit_logs)

```sql
-- AI输出审计日志表
CREATE TABLE ai_audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    session_id VARCHAR(64) COMMENT '会话ID',
    user_id BIGINT COMMENT '用户ID',
    agent_type VARCHAR(50) COMMENT 'Agent类型',
    agent_name VARCHAR(100) COMMENT 'Agent名称',
    input_content TEXT COMMENT '输入内容',
    output_content TEXT COMMENT '输出内容',
    retrieved_docs JSON COMMENT '检索的文档(JSON数组)',
    citations JSON COMMENT '引用来源(JSON数组)',
    function_calls JSON COMMENT 'Function调用记录(JSON数组)',
    prompt_tokens INT COMMENT 'Prompt Token数',
    completion_tokens INT COMMENT 'Completion Token数',
    total_tokens INT COMMENT '总Token数',
    response_time_ms INT COMMENT '响应时间(毫秒)',
    user_feedback VARCHAR(10) COMMENT '用户反馈：positive/negative/neutral',
    feedback_comment TEXT COMMENT '反馈评论',
    has_sensitive_info TINYINT DEFAULT 0 COMMENT '是否包含敏感信息',
    has_hallucination TINYINT DEFAULT 0 COMMENT '是否存在幻觉(人工标记)',
    manual_review_needed TINYINT DEFAULT 0 COMMENT '是否需要人工审核',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant_session (tenant_id, session_id),
    INDEX idx_user (user_id),
    INDEX idx_agent (agent_type),
    INDEX idx_time (created_at),
    INDEX idx_review (manual_review_needed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI输出审计日志表';
```

#### 1.1.4 What-if模拟记录表 (whatif_simulations)

```sql
-- What-if模拟记录表
CREATE TABLE whatif_simulations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    simulation_code VARCHAR(64) NOT NULL UNIQUE COMMENT '模拟编号',
    scenario_type VARCHAR(50) COMMENT '场景类型：headcount_change/salary_adjustment/org_restructure/batch_resign',
    scenario_name VARCHAR(100) COMMENT '场景名称',
    scenario_params JSON COMMENT '场景参数(JSON)',
    simulation_result JSON COMMENT '模拟结果(JSON)',
    impact_summary TEXT COMMENT '影响摘要',
    ai_analysis TEXT COMMENT 'AI分析',
    ai_recommendations JSON COMMENT 'AI建议(JSON数组)',
    created_by BIGINT COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant (tenant_id),
    INDEX idx_type (scenario_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='What-if模拟记录表';
```

**场景类型说明**：
- `headcount_change`: 编制变更模拟（增减岗位对成本/效率的影响）
- `salary_adjustment`: 薪资调整模拟（调薪方案对成本的影响）
- `org_restructure`: 组织重组模拟（部门合并/拆分的影响）
- `batch_resign`: 批量离职模拟（关键岗位离职的影响）

**scenario_params 示例（薪资调整）**：
```json
{
  "adjustment_type": "percentage",
  "percentage": 8,
  "target_scope": "all",
  "org_unit_ids": [1, 2, 3],
  "effective_date": "2025-04-01"
}
```

**simulation_result 示例**：
```json
{
  "affected_employees": 156,
  "current_monthly_cost": 2500000,
  "projected_monthly_cost": 2700000,
  "cost_increase": 200000,
  "cost_increase_percentage": 8,
  "annual_impact": 2400000,
  "budget_utilization": 95.5
}
```

#### 1.1.5 入职任务表 (onboarding_tasks) - 补充

```sql
-- 入职任务表（Phase 2 补充）
CREATE TABLE onboarding_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    onboarding_id BIGINT NOT NULL COMMENT '入职记录ID',
    task_type VARCHAR(50) COMMENT '任务类型：document/training/equipment/access/introduction',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    description TEXT COMMENT '任务描述',
    assignee_id BIGINT COMMENT '负责人ID',
    due_date DATE COMMENT '截止日期',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending/in_progress/completed/skipped',
    completed_at TIMESTAMP COMMENT '完成时间',
    remark TEXT COMMENT '备注',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_onboarding (onboarding_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入职任务表';
```

---

### 1.2 报表与分析模块（M09）- 2张表

#### 1.2.1 报表配置表 (report_configs)

```sql
-- 报表配置表
CREATE TABLE report_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    report_code VARCHAR(50) NOT NULL COMMENT '报表编码',
    report_name VARCHAR(100) NOT NULL COMMENT '报表名称',
    report_type VARCHAR(20) COMMENT '报表类型：table/chart/mixed',
    query_config JSON COMMENT '查询配置(JSON)',
    display_config JSON COMMENT '展示配置(JSON)',
    export_config JSON COMMENT '导出配置(JSON)',
    allowed_roles JSON COMMENT '允许访问的角色(JSON数组)',
    schedule_enabled TINYINT DEFAULT 0 COMMENT '是否启用定时发送',
    schedule_cron VARCHAR(50) COMMENT '定时表达式(CRON)',
    schedule_recipients JSON COMMENT '定时发送收件人(JSON数组)',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_code (tenant_id, report_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表配置表';
```

**query_config 结构示例**：
```json
{
  "data_source": "employees",
  "filters": [
    {"field": "employee_status", "operator": "in", "values": ["trial", "regular"]},
    {"field": "org_unit_id", "operator": "eq", "value": "${org_unit_id}"}
  ],
  "group_by": ["org_unit_id", "position_id"],
  "aggregations": [
    {"field": "id", "function": "count", "alias": "employee_count"},
    {"field": "base_salary", "function": "avg", "alias": "avg_salary"}
  ],
  "order_by": [{"field": "employee_count", "direction": "desc"}]
}
```

**预置报表**：
| 编码 | 名称 | 类型 |
|------|------|------|
| headcount_summary | 人员编制汇总 | table |
| turnover_analysis | 离职分析 | chart |
| attendance_summary | 考勤汇总 | table |
| salary_distribution | 薪资分布 | chart |
| performance_distribution | 绩效分布 | chart |
| recruitment_funnel | 招聘漏斗 | chart |
| ticket_statistics | 工单统计 | mixed |

#### 1.2.2 看板配置表 (dashboard_configs)

```sql
-- 看板配置表
CREATE TABLE dashboard_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    user_id BIGINT COMMENT '用户ID(null表示全局看板)',
    dashboard_code VARCHAR(50) COMMENT '看板编码',
    dashboard_name VARCHAR(100) COMMENT '看板名称',
    widgets JSON COMMENT '组件配置(JSON数组)',
    layout JSON COMMENT '布局配置(JSON)',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认看板',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant_user (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板配置表';
```

**widgets 结构示例**：
```json
[
  {
    "widget_id": "w1",
    "widget_type": "stat_card",
    "title": "在职员工数",
    "data_source": "employee_count",
    "config": {
      "show_trend": true,
      "trend_period": "month"
    }
  },
  {
    "widget_id": "w2",
    "widget_type": "pie_chart",
    "title": "部门人员分布",
    "data_source": "employee_by_dept",
    "config": {
      "show_legend": true
    }
  },
  {
    "widget_id": "w3",
    "widget_type": "line_chart",
    "title": "月度入离职趋势",
    "data_source": "turnover_trend",
    "config": {
      "time_range": "12_months"
    }
  }
]
```

**layout 结构**：
```json
{
  "columns": 3,
  "rows": [
    {"widgets": ["w1", "w2", "w3"], "height": 200},
    {"widgets": ["w4"], "height": 300}
  ]
}
```

---

## 2. 后端包结构

```
com.hrai.agent/
├── ai/                               # M10 AI能力中心
│   ├── controller/
│   │   ├── AIChatController.java         # AI对话助手
│   │   ├── AIInsightController.java      # AI洞察
│   │   ├── AISuggestionController.java   # AI建议池
│   │   ├── AIActionController.java       # AI行动
│   │   ├── AIAuditController.java        # AI审计
│   │   └── WhatIfController.java         # What-if模拟
│   │
│   ├── service/
│   │   ├── chat/
│   │   │   ├── ChatService.java
│   │   │   ├── StreamingChatService.java     # SSE流式对话
│   │   │   └── ChatHistoryService.java       # 对话历史
│   │   ├── insight/
│   │   │   ├── TeamOverviewService.java      # AI-SC-003 团队概览
│   │   │   ├── StabilityAnalysisService.java # AI-SC-004 稳定性分析
│   │   │   ├── OrgHealthService.java         # AI-SC-005 组织健康度
│   │   │   ├── HeadcountAnalysisService.java # AI-SC-006 编制合理性
│   │   │   └── TurnoverRiskService.java      # AI-SC-009 离职风险
│   │   ├── suggestion/
│   │   │   ├── SuggestionPoolService.java    # AI-SC-007 建议池
│   │   │   ├── SuggestionGeneratorService.java
│   │   │   └── WhatIfSimulationService.java  # AI-SC-010 What-if
│   │   ├── action/
│   │   │   ├── AIActionService.java
│   │   │   └── AIActionExecutor.java         # 行动执行器
│   │   └── audit/
│   │       └── AIOutputAuditService.java     # AI-SC-008 输出留痕
│   │
│   ├── agents/
│   │   ├── base/
│   │   │   ├── AbstractAgent.java
│   │   │   ├── AgentContext.java
│   │   │   └── AgentResponse.java
│   │   ├── impl/
│   │   │   ├── HRPolicyAgent.java            # AI-SC-002 政策问答
│   │   │   ├── AnalyticsAgent.java           # AI-SC-001 自然语言查询
│   │   │   ├── PayrollExplainerAgent.java    # AI-SC-011 薪资解释
│   │   │   └── PerformanceCommentAgent.java  # AI-SC-012 绩效评语
│   │   └── router/
│   │       └── AgentRouter.java              # Agent路由器
│   │
│   ├── rag/
│   │   ├── EmbeddingService.java             # 向量化服务
│   │   ├── VectorStoreService.java           # 向量存储
│   │   └── RAGService.java                   # RAG检索
│   │
│   ├── memory/
│   │   ├── ShortTermMemory.java              # Redis短期记忆
│   │   └── LongTermMemory.java               # Milvus长期记忆
│   │
│   ├── tools/
│   │   ├── FunctionRegistry.java             # Function注册中心
│   │   └── impl/
│   │       ├── SearchKnowledgeFunction.java  # 搜索知识库
│   │       ├── QueryEmployeeFunction.java    # 查询员工
│   │       ├── QueryAttendanceFunction.java  # 查询考勤
│   │       ├── QueryPayrollFunction.java     # 查询薪资
│   │       ├── QueryPerformanceFunction.java # 查询绩效
│   │       ├── CreateTicketFunction.java     # 创建工单
│   │       └── AnalyzeDataFunction.java      # 数据分析
│   │
│   ├── mapper/
│   │   ├── AISuggestionMapper.java
│   │   ├── AIActionMapper.java
│   │   ├── AIAuditLogMapper.java
│   │   └── WhatIfSimulationMapper.java
│   │
│   ├── entity/
│   │   ├── AISuggestion.java
│   │   ├── AIAction.java
│   │   ├── AIAuditLog.java
│   │   └── WhatIfSimulation.java
│   │
│   └── dto/
│       ├── ChatRequestDTO.java
│       ├── ChatResponseDTO.java
│       ├── InsightDTO.java
│       ├── SuggestionDTO.java
│       ├── ActionDTO.java
│       └── WhatIfRequestDTO.java
│
├── report/                           # M09 报表与分析
│   ├── controller/
│   │   ├── ReportController.java         # 报表接口
│   │   └── DashboardController.java      # 看板接口
│   ├── service/
│   │   ├── ReportConfigService.java
│   │   ├── ReportDataService.java
│   │   ├── DashboardService.java
│   │   ├── reports/
│   │   │   ├── HeadcountReportService.java
│   │   │   ├── TurnoverReportService.java
│   │   │   ├── AttendanceReportService.java
│   │   │   ├── PayrollReportService.java
│   │   │   └── PerformanceReportService.java
│   │   └── export/
│   │       ├── ExcelExporter.java
│   │       └── PdfExporter.java
│   ├── mapper/
│   │   ├── ReportConfigMapper.java
│   │   └── DashboardConfigMapper.java
│   ├── entity/
│   │   ├── ReportConfig.java
│   │   └── DashboardConfig.java
│   └── dto/
│       ├── ReportConfigDTO.java
│       ├── ReportDataDTO.java
│       ├── DashboardDTO.java
│       └── WidgetDataDTO.java
```

---

## 3. AI 场景详细设计

### 3.1 AI-SC-001 自然语言查询 (AnalyticsAgent)

**功能**：用户用自然语言提问数据相关问题，AI自动转换为查询并返回结果。

**示例问答**：
- Q: "上个月离职了多少人？"
- Q: "研发部门的平均工资是多少？"
- Q: "谁的绩效评分最高？"

**实现**：
```java
@Service
public class AnalyticsAgent extends AbstractAgent {

    @Autowired
    private QueryEmployeeFunction queryEmployeeFunction;

    @Autowired
    private QueryPayrollFunction queryPayrollFunction;

    @Override
    public AgentResponse execute(AgentContext context) {
        // 1. 理解用户意图
        String intent = parseIntent(context.getQuestion());

        // 2. 选择合适的Function
        Function function = selectFunction(intent);

        // 3. 提取参数
        Map<String, Object> params = extractParams(context.getQuestion(), function);

        // 4. 执行查询
        Object result = function.execute(params);

        // 5. 生成自然语言回复
        String answer = generateAnswer(context.getQuestion(), result);

        return AgentResponse.builder()
            .answer(answer)
            .data(result)
            .build();
    }
}
```

### 3.2 AI-SC-002 政策问答 (HRPolicyAgent)

**功能**：基于RAG检索知识库，回答HR政策相关问题。

**实现**：
```java
@Service
public class HRPolicyAgent extends AbstractAgent {

    @Autowired
    private RAGService ragService;

    @Override
    public AgentResponse execute(AgentContext context) {
        // 1. 检索相关文档
        List<Document> docs = ragService.retrieve(
            context.getTenantId(),
            context.getQuestion(),
            5  // Top-5
        );

        // 2. 构建Prompt
        String prompt = buildPromptWithContext(context.getQuestion(), docs);

        // 3. 调用LLM
        String answer = chatClient.call(prompt);

        // 4. 提取引用
        List<Citation> citations = extractCitations(answer, docs);

        return AgentResponse.builder()
            .answer(answer)
            .citations(citations)
            .build();
    }
}
```

### 3.3 AI-SC-003 团队概览 (TeamOverviewService)

**功能**：为管理者生成团队的综合概览报告。

**输出内容**：
- 团队人员构成（职级分布、司龄分布）
- 近期人员变动（入职、离职、调岗）
- 考勤异常
- 待处理事项
- AI洞察与建议

```java
@Service
public class TeamOverviewService {

    public TeamOverviewDTO generateOverview(Long managerId, Long orgUnitId) {
        TeamOverviewDTO overview = new TeamOverviewDTO();

        // 1. 人员构成
        overview.setHeadcount(getHeadcountStats(orgUnitId));
        overview.setLevelDistribution(getLevelDistribution(orgUnitId));
        overview.setTenureDistribution(getTenureDistribution(orgUnitId));

        // 2. 近期变动
        overview.setRecentJoins(getRecentJoins(orgUnitId, 30));
        overview.setRecentResigns(getRecentResigns(orgUnitId, 30));
        overview.setRecentTransfers(getRecentTransfers(orgUnitId, 30));

        // 3. 考勤异常
        overview.setAttendanceAlerts(getAttendanceAlerts(orgUnitId));

        // 4. 待处理事项
        overview.setPendingApprovals(getPendingApprovals(managerId));

        // 5. AI洞察
        overview.setAiInsights(generateAiInsights(overview));

        return overview;
    }
}
```

### 3.4 AI-SC-009 离职风险预测 (TurnoverRiskService)

**功能**：基于多维度数据预测员工离职风险。

**风险因素**：
- 工作年限（1年以下、3年、5年节点）
- 薪资竞争力（与市场/内部对比）
- 绩效趋势（连续下降）
- 考勤异常（请假增多）
- 工单投诉
- 司龄-职级匹配度

```java
@Service
public class TurnoverRiskService {

    public List<TurnoverRiskDTO> predictRisk(Long orgUnitId) {
        List<Employee> employees = employeeService.getByOrgUnit(orgUnitId);
        List<TurnoverRiskDTO> risks = new ArrayList<>();

        for (Employee emp : employees) {
            TurnoverRiskDTO risk = new TurnoverRiskDTO();
            risk.setEmployeeId(emp.getId());
            risk.setEmployeeName(emp.getRealName());

            // 计算各维度风险分数
            double tenureScore = calcTenureRiskScore(emp);
            double salaryScore = calcSalaryRiskScore(emp);
            double performanceScore = calcPerformanceRiskScore(emp);
            double attendanceScore = calcAttendanceRiskScore(emp);

            // 综合风险分数（加权平均）
            double totalScore = tenureScore * 0.2 +
                               salaryScore * 0.3 +
                               performanceScore * 0.3 +
                               attendanceScore * 0.2;

            risk.setRiskScore(totalScore);
            risk.setRiskLevel(determineLevel(totalScore));
            risk.setRiskFactors(identifyFactors(emp, tenureScore, salaryScore,
                                               performanceScore, attendanceScore));

            if (totalScore > 0.6) {
                risks.add(risk);
            }
        }

        // 按风险分数排序
        risks.sort((a, b) -> Double.compare(b.getRiskScore(), a.getRiskScore()));

        return risks;
    }

    private String determineLevel(double score) {
        if (score > 0.8) return "high";
        if (score > 0.6) return "medium";
        return "low";
    }
}
```

### 3.5 AI-SC-010 What-if模拟 (WhatIfSimulationService)

**功能**：模拟各种场景下的影响分析。

```java
@Service
public class WhatIfSimulationService {

    /**
     * 薪资调整模拟
     */
    public WhatIfResultDTO simulateSalaryAdjustment(SalaryAdjustmentScenario scenario) {
        WhatIfResultDTO result = new WhatIfResultDTO();

        // 1. 获取受影响员工
        List<Employee> affected = getAffectedEmployees(scenario);
        result.setAffectedCount(affected.size());

        // 2. 计算当前成本
        BigDecimal currentCost = calcCurrentMonthlyCost(affected);
        result.setCurrentMonthlyCost(currentCost);

        // 3. 计算调整后成本
        BigDecimal projectedCost = calcProjectedCost(affected, scenario);
        result.setProjectedMonthlyCost(projectedCost);

        // 4. 计算影响
        result.setCostIncrease(projectedCost.subtract(currentCost));
        result.setCostIncreasePercentage(
            result.getCostIncrease().divide(currentCost, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
        );
        result.setAnnualImpact(result.getCostIncrease().multiply(new BigDecimal(12)));

        // 5. AI分析
        result.setAiAnalysis(generateAiAnalysis(scenario, result));
        result.setAiRecommendations(generateRecommendations(scenario, result));

        return result;
    }

    /**
     * 批量离职模拟
     */
    public WhatIfResultDTO simulateBatchResign(BatchResignScenario scenario) {
        // 计算招聘成本、培训成本、生产力损失等
        // ...
    }
}
```

### 3.6 AI-SC-011 薪资解释 (PayrollExplainerAgent)

**功能**：用自然语言解释员工的工资条明细。

```java
@Service
public class PayrollExplainerAgent extends AbstractAgent {

    @Override
    public AgentResponse execute(AgentContext context) {
        // 1. 获取员工薪资数据
        PayrollDetail detail = payrollService.getLatestDetail(context.getUserId());

        // 2. 构建解释Prompt
        String prompt = """
            请用通俗易懂的语言解释以下工资明细：

            薪资周期：%s
            应发工资：%s
            - 基本工资：%s
            - 岗位工资：%s
            - 绩效工资：%s
            - 加班费：%s
            - 餐补：%s
            - 交通补贴：%s

            扣除项：
            - 社保个人部分：%s
            - 公积金个人部分：%s
            - 个人所得税：%s

            实发工资：%s

            请解释：
            1. 各项收入的含义
            2. 社保公积金的计算方式
            3. 个税的计算方式（累计预扣法）
            4. 如有异常，指出可能的原因

            用户问题：%s
            """.formatted(
                detail.getPayPeriod(),
                detail.getGrossIncome(),
                // ... 其他参数
                context.getQuestion()
            );

        return AgentResponse.builder()
            .answer(chatClient.call(prompt))
            .data(detail)
            .build();
    }
}
```

---

## 4. API 接口设计

### 4.1 AI对话接口 (AIChatController)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/ai/chat | 发送消息（同步） |
| GET | /api/ai/chat/stream | SSE流式对话 |
| GET | /api/ai/chat/history | 获取对话历史 |
| DELETE | /api/ai/chat/history | 清除对话历史 |
| POST | /api/ai/chat/feedback | 提交反馈 |

### 4.2 AI洞察接口 (AIInsightController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/ai/insights/team-overview | 团队概览 |
| GET | /api/ai/insights/stability | 稳定性分析 |
| GET | /api/ai/insights/org-health | 组织健康度 |
| GET | /api/ai/insights/headcount | 编制分析 |
| GET | /api/ai/insights/turnover-risk | 离职风险 |

### 4.3 AI建议接口 (AISuggestionController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/ai/suggestions | 建议列表 |
| GET | /api/ai/suggestions/{id} | 建议详情 |
| POST | /api/ai/suggestions/{id}/review | 审核建议 |
| POST | /api/ai/suggestions/{id}/adopt | 采纳建议 |
| POST | /api/ai/suggestions/{id}/dismiss | 忽略建议 |

### 4.4 AI行动接口 (AIActionController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/ai/actions | 行动列表 |
| GET | /api/ai/actions/{id} | 行动详情 |
| POST | /api/ai/actions/{id}/execute | 执行行动 |
| POST | /api/ai/actions/{id}/cancel | 取消行动 |

### 4.5 What-if模拟接口 (WhatIfController)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/ai/whatif/salary | 薪资调整模拟 |
| POST | /api/ai/whatif/headcount | 编制变更模拟 |
| POST | /api/ai/whatif/resign | 批量离职模拟 |
| GET | /api/ai/whatif/history | 模拟历史 |
| GET | /api/ai/whatif/{id} | 模拟详情 |

### 4.6 AI审计接口 (AIAuditController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/ai/audit/logs | 审计日志列表 |
| GET | /api/ai/audit/logs/{id} | 日志详情 |
| POST | /api/ai/audit/logs/{id}/mark-review | 标记需审核 |
| GET | /api/ai/audit/statistics | 统计数据 |

### 4.7 报表接口 (ReportController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/reports | 报表列表 |
| GET | /api/reports/{code} | 报表详情 |
| GET | /api/reports/{code}/data | 获取报表数据 |
| GET | /api/reports/{code}/export | 导出报表 |
| POST | /api/reports | 创建报表配置 |
| PUT | /api/reports/{code} | 更新报表配置 |

### 4.8 看板接口 (DashboardController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/dashboards | 看板列表 |
| GET | /api/dashboards/{id} | 看板详情 |
| GET | /api/dashboards/{id}/data | 看板数据 |
| POST | /api/dashboards | 创建看板 |
| PUT | /api/dashboards/{id} | 更新看板 |
| DELETE | /api/dashboards/{id} | 删除看板 |
| PUT | /api/dashboards/{id}/default | 设为默认 |

---

## 5. 前端页面设计（16个）

### 5.1 AI能力中心模块（9个页面）

| 需求ID | 页面 | 路径 | 说明 |
|--------|------|------|------|
| RQ0062 | AI助手 | /ai/chat | AI对话界面 |
| RQ0063 | 团队洞察 | /ai/insights/team | 团队概览 |
| RQ0064 | 离职风险 | /ai/insights/turnover | 离职风险预测 |
| RQ0065 | 组织健康 | /ai/insights/org-health | 组织健康度分析 |
| RQ0066 | 建议池 | /ai/suggestions | AI建议管理 |
| RQ0067 | What-if模拟 | /ai/whatif | 模拟分析工具 |
| RQ0068 | AI审计 | /ai/audit | 输出审计日志 |
| RQ0069 | 行动管理 | /ai/actions | AI行动跟踪 |
| RQ0070 | AI设置 | /ai/settings | AI配置管理 |

### 5.2 报表与分析模块（7个页面）

| 需求ID | 页面 | 路径 | 说明 |
|--------|------|------|------|
| RQ0071 | 报表中心 | /reports | 报表列表 |
| RQ0072 | 报表查看 | /reports/:code | 报表详情 |
| RQ0073 | 报表配置 | /reports/:code/config | 报表编辑 |
| RQ0074 | 看板中心 | /dashboards | 看板列表 |
| RQ0075 | 看板查看 | /dashboards/:id | 看板展示 |
| RQ0076 | 看板编辑 | /dashboards/:id/edit | 看板配置 |
| RQ0077 | HR驾驶舱 | /dashboards/hr | HR综合看板 |

---

## 6. 前端组件设计

### 6.1 AI组件

```
src/components/ai/
├── ChatPanel/                    # 对话面板
│   ├── index.tsx
│   ├── MessageList.tsx           # 消息列表
│   ├── MessageBubble.tsx         # 消息气泡
│   ├── ChatInput.tsx             # 输入框
│   └── CitationList.tsx          # 引用列表
│
├── InsightCard/                  # 洞察卡片
│   ├── index.tsx
│   ├── TeamOverviewCard.tsx
│   ├── RiskAlertCard.tsx
│   └── TrendCard.tsx
│
├── SuggestionCard/               # 建议卡片
│   ├── index.tsx
│   ├── ActionButtons.tsx
│   └── ImpactBadge.tsx
│
├── WhatIfSimulator/              # 模拟器
│   ├── index.tsx
│   ├── ScenarioForm.tsx
│   └── ResultDisplay.tsx
│
└── StreamingText/                # 流式文本
    └── index.tsx
```

### 6.2 报表组件

```
src/components/report/
├── ChartWidgets/                 # 图表组件
│   ├── LineChart.tsx
│   ├── BarChart.tsx
│   ├── PieChart.tsx
│   └── StatCard.tsx
│
├── DataTable/                    # 数据表格
│   ├── index.tsx
│   └── ExportButton.tsx
│
├── DashboardEditor/              # 看板编辑器
│   ├── index.tsx
│   ├── WidgetPicker.tsx
│   └── LayoutEditor.tsx
│
└── FilterPanel/                  # 筛选面板
    └── index.tsx
```

---

## 7. 验证标准

### 7.1 功能验证

- [ ] AI对话流式响应正常
- [ ] RAG检索结果相关性>80%
- [ ] 政策问答准确率>85%
- [ ] 自然语言查询转换正确
- [ ] 团队概览数据准确
- [ ] 离职风险预测合理
- [ ] AI建议生成质量可接受
- [ ] 建议采纳后行动执行正确
- [ ] What-if模拟计算准确
- [ ] AI审计日志完整
- [ ] 报表数据准确
- [ ] 看板配置保存正常
- [ ] 导出功能正常

### 7.2 性能验证

- [ ] 对话首Token延迟 < 3s
- [ ] RAG检索 < 500ms
- [ ] 洞察生成 < 5s
- [ ] 报表查询 < 2s
- [ ] 看板加载 < 3s

### 7.3 安全验证

- [ ] 敏感数据脱敏
- [ ] 数据权限控制
- [ ] AI输出审核机制

---

## 8. 交付物清单

| 类型 | 数量 | 说明 |
|------|------|------|
| 数据库表 | 7张 | ai_suggestions, report_configs 等 |
| Entity | 7个 | 对应数据库表 |
| Mapper | 7个 | MyBatis Plus Mapper |
| Service | 15个 | 业务服务类 |
| Controller | 8个 | REST接口 |
| AI Agent | 4个 | 政策问答/查询/薪资解释/绩效评语 |
| AI Service | 6个 | 洞察/建议/模拟等 |
| Function | 7个 | AI工具函数 |
| 前端页面 | 16个 | React 页面组件 |
| 前端组件 | 15个 | 可复用组件 |

---

*Phase 5 完成后，系统将具备完整的AI能力，包括智能对话、数据洞察、决策建议和报表分析。*
