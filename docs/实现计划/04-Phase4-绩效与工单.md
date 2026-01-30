# Phase 4: 绩效与员工关系

> 绩效管理（M07）+ 工单系统（M08）

## 概述

Phase 4 实现绩效管理和员工工单系统，包括：
- 绩效周期管理
- 目标设定与跟踪
- 绩效评估与校准
- 员工工单提交与处理
- 知识库管理

---

## 1. 数据库表设计（8张表）

### 1.1 绩效管理模块（M07）- 4张表

#### 1.1.1 绩效周期表 (performance_cycles)

```sql
-- 绩效周期表
CREATE TABLE performance_cycles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    cycle_code VARCHAR(50) NOT NULL COMMENT '周期编码',
    cycle_name VARCHAR(100) NOT NULL COMMENT '周期名称',
    cycle_type VARCHAR(20) COMMENT '周期类型：annual-年度/semi_annual-半年/quarterly-季度',
    start_date DATE COMMENT '周期开始日期',
    end_date DATE COMMENT '周期结束日期',
    goal_setting_start DATE COMMENT '目标设定开始日期',
    goal_setting_end DATE COMMENT '目标设定结束日期',
    self_review_start DATE COMMENT '自评开始日期',
    self_review_end DATE COMMENT '自评结束日期',
    manager_review_start DATE COMMENT '上级评估开始日期',
    manager_review_end DATE COMMENT '上级评估结束日期',
    calibration_start DATE COMMENT '校准开始日期',
    calibration_end DATE COMMENT '校准结束日期',
    status VARCHAR(20) DEFAULT 'not_started' COMMENT '状态：not_started/goal_setting/in_progress/self_review/manager_review/calibration/completed',
    org_unit_ids JSON COMMENT '参与部门ID列表(JSON数组)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_code (tenant_id, cycle_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效周期表';
```

**状态枚举及流程**：
- `not_started`: 未开始
- `goal_setting`: 目标设定阶段
- `in_progress`: 执行中（目标追踪）
- `self_review`: 员工自评阶段
- `manager_review`: 上级评估阶段
- `calibration`: 绩效校准阶段
- `completed`: 已完成

#### 1.1.2 绩效目标表 (performance_goals)

```sql
-- 绩效目标表
CREATE TABLE performance_goals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    cycle_id BIGINT NOT NULL COMMENT '绩效周期ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    goal_type VARCHAR(20) COMMENT '目标类型：business-业务目标/development-发展目标/key_result-关键结果',
    goal_title VARCHAR(200) NOT NULL COMMENT '目标标题',
    description TEXT COMMENT '目标描述',
    target_value VARCHAR(100) COMMENT '目标值',
    unit VARCHAR(20) COMMENT '单位',
    weight DECIMAL(5,2) COMMENT '权重(%)',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/pending/approved/rejected',
    approver_id BIGINT COMMENT '审批人ID（上级）',
    approve_time TIMESTAMP COMMENT '审批时间',
    current_value VARCHAR(100) COMMENT '当前完成值',
    progress_percent INT DEFAULT 0 COMMENT '完成进度(%)',
    parent_goal_id BIGINT COMMENT '父目标ID（支持目标分解）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_cycle_employee (cycle_id, employee_id),
    INDEX idx_parent_goal (parent_goal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效目标表';
```

**目标类型说明**：
- `business`: 业务目标（如销售额、客户满意度）
- `development`: 发展目标（如学习、技能提升）
- `key_result`: 关键结果（OKR模式下使用）

#### 1.1.3 绩效评估表 (performance_reviews)

