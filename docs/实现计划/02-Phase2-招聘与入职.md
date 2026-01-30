# Phase 2: 招聘与入职

> 招聘管理（M03）+ 入职Onboarding（M04）

## 概述

Phase 2 实现完整的招聘流程管理和入职流程管理，包括：
- 招聘需求发起与审批
- 候选人管理与简历解析
- 面试安排与反馈
- Offer 管理
- 入职流程与资料收集

---

## 1. 数据库表设计（9张表）

### 1.1 招聘管理模块（M03）- 7张表

#### 1.1.1 招聘需求表 (job_requisitions)

```sql
-- 招聘需求表
CREATE TABLE job_requisitions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    req_code VARCHAR(64) NOT NULL UNIQUE COMMENT '需求编号',
    position_id BIGINT NOT NULL COMMENT '岗位ID',
    org_unit_id BIGINT NOT NULL COMMENT '招聘部门ID',
    headcount INT NOT NULL COMMENT '招聘人数',
    job_title VARCHAR(100) COMMENT '职位名称（可自定义）',
    job_description TEXT COMMENT '职位描述',
    requirements TEXT COMMENT '任职要求',
    salary_range_min INT COMMENT '薪资范围-最低(元/月)',
    salary_range_max INT COMMENT '薪资范围-最高(元/月)',
    work_location VARCHAR(100) COMMENT '工作地点',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/pending_approval/open/paused/closed',
    applicant_id BIGINT COMMENT '申请人ID',
    apply_date DATE COMMENT '申请日期',
    approver_id BIGINT COMMENT '审批人ID',
    approve_date DATE COMMENT '审批日期',
    applied_count INT DEFAULT 0 COMMENT '已投递数',
    interviewed_count INT DEFAULT 0 COMMENT '已面试数',
    offered_count INT DEFAULT 0 COMMENT '已发Offer数',
    hired_count INT DEFAULT 0 COMMENT '已录用数',
    urgency VARCHAR(10) DEFAULT 'normal' COMMENT '紧急程度：urgent/normal/low',
    expected_onboard_date DATE COMMENT '期望到岗日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_org_unit (org_unit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘需求表';
```

**状态枚举**：
- `draft`: 草稿
- `pending_approval`: 待审批
- `open`: 招聘中
- `paused`: 暂停
- `closed`: 已关闭

#### 1.1.2 候选人表 (candidates)

```sql
-- 候选人表
CREATE TABLE candidates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    candidate_code VARCHAR(64) NOT NULL UNIQUE COMMENT '候选人编号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT COMMENT '性别：1-男/2-女',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    source VARCHAR(50) COMMENT '来源：internal_referral/headhunter/job_site/campus/direct',
    referrer_id BIGINT COMMENT '内推人ID（员工）',
    headhunter_name VARCHAR(50) COMMENT '猎头名称',
    status VARCHAR(20) DEFAULT 'new' COMMENT '状态：new/screening/interviewing/offering/pending_onboard/onboarded/rejected',
    current_req_id BIGINT COMMENT '当前关联的招聘需求ID',
    tags JSON COMMENT '标签(JSON数组)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_phone (phone),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人表';
```

**来源枚举**：
- `internal_referral`: 内部推荐
- `headhunter`: 猎头推荐
- `job_site`: 招聘网站（智联/Boss等）
- `campus`: 校园招聘
- `direct`: 直接投递

**状态枚举**：
- `new`: 新简历
- `screening`: 筛选中
- `interviewing`: 面试中
- `offering`: Offer阶段
- `pending_onboard`: 待入职
- `onboarded`: 已入职
- `rejected`: 已淘汰

#### 1.1.3 候选人简历表 (candidate_resumes)