```sql
-- 绩效评估表
CREATE TABLE performance_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    cycle_id BIGINT NOT NULL COMMENT '绩效周期ID',
    employee_id BIGINT NOT NULL COMMENT '被评估员工ID',
    reviewer_id BIGINT NOT NULL COMMENT '评估人ID',
    review_type VARCHAR(20) COMMENT '评估类型：self-自评/manager-上级评估/peer-同级评估',
    overall_score DECIMAL(4,2) COMMENT '总体评分(0-5)',
    goal_score DECIMAL(4,2) COMMENT '目标完成评分',
    competency_score DECIMAL(4,2) COMMENT '能力评分',
    achievements TEXT COMMENT '主要成就',
    improvements TEXT COMMENT '待改进领域',
    comments TEXT COMMENT '详细评语',
    ai_generated TINYINT DEFAULT 0 COMMENT '是否AI生成',
    ai_suggestions JSON COMMENT 'AI建议(JSON)',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/submitted',
    submit_time TIMESTAMP COMMENT '提交时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_cycle_employee (cycle_id, employee_id),
    INDEX idx_reviewer (reviewer_id),
    UNIQUE KEY uk_cycle_employee_reviewer (cycle_id, employee_id, reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效评估表';
```

#### 1.1.4 绩效结果表 (performance_results)

```sql
-- 绩效结果表
CREATE TABLE performance_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    cycle_id BIGINT NOT NULL COMMENT '绩效周期ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    final_score DECIMAL(4,2) COMMENT '最终评分',
    final_rating VARCHAR(20) COMMENT '最终等级：A/B/C/D/E 或 exceeds/meets/below',
    forced_distribution VARCHAR(20) COMMENT '强制分布等级',
    original_score DECIMAL(4,2) COMMENT '原始评分（校准前）',
    calibrated TINYINT DEFAULT 0 COMMENT '是否经过校准',
    calibration_reason TEXT COMMENT '校准原因',
    calibrator_id BIGINT COMMENT '校准人ID',
    salary_adjust_suggestion DECIMAL(5,2) COMMENT '建议调薪比例(%)',
    bonus_coefficient DECIMAL(3,2) COMMENT '奖金系数',
    promotion_suggestion TINYINT DEFAULT 0 COMMENT '是否建议晋升',
    confirmed TINYINT DEFAULT 0 COMMENT '员工是否确认',
    confirm_time TIMESTAMP COMMENT '确认时间',
    employee_feedback TEXT COMMENT '员工反馈',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_cycle_employee (cycle_id, employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效结果表';
```

**绩效等级与强制分布示例**：
| 等级 | 描述 | 强制分布比例 |
|------|------|--------------|
| A/Exceeds | 卓越 | 10% |
| B/Meets+ | 优秀 | 25% |
| C/Meets | 合格 | 45% |
| D/Below | 待改进 | 15% |
| E/Poor | 不合格 | 5% |

---

### 1.2 工单系统模块（M08）- 4张表

#### 1.2.1 工单类型表 (ticket_types)

```sql
-- 工单类型表
CREATE TABLE ticket_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    type_code VARCHAR(50) NOT NULL COMMENT '类型编码',
    type_name VARCHAR(100) NOT NULL COMMENT '类型名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父类型ID(支持多级分类)',
    description TEXT COMMENT '类型描述',
    default_assignee_id BIGINT COMMENT '默认处理人ID',
    default_team VARCHAR(50) COMMENT '默认处理团队',
    sla_hours INT COMMENT 'SLA时效(小时)',
    auto_route TINYINT DEFAULT 0 COMMENT '是否自动路由',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_code (tenant_id, type_code),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单类型表';
```

**预置工单类型**：
- 人事类
  - 证明开具（在职证明、收入证明）
  - 档案查询
  - 信息变更
- 薪酬类
  - 工资查询
  - 社保公积金
  - 报销问题
- 假期类
  - 假期查询
  - 请假问题
- 系统类
  - 账号问题
  - 系统故障
- 其他

#### 1.2.2 工单表 (tickets)

```sql
-- 工单表
CREATE TABLE tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    ticket_code VARCHAR(64) NOT NULL UNIQUE COMMENT '工单编号',
    ticket_type_id BIGINT NOT NULL COMMENT '工单类型ID',
    title VARCHAR(200) NOT NULL COMMENT '工单标题',
    description TEXT COMMENT '问题描述',
    submitter_id BIGINT NOT NULL COMMENT '提交人ID',
    submitter_name VARCHAR(50) COMMENT '提交人姓名',
    submit_channel VARCHAR(20) COMMENT '提交渠道：web/app/chat/email',
    assignee_id BIGINT COMMENT '处理人ID',
    team VARCHAR(50) COMMENT '处理团队',
    status VARCHAR(20) DEFAULT 'new' COMMENT '状态：new/assigned/in_progress/pending_reply/resolved/closed/reopened',
    priority VARCHAR(10) DEFAULT 'normal' COMMENT '优先级：urgent/high/normal/low',
    sla_deadline TIMESTAMP COMMENT 'SLA截止时间',
    sla_breached TINYINT DEFAULT 0 COMMENT 'SLA是否超时',
    first_response_time TIMESTAMP COMMENT '首次响应时间',
    resolved_time TIMESTAMP COMMENT '解决时间',
    closed_time TIMESTAMP COMMENT '关闭时间',
    ai_routed TINYINT DEFAULT 0 COMMENT '是否AI路由',
    ai_suggested_answer TEXT COMMENT 'AI建议答案',
    satisfaction_score INT COMMENT '满意度评分(1-5)',
    satisfaction_comment TEXT COMMENT '满意度评价',
    related_ticket_ids JSON COMMENT '关联工单ID(JSON数组)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_assignee (assignee_id),
    INDEX idx_submitter (submitter_id),
    INDEX idx_sla_deadline (sla_deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';
```

**状态枚举**：
- `new`: 新建
- `assigned`: 已分配
- `in_progress`: 处理中
- `pending_reply`: 待回复（等待用户补充）
- `resolved`: 已解决
- `closed`: 已关闭
- `reopened`: 重新打开

#### 1.2.3 工单评论表 (ticket_comments)

```sql
-- 工单评论表
CREATE TABLE ticket_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ticket_id BIGINT NOT NULL COMMENT '工单ID',
    commenter_id BIGINT NOT NULL COMMENT '评论人ID',
    commenter_name VARCHAR(50) COMMENT '评论人姓名',
    content TEXT NOT NULL COMMENT '评论内容',
    is_internal TINYINT DEFAULT 0 COMMENT '是否内部备注(用户不可见)',
    attachments JSON COMMENT '附件列表(JSON数组)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ticket (ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单评论表';
```

#### 1.2.4 知识库文章表 (knowledge_articles)

```sql
-- 知识库文章表
CREATE TABLE knowledge_articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    article_code VARCHAR(64) NOT NULL UNIQUE COMMENT '文章编号',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    summary TEXT COMMENT '摘要',
    content LONGTEXT NOT NULL COMMENT '文章内容(Markdown)',
    category VARCHAR(100) COMMENT '分类',
    tags JSON COMMENT '标签(JSON数组)',
    visibility VARCHAR(20) DEFAULT 'all' COMMENT '可见性：all-全员/hr_only-仅HR/roles-指定角色',
    allowed_roles JSON COMMENT '允许查看的角色(JSON数组)',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    helpful_count INT DEFAULT 0 COMMENT '有帮助数',
    not_helpful_count INT DEFAULT 0 COMMENT '无帮助数',
    version INT DEFAULT 1 COMMENT '版本号',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/published/archived',
    published_at TIMESTAMP COMMENT '发布时间',
    author_id BIGINT COMMENT '作者ID',
    vector_indexed TINYINT DEFAULT 0 COMMENT '是否已向量化',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_category (tenant_id, category),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文章表';
```

---

## 2. 后端包结构