```sql
-- 候选人简历表
CREATE TABLE candidate_resumes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    educations JSON COMMENT '教育经历(JSON数组)',
    experiences JSON COMMENT '工作经历(JSON数组)',
    skills JSON COMMENT '技能标签(JSON数组)',
    expected_salary INT COMMENT '期望薪资(元/月)',
    expected_location VARCHAR(100) COMMENT '期望工作地点',
    available_date DATE COMMENT '可到岗日期',
    resume_url VARCHAR(255) COMMENT '简历文件URL',
    ai_parsed TINYINT DEFAULT 0 COMMENT '是否AI解析过',
    ai_summary TEXT COMMENT 'AI简历摘要',
    ai_match_score DECIMAL(5,2) COMMENT 'AI匹配度评分(0-100)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_candidate (candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人简历表';
```

**educations JSON 结构**：
```json
[
  {
    "school": "清华大学",
    "degree": "master",
    "major": "计算机科学",
    "start_date": "2018-09",
    "end_date": "2021-06"
  }
]
```

**experiences JSON 结构**：
```json
[
  {
    "company": "阿里巴巴",
    "position": "高级Java开发",
    "start_date": "2021-07",
    "end_date": "2024-12",
    "description": "负责核心交易系统开发..."
  }
]
```

#### 1.1.4 候选人应聘记录表 (candidate_applications)

```sql
-- 候选人应聘记录表
CREATE TABLE candidate_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    job_req_id BIGINT NOT NULL COMMENT '招聘需求ID',
    status VARCHAR(20) DEFAULT 'applied' COMMENT '状态：applied/screening/passed/rejected',
    apply_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '投递日期',
    hr_evaluation TEXT COMMENT 'HR初评',
    hr_score INT COMMENT 'HR评分(1-5)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_candidate (candidate_id),
    INDEX idx_job_req (job_req_id),
    UNIQUE KEY uk_candidate_req (candidate_id, job_req_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人应聘记录表';
```

#### 1.1.5 面试表 (interviews)

```sql
-- 面试表
CREATE TABLE interviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    interview_code VARCHAR(64) NOT NULL UNIQUE COMMENT '面试编号',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    job_req_id BIGINT NOT NULL COMMENT '招聘需求ID',
    application_id BIGINT NOT NULL COMMENT '应聘记录ID',
    round INT COMMENT '面试轮次',
    interview_type VARCHAR(20) COMMENT '面试类型：phone/video/onsite/written',
    interviewer_ids JSON COMMENT '面试官ID列表(JSON数组)',
    scheduled_time TIMESTAMP COMMENT '预约时间',
    duration_minutes INT COMMENT '预计时长(分钟)',
    location VARCHAR(100) COMMENT '面试地点/会议链接',
    status VARCHAR(20) DEFAULT 'scheduled' COMMENT '状态：scheduled/in_progress/completed/cancelled/no_show',
    result VARCHAR(20) COMMENT '结果：pass/fail/pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_candidate (candidate_id),
    INDEX idx_scheduled_time (scheduled_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试表';
```

**面试类型**：
- `phone`: 电话面试
- `video`: 视频面试
- `onsite`: 现场面试
- `written`: 笔试

#### 1.1.6 面试反馈表 (interview_feedbacks)

```sql
-- 面试反馈表
CREATE TABLE interview_feedbacks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    interview_id BIGINT NOT NULL COMMENT '面试ID',
    interviewer_id BIGINT NOT NULL COMMENT '面试官ID',
    overall_score INT COMMENT '总体评分(1-5)',
    skill_score INT COMMENT '技能评分(1-5)',
    culture_score INT COMMENT '文化匹配评分(1-5)',
    communication_score INT COMMENT '沟通能力评分(1-5)',
    strengths TEXT COMMENT '优势',
    weaknesses TEXT COMMENT '不足',
    recommendation VARCHAR(20) COMMENT '建议：strong_hire/hire/no_hire/strong_no_hire',
    comments TEXT COMMENT '详细评价',
    ai_generated TINYINT DEFAULT 0 COMMENT '是否AI生成',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_interview (interview_id),
    UNIQUE KEY uk_interview_interviewer (interview_id, interviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试反馈表';
```