```
com.hrai.agent/
├── performance/                      # M07 绩效管理
│   ├── controller/
│   │   ├── PerformanceCycleController.java   # 绩效周期
│   │   ├── PerformanceGoalController.java    # 绩效目标
│   │   ├── PerformanceReviewController.java  # 绩效评估
│   │   └── PerformanceResultController.java  # 绩效结果
│   ├── service/
│   │   ├── PerformanceCycleService.java
│   │   ├── PerformanceGoalService.java
│   │   ├── PerformanceReviewService.java
│   │   ├── PerformanceResultService.java
│   │   └── CalibrationService.java           # 绩效校准
│   ├── statemachine/
│   │   └── PerformanceCycleStateMachine.java
│   ├── ai/
│   │   └── PerformanceCommentGenerator.java  # AI绩效评语
│   ├── mapper/
│   │   ├── PerformanceCycleMapper.java
│   │   ├── PerformanceGoalMapper.java
│   │   ├── PerformanceReviewMapper.java
│   │   └── PerformanceResultMapper.java
│   ├── entity/
│   │   ├── PerformanceCycle.java
│   │   ├── PerformanceGoal.java
│   │   ├── PerformanceReview.java
│   │   └── PerformanceResult.java
│   └── dto/
│       ├── PerformanceCycleDTO.java
│       ├── PerformanceGoalDTO.java
│       ├── PerformanceReviewDTO.java
│       ├── PerformanceResultDTO.java
│       └── CalibrationDTO.java
│
├── ticket/                           # M08 工单系统
│   ├── controller/
│   │   ├── TicketTypeController.java         # 工单类型
│   │   ├── TicketController.java             # 工单管理
│   │   └── KnowledgeArticleController.java   # 知识库
│   ├── service/
│   │   ├── TicketTypeService.java
│   │   ├── TicketService.java
│   │   ├── TicketCommentService.java
│   │   ├── TicketRoutingService.java         # 工单路由
│   │   └── KnowledgeArticleService.java
│   ├── statemachine/
│   │   └── TicketStateMachine.java
│   ├── ai/
│   │   ├── TicketRouterAgent.java            # AI工单路由
│   │   └── TicketAnswerAgent.java            # AI答案建议
│   ├── mapper/
│   │   ├── TicketTypeMapper.java
│   │   ├── TicketMapper.java
│   │   ├── TicketCommentMapper.java
│   │   └── KnowledgeArticleMapper.java
│   ├── entity/
│   │   ├── TicketType.java
│   │   ├── Ticket.java
│   │   ├── TicketComment.java
│   │   └── KnowledgeArticle.java
│   └── dto/
│       ├── TicketTypeDTO.java
│       ├── TicketDTO.java
│       ├── TicketCreateDTO.java
│       ├── TicketCommentDTO.java
│       └── KnowledgeArticleDTO.java
```

---

## 3. 状态机设计

### 3.1 绩效周期状态机 (PerformanceCycleStateMachine)

```
状态流转：
    ┌───────────────┐
    │  not_started  │
    │   (未开始)    │
    └───────┬───────┘
            │ START_GOAL_SETTING
            ▼
    ┌───────────────┐
    │ goal_setting  │
    │ (目标设定)    │
    └───────┬───────┘
            │ START_EXECUTION
            ▼
    ┌───────────────┐
    │  in_progress  │
    │  (执行中)     │
    └───────┬───────┘
            │ START_SELF_REVIEW
            ▼
    ┌───────────────┐
    │  self_review  │
    │  (员工自评)   │
    └───────┬───────┘
            │ START_MANAGER_REVIEW
            ▼
    ┌────────────────┐
    │ manager_review │
    │  (上级评估)    │
    └───────┬────────┘
            │ START_CALIBRATION
            ▼
    ┌───────────────┐
    │  calibration  │
    │  (绩效校准)   │
    └───────┬───────┘
            │ COMPLETE
            ▼
    ┌───────────────┐
    │   completed   │
    │   (已完成)    │
    └───────────────┘
```

### 3.2 工单状态机 (TicketStateMachine)

```
状态流转：
    ┌─────────────┐
    │    new      │ ←──────────┐
    │  (新建)     │            │
    └──────┬──────┘            │
           │ ASSIGN            │ REOPEN
           ▼                   │
    ┌─────────────┐            │
    │  assigned   │            │
    │  (已分配)   │            │
    └──────┬──────┘            │
           │ START_PROCESS     │
           ▼                   │
    ┌─────────────┐            │
    │ in_progress │────────────┤
    │  (处理中)   │            │
    └──────┬──────┘            │
           │                   │
    ┌──────┼──────────┐        │
    │      │          │        │
    │ ASK_USER    RESOLVE      │
    ▼      │          │        │
┌──────────┴──┐       │        │
│pending_reply│       │        │
│ (待回复)    │       │        │
└──────┬──────┘       │        │
       │ USER_REPLY   │        │
       ▼              ▼        │
    ┌─────────────────────┐    │
    │      resolved       │    │
    │      (已解决)       │    │
    └──────────┬──────────┘    │
               │ CLOSE         │
               ▼               │
    ┌─────────────────────┐    │
    │       closed        │────┘
    │      (已关闭)       │
    └─────────────────────┘
```

---

## 4. API 接口设计

### 4.1 绩效周期接口 (PerformanceCycleController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/performance/cycles | 周期列表 |
| GET | /api/performance/cycles/{id} | 周期详情 |
| POST | /api/performance/cycles | 创建周期 |
| PUT | /api/performance/cycles/{id} | 更新周期 |
| POST | /api/performance/cycles/{id}/start-goal-setting | 开始目标设定 |
| POST | /api/performance/cycles/{id}/start-execution | 开始执行 |
| POST | /api/performance/cycles/{id}/start-self-review | 开始自评 |
| POST | /api/performance/cycles/{id}/start-manager-review | 开始上级评估 |
| POST | /api/performance/cycles/{id}/start-calibration | 开始校准 |
| POST | /api/performance/cycles/{id}/complete | 完成周期 |
| GET | /api/performance/cycles/{id}/statistics | 周期统计 |

### 4.2 绩效目标接口 (PerformanceGoalController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/performance/goals | 目标列表 |
| GET | /api/performance/goals/my | 我的目标 |
| GET | /api/performance/goals/{id} | 目标详情 |
| POST | /api/performance/goals | 创建目标 |
| PUT | /api/performance/goals/{id} | 更新目标 |
| POST | /api/performance/goals/{id}/submit | 提交审批 |
| POST | /api/performance/goals/{id}/approve | 审批通过 |
| POST | /api/performance/goals/{id}/reject | 审批驳回 |
| PUT | /api/performance/goals/{id}/progress | 更新进度 |
| GET | /api/performance/goals/team | 团队目标（管理者） |

### 4.3 绩效评估接口 (PerformanceReviewController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/performance/reviews | 评估列表 |
| GET | /api/performance/reviews/pending | 待评估列表 |
| GET | /api/performance/reviews/{id} | 评估详情 |
| POST | /api/performance/reviews | 创建评估 |
| PUT | /api/performance/reviews/{id} | 更新评估 |
| POST | /api/performance/reviews/{id}/submit | 提交评估 |
| POST | /api/performance/reviews/{id}/ai-generate | AI生成评语 |
| GET | /api/performance/reviews/employee/{employeeId} | 员工所有评估 |

### 4.4 绩效结果接口 (PerformanceResultController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/performance/results | 结果列表 |
| GET | /api/performance/results/my | 我的绩效结果 |
| GET | /api/performance/results/{id} | 结果详情 |
| POST | /api/performance/results/calibrate | 批量校准 |
| POST | /api/performance/results/{id}/confirm | 员工确认 |
| GET | /api/performance/results/distribution | 分布统计 |
| GET | /api/performance/results/export | 导出结果 |

### 4.5 工单类型接口 (TicketTypeController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/tickets/types | 工单类型列表 |
| GET | /api/tickets/types/tree | 工单类型树 |
| GET | /api/tickets/types/{id} | 类型详情 |
| POST | /api/tickets/types | 创建类型 |
| PUT | /api/tickets/types/{id} | 更新类型 |
| DELETE | /api/tickets/types/{id} | 删除类型 |