**推荐级别**：
- `strong_hire`: 强烈建议录用
- `hire`: 建议录用
- `no_hire`: 不建议录用
- `strong_no_hire`: 强烈不建议录用

#### 1.1.7 Offer表 (offers)

```sql
-- Offer表
CREATE TABLE offers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    offer_code VARCHAR(64) NOT NULL UNIQUE COMMENT 'Offer编号',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    job_req_id BIGINT NOT NULL COMMENT '招聘需求ID',
    position_id BIGINT COMMENT '岗位ID',
    org_unit_id BIGINT COMMENT '入职部门ID',
    salary_monthly INT COMMENT '月薪(元)',
    salary_annual INT COMMENT '年薪(元)',
    bonus_months INT COMMENT '年终奖月数',
    other_benefits TEXT COMMENT '其他福利',
    expected_onboard_date DATE COMMENT '期望入职日期',
    offer_expire_date DATE COMMENT 'Offer有效期',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/pending_approval/approved/sent/accepted/rejected/expired',
    approver_id BIGINT COMMENT '审批人ID',
    approve_date DATE COMMENT '审批日期',
    candidate_response VARCHAR(20) COMMENT '候选人反馈：accepted/rejected/negotiating',
    response_date DATE COMMENT '反馈日期',
    reject_reason TEXT COMMENT '拒绝原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_candidate (candidate_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Offer表';
```

---

### 1.2 入职Onboarding模块（M04）- 2张表

#### 1.2.1 入职记录表 (onboardings)

```sql
-- 入职记录表
CREATE TABLE onboardings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    onboarding_code VARCHAR(64) NOT NULL UNIQUE COMMENT '入职编号',
    offer_id BIGINT COMMENT '关联Offer ID',
    candidate_id BIGINT NOT NULL COMMENT '候选人ID',
    expected_date DATE COMMENT '预计入职日期',
    actual_date DATE COMMENT '实际入职日期',
    position_id BIGINT COMMENT '岗位ID',
    org_unit_id BIGINT COMMENT '入职部门ID',
    direct_manager_id BIGINT COMMENT '直属上级ID',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending/materials_collecting/materials_verified/completed/cancelled',
    employee_id BIGINT COMMENT '创建的员工ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_expected_date (expected_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入职记录表';
```

**状态枚举**：
- `pending`: 待入职
- `materials_collecting`: 资料收集中
- `materials_verified`: 资料已验证
- `completed`: 入职完成
- `cancelled`: 已取消

#### 1.2.2 入职资料表 (onboarding_materials)

```sql
-- 入职资料表
CREATE TABLE onboarding_materials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    onboarding_id BIGINT NOT NULL COMMENT '入职记录ID',
    material_type VARCHAR(50) NOT NULL COMMENT '资料类型：id_card/photo/diploma/bank_card/health_cert/contract',
    material_name VARCHAR(100) COMMENT '资料名称',
    is_required TINYINT DEFAULT 1 COMMENT '是否必须：1-是/0-否',
    file_url VARCHAR(255) COMMENT '文件URL',
    submitted TINYINT DEFAULT 0 COMMENT '是否已提交',
    submit_time TIMESTAMP COMMENT '提交时间',
    verified TINYINT DEFAULT 0 COMMENT '是否已审核',
    verify_time TIMESTAMP COMMENT '审核时间',
    verifier_id BIGINT COMMENT '审核人ID',
    remark TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_onboarding (onboarding_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入职资料表';
```

**资料类型枚举**：
- `id_card`: 身份证（正反面）
- `photo`: 证件照
- `diploma`: 学历证书
- `degree`: 学位证书
- `bank_card`: 银行卡
- `health_cert`: 体检报告
- `resignation_cert`: 离职证明
- `contract`: 劳动合同

---

## 2. 后端包结构

```
com.hrai.agent/
├── recruiting/                       # M03 招聘管理
│   ├── controller/
│   │   ├── JobRequisitionController.java   # 招聘需求
│   │   ├── CandidateController.java        # 候选人管理
│   │   ├── InterviewController.java        # 面试管理
│   │   └── OfferController.java            # Offer管理
│   ├── service/
│   │   ├── JobRequisitionService.java
│   │   ├── CandidateService.java
│   │   ├── ResumeParseService.java         # 简历解析服务
│   │   ├── InterviewService.java
│   │   └── OfferService.java
│   ├── statemachine/
│   │   ├── JobRequisitionStateMachine.java
│   │   ├── JobRequisitionState.java
│   │   ├── JobRequisitionEvent.java
│   │   ├── CandidateStateMachine.java
│   │   ├── CandidateState.java
│   │   └── CandidateEvent.java
│   ├── ai/
│   │   ├── ResumeParserAgent.java          # AI简历解析
│   │   └── CandidateMatchAgent.java        # AI候选人匹配
│   ├── integration/
│   │   └── OnboardingIntegration.java      # 与入职模块集成
│   ├── mapper/
│   │   ├── JobRequisitionMapper.java
│   │   ├── CandidateMapper.java
│   │   ├── CandidateResumeMapper.java
│   │   ├── CandidateApplicationMapper.java
│   │   ├── InterviewMapper.java
│   │   ├── InterviewFeedbackMapper.java
│   │   └── OfferMapper.java
│   ├── entity/
│   │   ├── JobRequisition.java
│   │   ├── Candidate.java
│   │   ├── CandidateResume.java
│   │   ├── CandidateApplication.java
│   │   ├── Interview.java
│   │   ├── InterviewFeedback.java
│   │   └── Offer.java
│   └── dto/
│       ├── JobRequisitionDTO.java
│       ├── CandidateDTO.java
│       ├── ResumeDTO.java
│       ├── InterviewDTO.java
│       ├── InterviewFeedbackDTO.java
│       └── OfferDTO.java
│
├── onboarding/                       # M04 入职Onboarding
│   ├── controller/
│   │   ├── OnboardingController.java       # 入职流程
│   │   └── OnboardingMaterialController.java # 入职资料
│   ├── service/
│   │   ├── OnboardingService.java
│   │   └── OnboardingMaterialService.java
│   ├── integration/
│   │   └── EmployeeCreationIntegration.java  # 创建员工
│   ├── mapper/
│   │   ├── OnboardingMapper.java
│   │   └── OnboardingMaterialMapper.java
│   ├── entity/
│   │   ├── Onboarding.java
│   │   └── OnboardingMaterial.java
│   └── dto/
│       ├── OnboardingDTO.java
│       └── OnboardingMaterialDTO.java
```

---

## 3. 状态机设计

### 3.1 招聘需求状态机 (JobRequisitionStateMachine)

```
状态流转：
                    ┌─────────────┐
                    │   draft     │
                    │  (草稿)     │
                    └──────┬──────┘
                           │ SUBMIT
                           ▼
                    ┌─────────────────┐
              ┌─────│pending_approval │─────┐
              │     │   (待审批)      │     │
              │     └────────┬────────┘     │
    REJECT    │              │ APPROVE      │ WITHDRAW
              ▼              ▼              ▼
        ┌──────────┐  ┌──────────┐  ┌──────────┐
        │  draft   │  │   open   │  │  draft   │
        │ (驳回后) │  │ (招聘中) │  │ (撤回后) │
        └──────────┘  └────┬─────┘  └──────────┘
                           │
              ┌────────────┼────────────┐
              │ PAUSE      │ COMPLETE   │ CLOSE
              ▼            │            ▼
        ┌──────────┐       │      ┌──────────┐
        │  paused  │───────┘      │  closed  │
        │ (已暂停) │ RESUME       │ (已关闭) │
        └──────────┘              └──────────┘
```

### 3.2 候选人状态机 (CandidateStateMachine)