### 4.6 工单管理接口 (TicketController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/tickets | 工单列表（管理员） |
| GET | /api/tickets/my | 我的工单（员工） |
| GET | /api/tickets/assigned | 分配给我的工单 |
| GET | /api/tickets/{id} | 工单详情 |
| POST | /api/tickets | 创建工单 |
| PUT | /api/tickets/{id} | 更新工单 |
| POST | /api/tickets/{id}/assign | 分配工单 |
| POST | /api/tickets/{id}/start | 开始处理 |
| POST | /api/tickets/{id}/reply | 回复工单 |
| POST | /api/tickets/{id}/resolve | 解决工单 |
| POST | /api/tickets/{id}/close | 关闭工单 |
| POST | /api/tickets/{id}/reopen | 重新打开 |
| POST | /api/tickets/{id}/comment | 添加评论 |
| GET | /api/tickets/{id}/comments | 获取评论 |
| POST | /api/tickets/{id}/satisfaction | 满意度评价 |
| GET | /api/tickets/statistics | 工单统计 |

### 4.7 知识库接口 (KnowledgeArticleController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/knowledge/articles | 文章列表 |
| GET | /api/knowledge/articles/{id} | 文章详情 |
| POST | /api/knowledge/articles | 创建文章 |
| PUT | /api/knowledge/articles/{id} | 更新文章 |
| POST | /api/knowledge/articles/{id}/publish | 发布文章 |
| POST | /api/knowledge/articles/{id}/archive | 归档文章 |
| POST | /api/knowledge/articles/{id}/helpful | 标记有帮助 |
| POST | /api/knowledge/articles/{id}/not-helpful | 标记无帮助 |
| GET | /api/knowledge/articles/search | 搜索文章 |
| GET | /api/knowledge/categories | 分类列表 |

---

## 5. 前端页面设计（14个）

### 5.1 绩效管理模块（7个页面）

| 需求ID | 页面 | 路径 | 说明 |
|--------|------|------|------|
| RQ0048 | 绩效周期列表 | /performance/cycles | 周期管理 |
| RQ0049 | 绩效周期详情 | /performance/cycles/:id | 周期配置与进度 |
| RQ0050 | 我的目标 | /performance/goals/my | 员工目标管理 |
| RQ0051 | 团队目标 | /performance/goals/team | 管理者查看团队目标 |
| RQ0052 | 绩效评估 | /performance/reviews | 评估工作台 |
| RQ0053 | 绩效校准 | /performance/calibration | 校准工具（管理层） |
| RQ0054 | 绩效结果 | /performance/results | 结果查看与确认 |

### 5.2 工单系统模块（7个页面）

| 需求ID | 页面 | 路径 | 说明 |
|--------|------|------|------|
| RQ0055 | 工单类型管理 | /tickets/types | 类型配置 |
| RQ0056 | 工单看板 | /tickets/board | 看板视图 |
| RQ0057 | 工单列表 | /tickets/list | 列表视图 |
| RQ0058 | 工单详情 | /tickets/:id | 工单详情与处理 |
| RQ0059 | 提交工单 | /tickets/create | 员工提交工单 |
| RQ0060 | 知识库 | /knowledge | 知识库首页 |
| RQ0061 | 文章详情 | /knowledge/:id | 文章阅读页 |

---

## 6. AI 能力集成

### 6.1 AI 绩效评语生成 (PerformanceCommentGenerator)

```java
@Service
public class PerformanceCommentGenerator {

    @Autowired
    private ChatClient chatClient;

    /**
     * 基于目标完成情况生成绩效评语
     */
    public String generateReviewComment(
            Employee employee,
            List<PerformanceGoal> goals,
            PerformanceReview selfReview) {

        String prompt = """
            请为以下员工生成绩效评语：

            员工信息：
            - 姓名：%s
            - 岗位：%s
            - 入职时间：%s

            目标完成情况：
            %s

            员工自评：
            %s

            请生成一段200字左右的绩效评语，包含：
            1. 总体评价
            2. 主要成就（2-3点）
            3. 待改进领域（1-2点）
            4. 发展建议

            要求客观、具体、有建设性，避免套话。
            """.formatted(
                employee.getRealName(),
                employee.getPosition().getPositionName(),
                employee.getEntryDate(),
                formatGoals(goals),
                selfReview.getComments()
            );

        return chatClient.call(prompt);
    }

    private String formatGoals(List<PerformanceGoal> goals) {
        StringBuilder sb = new StringBuilder();
        for (PerformanceGoal goal : goals) {
            sb.append(String.format(
                "- %s：目标值=%s，完成值=%s，完成率=%d%%\n",
                goal.getGoalTitle(),
                goal.getTargetValue(),
                goal.getCurrentValue(),
                goal.getProgressPercent()
            ));
        }
        return sb.toString();
    }
}
```