```
状态流转：
    ┌─────────────┐
    │    new      │
    │ (新简历)    │
    └──────┬──────┘
           │ SCREEN
           ▼
    ┌─────────────┐
    │  screening  │──────────────┐
    │ (筛选中)    │              │
    └──────┬──────┘              │
           │ PASS_SCREEN         │ REJECT
           ▼                     │
    ┌─────────────┐              │
    │interviewing │──────────────┤
    │ (面试中)    │              │
    └──────┬──────┘              │
           │ PASS_INTERVIEW      │
           ▼                     │
    ┌─────────────┐              │
    │  offering   │──────────────┤
    │(Offer阶段)  │              │
    └──────┬──────┘              │
           │ ACCEPT_OFFER        │
           ▼                     ▼
    ┌───────────────┐     ┌──────────┐
    │pending_onboard│     │ rejected │
    │  (待入职)     │     │ (已淘汰) │
    └──────┬────────┘     └──────────┘
           │ COMPLETE_ONBOARD
           ▼
    ┌─────────────┐
    │  onboarded  │
    │ (已入职)    │
    └─────────────┘
```

---

## 4. API 接口设计

### 4.1 招聘需求接口 (JobRequisitionController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/recruiting/requisitions | 需求列表（分页） |
| GET | /api/recruiting/requisitions/{id} | 需求详情 |
| POST | /api/recruiting/requisitions | 创建需求 |
| PUT | /api/recruiting/requisitions/{id} | 更新需求 |
| POST | /api/recruiting/requisitions/{id}/submit | 提交审批 |
| POST | /api/recruiting/requisitions/{id}/approve | 审批通过 |
| POST | /api/recruiting/requisitions/{id}/reject | 审批驳回 |
| POST | /api/recruiting/requisitions/{id}/pause | 暂停招聘 |
| POST | /api/recruiting/requisitions/{id}/resume | 恢复招聘 |
| POST | /api/recruiting/requisitions/{id}/close | 关闭招聘 |

### 4.2 候选人接口 (CandidateController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/recruiting/candidates | 候选人列表 |
| GET | /api/recruiting/candidates/{id} | 候选人详情 |
| POST | /api/recruiting/candidates | 创建候选人 |
| PUT | /api/recruiting/candidates/{id} | 更新候选人 |
| POST | /api/recruiting/candidates/{id}/resume | 上传/更新简历 |
| POST | /api/recruiting/candidates/{id}/parse-resume | AI解析简历 |
| POST | /api/recruiting/candidates/{id}/apply | 投递岗位 |
| POST | /api/recruiting/candidates/{id}/reject | 淘汰候选人 |
| GET | /api/recruiting/candidates/{id}/applications | 投递记录 |
| GET | /api/recruiting/candidates/{id}/interviews | 面试记录 |

### 4.3 面试接口 (InterviewController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/recruiting/interviews | 面试列表 |
| GET | /api/recruiting/interviews/{id} | 面试详情 |
| POST | /api/recruiting/interviews | 安排面试 |
| PUT | /api/recruiting/interviews/{id} | 更新面试 |
| POST | /api/recruiting/interviews/{id}/cancel | 取消面试 |
| POST | /api/recruiting/interviews/{id}/feedback | 提交反馈 |
| GET | /api/recruiting/interviews/{id}/feedbacks | 获取所有反馈 |
| GET | /api/recruiting/interviews/calendar | 面试日历视图 |

### 4.4 Offer接口 (OfferController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/recruiting/offers | Offer列表 |
| GET | /api/recruiting/offers/{id} | Offer详情 |
| POST | /api/recruiting/offers | 创建Offer |
| PUT | /api/recruiting/offers/{id} | 更新Offer |
| POST | /api/recruiting/offers/{id}/submit | 提交审批 |
| POST | /api/recruiting/offers/{id}/approve | 审批通过 |
| POST | /api/recruiting/offers/{id}/send | 发送Offer |
| POST | /api/recruiting/offers/{id}/accept | 候选人接受 |
| POST | /api/recruiting/offers/{id}/reject | 候选人拒绝 |

### 4.5 入职接口 (OnboardingController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/onboarding/list | 入职记录列表 |
| GET | /api/onboarding/{id} | 入职详情 |
| POST | /api/onboarding | 创建入职记录 |
| PUT | /api/onboarding/{id} | 更新入职信息 |
| POST | /api/onboarding/{id}/complete | 完成入职 |
| POST | /api/onboarding/{id}/cancel | 取消入职 |

### 4.6 入职资料接口 (OnboardingMaterialController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/onboarding/{id}/materials | 获取资料清单 |
| POST | /api/onboarding/{id}/materials | 上传资料 |
| PUT | /api/onboarding/materials/{materialId} | 更新资料 |
| POST | /api/onboarding/materials/{materialId}/verify | 审核资料 |

---

## 5. 前端页面设计（15个）

### 5.1 招聘管理模块（10个页面）

| 需求ID | 页面 | 路径 | 说明 |
|--------|------|------|------|
| RQ0021 | 招聘需求列表 | /recruiting/requisitions | 看板/列表双视图 |
| RQ0022 | 招聘需求详情 | /recruiting/requisitions/:id | 查看/编辑需求 |
| RQ0023 | 候选人列表 | /recruiting/candidates | 候选人管理 |
| RQ0024 | 候选人详情 | /recruiting/candidates/:id | 候选人360视图 |
| RQ0025 | 面试日历 | /recruiting/interviews/calendar | 日历形式查看 |
| RQ0026 | 面试详情 | /recruiting/interviews/:id | 面试信息+反馈 |
| RQ0027 | 面试反馈表单 | /recruiting/interviews/:id/feedback | 填写面试反馈 |
| RQ0028 | Offer列表 | /recruiting/offers | Offer管理 |
| RQ0029 | Offer详情 | /recruiting/offers/:id | Offer信息 |
| RQ0030 | 招聘看板 | /recruiting/board | 招聘流程看板 |

### 5.2 入职模块（5个页面）

| 需求ID | 页面 | 路径 | 说明 |
|--------|------|------|------|
| RQ0031 | 入职待办 | /onboarding/pending | 待入职人员列表 |
| RQ0032 | 入职详情 | /onboarding/:id | 入职流程详情 |
| RQ0033 | 资料上传 | /onboarding/:id/materials | 入职资料上传（员工自助） |
| RQ0034 | 资料审核 | /onboarding/:id/verify | HR审核资料 |
| RQ0035 | 入职欢迎页 | /onboarding/:id/welcome | 入职引导页面 |

---

## 6. AI 能力集成

### 6.1 简历解析 (ResumeParserAgent)

**功能**：
- 上传简历文件（PDF/Word）
- AI 提取结构化信息
- 填充到 candidate_resumes 表

**实现**：
```java
@Service
public class ResumeParserAgent {

    @Autowired
    private ChatClient chatClient;

    public ResumeDTO parseResume(MultipartFile file) {
        // 1. 提取文本内容
        String content = extractText(file);

        // 2. 调用 AI 解析
        String prompt = """
            请从以下简历内容中提取结构化信息，以JSON格式返回：
            {
              "name": "姓名",
              "phone": "手机号",
              "email": "邮箱",
              "educations": [...],
              "experiences": [...],
              "skills": [...],
              "expected_salary": 期望薪资(数字),
              "summary": "一句话概括"
            }

            简历内容：
            %s
            """.formatted(content);

        String response = chatClient.call(prompt);

        // 3. 解析 JSON 并返回
        return parseJson(response, ResumeDTO.class);
    }
}
```

### 6.2 候选人匹配 (CandidateMatchAgent)

**功能**：
- 根据招聘需求，对候选人进行匹配度评分
- 返回匹配度分数和理由

**实现**：
```java
@Service
public class CandidateMatchAgent {

    public MatchResult matchCandidate(JobRequisition req, CandidateResume resume) {
        String prompt = """
            请评估该候选人与岗位的匹配度。

            岗位要求：
            - 职位：%s
            - 要求：%s

            候选人简历：
            - 教育背景：%s
            - 工作经历：%s
            - 技能：%s

            请返回：
            {
              "score": 0-100的匹配度分数,
              "strengths": ["优势1", "优势2"],
              "gaps": ["不足1", "不足2"],
              "recommendation": "建议"
            }
            """.formatted(req.getJobTitle(), req.getRequirements(),
                         resume.getEducations(), resume.getExperiences(), resume.getSkills());

        return chatClient.call(prompt, MatchResult.class);
    }
}
```