### 6.2 AI 工单路由 (TicketRouterAgent)

```java
@Service
public class TicketRouterAgent {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private TicketTypeService ticketTypeService;

    /**
     * 根据工单内容智能路由
     */
    public TicketRoutingResult routeTicket(String title, String description) {
        // 获取所有工单类型
        List<TicketType> types = ticketTypeService.getAllActiveTypes();

        String prompt = """
            请根据工单内容，判断应该归类到哪个类型，并推荐处理人。

            工单标题：%s
            工单描述：%s

            可选的工单类型：
            %s

            请返回JSON格式：
            {
              "type_code": "工单类型编码",
              "confidence": 0.95,
              "priority": "normal/high/urgent",
              "suggested_team": "建议处理团队",
              "reason": "判断理由"
            }
            """.formatted(title, description, formatTypes(types));

        return chatClient.call(prompt, TicketRoutingResult.class);
    }
}
```

### 6.3 AI 答案建议 (TicketAnswerAgent)

```java
@Service
public class TicketAnswerAgent {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private RAGService ragService;

    /**
     * 根据工单内容和知识库生成答案建议
     */
    public String suggestAnswer(Ticket ticket) {
        // 1. 从知识库检索相关文档
        List<Document> docs = ragService.retrieve(
            ticket.getTitle() + " " + ticket.getDescription(),
            5  // Top-5
        );

        // 2. 生成答案
        String prompt = """
            请根据工单问题和参考资料，生成一个专业的回复建议。

            工单问题：
            标题：%s
            描述：%s

            参考资料：
            %s

            请生成一个清晰、专业、有帮助的回复，如果参考资料不足以回答问题，请说明。
            """.formatted(
                ticket.getTitle(),
                ticket.getDescription(),
                formatDocs(docs)
            );

        return chatClient.call(prompt);
    }
}
```

---

## 7. 验证标准

### 7.1 功能验证

- [ ] 绩效周期CRUD及阶段切换正常
- [ ] 目标设定及审批流程正常
- [ ] 目标进度更新正常
- [ ] 员工自评提交正常
- [ ] 上级评估及评分正常
- [ ] 绩效校准功能正常
- [ ] 绩效结果确认正常
- [ ] AI绩效评语生成质量可接受
- [ ] 工单创建及流转正常
- [ ] AI工单路由准确率>80%
- [ ] AI答案建议有参考价值
- [ ] 知识库搜索结果相关
- [ ] SLA超时提醒正常

### 7.2 数据验证

- [ ] 绩效评分计算正确
- [ ] 强制分布比例准确
- [ ] 工单SLA统计准确

### 7.3 性能验证

- [ ] AI评语生成 < 10s
- [ ] AI工单路由 < 3s
- [ ] 知识库搜索 < 500ms

---

## 8. 交付物清单

| 类型 | 数量 | 说明 |
|------|------|------|
| 数据库表 | 8张 | performance_cycles, tickets 等 |
| Entity | 8个 | 对应数据库表 |
| Mapper | 8个 | MyBatis Plus Mapper |
| Service | 8个 | 业务服务类 |
| Controller | 6个 | REST接口 |
| StateMachine | 2个 | 绩效周期+工单状态机 |
| AI Agent | 3个 | 评语生成+工单路由+答案建议 |
| 前端页面 | 14个 | React 页面组件 |

---

*Phase 4 完成后，系统将具备完整的绩效管理和员工工单处理能力，AI辅助功能将提升HR效率。*