---

## 7. 模块联动

### 7.1 Offer接受 -> 创建入职记录

```java
@Component
public class OnboardingIntegration {

    @Autowired
    private OnboardingService onboardingService;

    @EventListener
    public void onOfferAccepted(OfferAcceptedEvent event) {
        Offer offer = event.getOffer();

        // 创建入职记录
        Onboarding onboarding = new Onboarding();
        onboarding.setOfferId(offer.getId());
        onboarding.setCandidateId(offer.getCandidateId());
        onboarding.setExpectedDate(offer.getExpectedOnboardDate());
        onboarding.setPositionId(offer.getPositionId());
        onboarding.setOrgUnitId(offer.getOrgUnitId());
        onboarding.setStatus("pending");

        onboardingService.create(onboarding);

        // 初始化入职资料清单
        onboardingService.initializeMaterials(onboarding.getId());
    }
}
```

### 7.2 入职完成 -> 创建员工

```java
@Component
public class EmployeeCreationIntegration {

    @Autowired
    private EmployeeService employeeService;

    @EventListener
    public void onOnboardingCompleted(OnboardingCompletedEvent event) {
        Onboarding onboarding = event.getOnboarding();
        Candidate candidate = event.getCandidate();

        // 创建员工
        Employee employee = new Employee();
        employee.setEmployeeCode(generateEmployeeCode());
        employee.setRealName(candidate.getName());
        employee.setPhone(candidate.getPhone());
        employee.setEmail(candidate.getEmail());
        employee.setOrgUnitId(onboarding.getOrgUnitId());
        employee.setPositionId(onboarding.getPositionId());
        employee.setDirectManagerId(onboarding.getDirectManagerId());
        employee.setEntryDate(onboarding.getActualDate());
        employee.setEmployeeStatus("trial");

        employeeService.create(employee);

        // 更新入职记录
        onboarding.setEmployeeId(employee.getId());

        // 更新候选人状态
        candidate.setStatus("onboarded");

        // 更新编制
        headcountService.incrementCurrentCount(
            onboarding.getOrgUnitId(),
            onboarding.getPositionId()
        );
    }
}
```

---

## 8. 验证标准

### 8.1 功能验证

- [ ] 招聘需求CRUD及状态流转正常
- [ ] 候选人全生命周期管理正常
- [ ] 简历上传及AI解析正常
- [ ] 面试安排及日历展示正常
- [ ] 面试反馈提交及汇总正常
- [ ] Offer审批流程正常
- [ ] Offer接受后自动创建入职记录
- [ ] 入职资料上传及审核正常
- [ ] 入职完成后自动创建员工

### 8.2 数据验证

- [ ] 候选人手机号/邮箱唯一性
- [ ] 一个候选人可投递多个岗位
- [ ] 面试时间冲突检测

### 8.3 性能验证

- [ ] 简历解析 < 10s
- [ ] 候选人匹配 < 5s
- [ ] 面试日历加载 < 500ms

---

## 9. 交付物清单

| 类型 | 数量 | 说明 |
|------|------|------|
| 数据库表 | 9张 | job_requisitions, candidates 等 |
| Entity | 9个 | 对应数据库表 |
| Mapper | 9个 | MyBatis Plus Mapper |
| Service | 8个 | 业务服务类 |
| Controller | 6个 | REST接口 |
| StateMachine | 2个 | 招聘需求+候选人状态机 |
| AI Agent | 2个 | 简历解析+候选人匹配 |
| 前端页面 | 15个 | React 页面组件 |

---

*Phase 2 完成后，系统将具备完整的招聘和入职管理能力，并与 Core HR 模块实现联动。*
