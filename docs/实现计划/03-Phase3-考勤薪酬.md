# Phase 3: 考勤薪酬

> 考勤假勤（M05）+ 薪酬薪资（M06）

## 概述

Phase 3 实现考勤管理和薪酬管理，包括：
- 班次配置与考勤规则
- 打卡记录管理
- 请假与加班申请
- 薪酬项目配置
- 算薪与工资条发放

---

## 1. 数据库表设计（12张表）

### 1.1 考勤假勤模块（M05）- 6张表

#### 1.1.1 班次表 (attendance_shifts)

```sql
-- 班次表
CREATE TABLE attendance_shifts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    shift_code VARCHAR(50) NOT NULL COMMENT '班次编码',
    shift_name VARCHAR(100) NOT NULL COMMENT '班次名称',
    work_start TIME COMMENT '上班时间',
    work_end TIME COMMENT '下班时间',
    break_start TIME COMMENT '休息开始时间',
    break_end TIME COMMENT '休息结束时间',
    work_hours DECIMAL(4,2) COMMENT '工作时长(小时)',
    is_flexible TINYINT DEFAULT 0 COMMENT '是否弹性工作制：0-否/1-是',
    flexible_minutes INT DEFAULT 0 COMMENT '弹性分钟数',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_code (tenant_id, shift_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次表';
```

**预置班次示例**：
- `standard`: 标准班 (09:00-18:00)
- `flexible`: 弹性班 (10:00-19:00, 弹性1小时)
- `early`: 早班 (08:00-17:00)
- `night`: 晚班 (14:00-23:00)

#### 1.1.2 考勤规则表 (attendance_rules)

```sql
-- 考勤规则表
CREATE TABLE attendance_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    org_unit_ids JSON COMMENT '适用部门ID列表(JSON数组)',
    employee_ids JSON COMMENT '特定员工ID列表(JSON数组)',
    shift_id BIGINT COMMENT '关联班次ID',
    workdays JSON COMMENT '工作日配置(JSON数组，如[1,2,3,4,5]表示周一到周五)',
    late_grace_minutes INT DEFAULT 0 COMMENT '迟到宽限分钟数',
    early_grace_minutes INT DEFAULT 0 COMMENT '早退宽限分钟数',
    absent_threshold_minutes INT DEFAULT 240 COMMENT '缺勤阈值(分钟)，超过视为旷工',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤规则表';
```

#### 1.1.3 打卡记录表 (attendance_records)

```sql
-- 打卡记录表
CREATE TABLE attendance_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    attendance_date DATE NOT NULL COMMENT '考勤日期',
    clock_in_time TIMESTAMP COMMENT '上班打卡时间',
    clock_out_time TIMESTAMP COMMENT '下班打卡时间',
    clock_in_location VARCHAR(100) COMMENT '上班打卡地点',
    clock_out_location VARCHAR(100) COMMENT '下班打卡地点',
    status VARCHAR(20) COMMENT '状态：normal-正常/late-迟到/early-早退/absent-缺勤/leave-请假',
    late_minutes INT DEFAULT 0 COMMENT '迟到分钟数',
    early_minutes INT DEFAULT 0 COMMENT '早退分钟数',
    work_hours DECIMAL(4,2) COMMENT '实际工作时长',
    overtime_hours DECIMAL(4,2) COMMENT '加班时长',
    is_supplemented TINYINT DEFAULT 0 COMMENT '是否补卡',
    supplement_reason TEXT COMMENT '补卡原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_employee_date (employee_id, attendance_date),
    INDEX idx_tenant_date (tenant_id, attendance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';
```

**状态枚举**：
- `normal`: 正常
- `late`: 迟到
- `early`: 早退
- `late_early`: 迟到+早退
- `absent`: 缺勤/旷工
- `leave`: 请假
- `holiday`: 节假日
- `rest`: 休息日

#### 1.1.4 假期类型表 (leave_types)

```sql
-- 假期类型表
CREATE TABLE leave_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    type_code VARCHAR(50) NOT NULL COMMENT '假期类型编码',
    type_name VARCHAR(100) NOT NULL COMMENT '假期类型名称',
    is_paid TINYINT DEFAULT 1 COMMENT '是否带薪：1-是/0-否',
    pay_ratio DECIMAL(3,2) DEFAULT 1.00 COMMENT '薪资比例(0-1)',
    need_proof TINYINT DEFAULT 0 COMMENT '是否需要证明材料',
    max_days INT COMMENT '年度最大天数(null表示不限)',
    min_unit DECIMAL(3,1) DEFAULT 0.5 COMMENT '最小请假单位(天)',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_code (tenant_id, type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='假期类型表';
```

**预置假期类型**：
- `annual`: 年假 (带薪, max_days根据工龄)
- `sick`: 病假 (带薪60%, need_proof)
- `personal`: 事假 (无薪)
- `marriage`: 婚假 (带薪, max_days=3)
- `maternity`: 产假 (带薪, max_days=158)
- `paternity`: 陪产假 (带薪, max_days=15)
- `bereavement`: 丧假 (带薪, max_days=3)
- `work_injury`: 工伤假 (带薪)

#### 1.1.5 假期余额表 (leave_balances)

```sql
-- 假期余额表
CREATE TABLE leave_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    leave_type_id BIGINT NOT NULL COMMENT '假期类型ID',
    year INT NOT NULL COMMENT '年度',
    total_days DECIMAL(5,1) COMMENT '总天数',
    used_days DECIMAL(5,1) DEFAULT 0 COMMENT '已使用天数',
    remaining_days DECIMAL(5,1) COMMENT '剩余天数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_employee_type_year (employee_id, leave_type_id, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='假期余额表';
```

#### 1.1.6 请假申请表 (leave_requests)

```sql
-- 请假申请表
CREATE TABLE leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    request_code VARCHAR(64) NOT NULL UNIQUE COMMENT '请假单号',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    leave_type_id BIGINT NOT NULL COMMENT '假期类型ID',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    start_time TIME COMMENT '开始时间(半天假时使用)',
    end_time TIME COMMENT '结束时间(半天假时使用)',
    duration_days DECIMAL(5,1) NOT NULL COMMENT '请假天数',
    reason TEXT COMMENT '请假原因',
    proof_url VARCHAR(255) COMMENT '证明材料URL',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/pending/approved/rejected/cancelled',
    approver_ids JSON COMMENT '审批人ID列表(JSON数组)',
    current_approver_id BIGINT COMMENT '当前审批人ID',
    approve_records JSON COMMENT '审批记录(JSON数组)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_employee (employee_id),
    INDEX idx_status (status),
    INDEX idx_date_range (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';
```

---

### 1.2 薪酬薪资模块（M06）- 6张表

#### 1.2.1 薪酬项目表 (salary_items)

```sql
-- 薪酬项目表
CREATE TABLE salary_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    item_code VARCHAR(50) NOT NULL COMMENT '项目编码',
    item_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    item_type VARCHAR(20) COMMENT '类型：income-收入/deduction-扣除',
    calc_type VARCHAR(20) COMMENT '计算方式：fixed-固定/formula-公式/manual-手工',
    formula TEXT COMMENT '计算公式',
    is_taxable TINYINT DEFAULT 1 COMMENT '是否计税',
    is_social_base TINYINT DEFAULT 0 COMMENT '是否计入社保基数',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_code (tenant_id, item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪酬项目表';
```

**预置薪酬项目**：
| 编码 | 名称 | 类型 | 计算方式 | 计税 |
|------|------|------|----------|------|
| base_salary | 基本工资 | income | fixed | 是 |
| post_salary | 岗位工资 | income | fixed | 是 |
| performance_salary | 绩效工资 | income | formula | 是 |
| overtime_pay | 加班费 | income | formula | 是 |
| meal_allowance | 餐补 | income | fixed | 否 |
| transport_allowance | 交通补贴 | income | fixed | 否 |
| social_personal | 社保个人部分 | deduction | formula | - |
| housing_fund_personal | 公积金个人部分 | deduction | formula | - |
| tax | 个人所得税 | deduction | formula | - |

#### 1.2.2 员工薪资档案表 (salary_archives)

```sql
-- 员工薪资档案表
CREATE TABLE salary_archives (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    salary_grade VARCHAR(50) COMMENT '薪资等级',
    base_salary DECIMAL(12,2) COMMENT '基本工资',
    post_salary DECIMAL(12,2) COMMENT '岗位工资',
    performance_salary DECIMAL(12,2) COMMENT '绩效工资基数',
    allowances JSON COMMENT '补贴明细(JSON)',
    social_base DECIMAL(12,2) COMMENT '社保基数',
    housing_fund_base DECIMAL(12,2) COMMENT '公积金基数',
    housing_fund_ratio_personal DECIMAL(4,2) COMMENT '公积金个人比例',
    housing_fund_ratio_company DECIMAL(4,2) COMMENT '公积金公司比例',
    bank_name VARCHAR(50) COMMENT '开户银行',
    bank_account VARCHAR(50) COMMENT '银行账号',
    effective_date DATE COMMENT '生效日期',
    expire_date DATE COMMENT '失效日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_employee (employee_id),
    INDEX idx_effective_date (effective_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工薪资档案表';
```

**allowances JSON 结构**：
```json
{
  "meal_allowance": 500,
  "transport_allowance": 300,
  "phone_allowance": 100,
  "housing_allowance": 1000
}
```

#### 1.2.3 算薪批次表 (payroll_runs)

```sql
-- 算薪批次表
CREATE TABLE payroll_runs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    run_code VARCHAR(64) NOT NULL UNIQUE COMMENT '批次编号',
    pay_period VARCHAR(20) COMMENT '薪资周期(如2025-01)',
    period_start DATE COMMENT '周期开始日期',
    period_end DATE COMMENT '周期结束日期',
    org_unit_ids JSON COMMENT '计算范围-部门ID列表',
    employee_count INT COMMENT '员工人数',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/calculating/calculated/reviewing/approved/paid',
    total_gross DECIMAL(14,2) COMMENT '应发总额',
    total_deduction DECIMAL(14,2) COMMENT '扣除总额',
    total_net DECIMAL(14,2) COMMENT '实发总额',
    total_company_cost DECIMAL(14,2) COMMENT '公司成本总额',
    calculator_id BIGINT COMMENT '计算人ID',
    calc_time TIMESTAMP COMMENT '计算时间',
    reviewer_id BIGINT COMMENT '复核人ID',
    review_time TIMESTAMP COMMENT '复核时间',
    approver_id BIGINT COMMENT '审批人ID',
    approve_time TIMESTAMP COMMENT '审批时间',
    remark TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_period (tenant_id, pay_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='算薪批次表';
```

**状态枚举**：
- `draft`: 草稿
- `calculating`: 计算中
- `calculated`: 已计算
- `reviewing`: 复核中
- `approved`: 已审批
- `paid`: 已发放

#### 1.2.4 薪资明细表 (payroll_details)

```sql
-- 薪资明细表
CREATE TABLE payroll_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    payroll_run_id BIGINT NOT NULL COMMENT '算薪批次ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    work_days DECIMAL(4,1) COMMENT '应出勤天数',
    actual_work_days DECIMAL(4,1) COMMENT '实际出勤天数',
    absent_days DECIMAL(4,1) COMMENT '缺勤天数',
    leave_days DECIMAL(4,1) COMMENT '请假天数',
    overtime_hours DECIMAL(5,1) COMMENT '加班小时数',
    income_items JSON COMMENT '收入项目明细(JSON)',
    gross_income DECIMAL(12,2) COMMENT '应发合计',
    deduction_items JSON COMMENT '扣除项目明细(JSON)',
    total_deduction DECIMAL(12,2) COMMENT '扣除合计',
    social_personal DECIMAL(10,2) COMMENT '社保个人部分',
    social_company DECIMAL(10,2) COMMENT '社保公司部分',
    housing_fund_personal DECIMAL(10,2) COMMENT '公积金个人部分',
    housing_fund_company DECIMAL(10,2) COMMENT '公积金公司部分',
    tax_base DECIMAL(12,2) COMMENT '计税基数',
    tax_amount DECIMAL(10,2) COMMENT '个人所得税',
    cumulative_income DECIMAL(14,2) COMMENT '累计收入(年度)',
    cumulative_deduction DECIMAL(14,2) COMMENT '累计扣除(年度)',
    cumulative_tax DECIMAL(12,2) COMMENT '累计已缴税(年度)',
    net_salary DECIMAL(12,2) COMMENT '实发工资',
    company_cost DECIMAL(12,2) COMMENT '公司成本',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_run (payroll_run_id),
    INDEX idx_employee (employee_id),
    UNIQUE KEY uk_run_employee (payroll_run_id, employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资明细表';
```

**income_items JSON 结构**：
```json
{
  "base_salary": 10000,
  "post_salary": 3000,
  "performance_salary": 2000,
  "overtime_pay": 500,
  "meal_allowance": 500,
  "transport_allowance": 300
}
```

#### 1.2.5 工资条表 (payslips)

```sql
-- 工资条表
CREATE TABLE payslips (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    payroll_detail_id BIGINT NOT NULL COMMENT '薪资明细ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    pay_period VARCHAR(20) COMMENT '薪资周期',
    content_encrypted TEXT COMMENT '工资条内容(加密JSON)',
    sent TINYINT DEFAULT 0 COMMENT '是否已发送',
    sent_time TIMESTAMP COMMENT '发送时间',
    sent_channel VARCHAR(20) COMMENT '发送渠道：email/sms/app',
    confirmed TINYINT DEFAULT 0 COMMENT '员工是否确认',
    confirm_time TIMESTAMP COMMENT '确认时间',
    viewed TINYINT DEFAULT 0 COMMENT '是否已查看',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    last_view_time TIMESTAMP COMMENT '最后查看时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_employee_period (employee_id, pay_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工资条表';
```

#### 1.2.6 加班申请表 (overtime_requests)

```sql
-- 加班申请表
CREATE TABLE overtime_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    request_code VARCHAR(64) NOT NULL UNIQUE COMMENT '加班单号',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    overtime_date DATE NOT NULL COMMENT '加班日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    duration_hours DECIMAL(4,1) NOT NULL COMMENT '加班时长(小时)',
    overtime_type VARCHAR(20) COMMENT '加班类型：weekday-工作日/weekend-周末/holiday-节假日',
    reason TEXT COMMENT '加班原因',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/pending/approved/rejected',
    approver_ids JSON COMMENT '审批人ID列表',
    current_approver_id BIGINT COMMENT '当前审批人ID',
    actual_hours DECIMAL(4,1) COMMENT '实际加班时长',
    compensation_type VARCHAR(20) COMMENT '补偿方式：pay-加班费/leave-调休',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_employee (employee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班申请表';
```

---

## 2. 后端包结构

```
com.hrai.agent/
├── attendance/                       # M05 考勤假勤
│   ├── controller/
│   │   ├── ShiftController.java          # 班次管理
│   │   ├── AttendanceRuleController.java # 考勤规则
│   │   ├── AttendanceController.java     # 打卡记录
│   │   ├── LeaveTypeController.java      # 假期类型
│   │   ├── LeaveController.java          # 请假管理
│   │   └── OvertimeController.java       # 加班管理
│   ├── service/
│   │   ├── ShiftService.java
│   │   ├── AttendanceRuleService.java
│   │   ├── AttendanceService.java
│   │   ├── LeaveTypeService.java
│   │   ├── LeaveBalanceService.java      # 假期余额
│   │   ├── LeaveRequestService.java
│   │   └── OvertimeService.java
│   ├── statemachine/
│   │   ├── LeaveRequestStateMachine.java
│   │   └── OvertimeRequestStateMachine.java
│   ├── calculator/
│   │   └── AttendanceCalculator.java     # 考勤计算器
│   ├── mapper/
│   │   ├── AttendanceShiftMapper.java
│   │   ├── AttendanceRuleMapper.java
│   │   ├── AttendanceRecordMapper.java
│   │   ├── LeaveTypeMapper.java
│   │   ├── LeaveBalanceMapper.java
│   │   ├── LeaveRequestMapper.java
│   │   └── OvertimeRequestMapper.java
│   ├── entity/
│   │   ├── AttendanceShift.java
│   │   ├── AttendanceRule.java
│   │   ├── AttendanceRecord.java
│   │   ├── LeaveType.java
│   │   ├── LeaveBalance.java
│   │   ├── LeaveRequest.java
│   │   └── OvertimeRequest.java
│   └── dto/
│       ├── ShiftDTO.java
│       ├── AttendanceRuleDTO.java
│       ├── AttendanceRecordDTO.java
│       ├── LeaveTypeDTO.java
│       ├── LeaveBalanceDTO.java
│       ├── LeaveRequestDTO.java
│       ├── OvertimeRequestDTO.java
│       └── AttendanceSummaryDTO.java     # 考勤汇总
│
├── payroll/                          # M06 薪酬薪资
│   ├── controller/
│   │   ├── SalaryItemController.java     # 薪酬项目
│   │   ├── SalaryArchiveController.java  # 薪资档案
│   │   ├── PayrollRunController.java     # 算薪批次
│   │   └── PayslipController.java        # 工资条
│   ├── service/
│   │   ├── SalaryItemService.java
│   │   ├── SalaryArchiveService.java
│   │   ├── PayrollRunService.java
│   │   ├── PayrollCalculator.java        # 算薪核心
│   │   ├── TaxCalculator.java            # 个税计算
│   │   ├── SocialSecurityCalculator.java # 社保计算
│   │   └── PayslipService.java
│   ├── statemachine/
│   │   └── PayrollRunStateMachine.java
│   ├── integration/
│   │   └── AttendancePayrollIntegration.java  # 考勤数据集成
│   ├── mapper/
│   │   ├── SalaryItemMapper.java
│   │   ├── SalaryArchiveMapper.java
│   │   ├── PayrollRunMapper.java
│   │   ├── PayrollDetailMapper.java
│   │   └── PayslipMapper.java
│   ├── entity/
│   │   ├── SalaryItem.java
│   │   ├── SalaryArchive.java
│   │   ├── PayrollRun.java
│   │   ├── PayrollDetail.java
│   │   └── Payslip.java
│   └── dto/
│       ├── SalaryItemDTO.java
│       ├── SalaryArchiveDTO.java
│       ├── PayrollRunDTO.java
│       ├── PayrollDetailDTO.java
│       └── PayslipDTO.java
```

---

## 3. 状态机设计

### 3.1 请假申请状态机 (LeaveRequestStateMachine)

```
状态流转：
                    ┌─────────────┐
                    │   draft     │
                    │  (草稿)     │
                    └──────┬──────┘
                           │ SUBMIT
                           ▼
                    ┌─────────────┐
              ┌─────│  pending    │─────┐
              │     │ (审批中)    │     │
              │     └──────┬──────┘     │
    REJECT    │            │ APPROVE    │ CANCEL
              ▼            ▼            ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │ rejected │ │ approved │ │cancelled │
        │ (已驳回) │ │ (已通过) │ │(已取消)  │
        └──────────┘ └──────────┘ └──────────┘
```

### 3.2 算薪批次状态机 (PayrollRunStateMachine)

```
状态流转：
    ┌─────────────┐
    │   draft     │
    │  (草稿)     │
    └──────┬──────┘
           │ START_CALC
           ▼
    ┌─────────────┐
    │ calculating │
    │  (计算中)   │
    └──────┬──────┘
           │ CALC_DONE
           ▼
    ┌─────────────┐
    │ calculated  │
    │  (已计算)   │
    └──────┬──────┘
           │ SUBMIT_REVIEW
           ▼
    ┌─────────────┐
    │  reviewing  │──────────────┐
    │  (复核中)   │              │
    └──────┬──────┘              │
           │ APPROVE             │ REJECT
           ▼                     ▼
    ┌─────────────┐       ┌─────────────┐
    │  approved   │       │ calculated  │
    │  (已审批)   │       │ (需重算)    │
    └──────┬──────┘       └─────────────┘
           │ PAY
           ▼
    ┌─────────────┐
    │    paid     │
    │  (已发放)   │
    └─────────────┘
```

---

## 4. API 接口设计

### 4.1 班次管理接口 (ShiftController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/attendance/shifts | 班次列表 |
| GET | /api/attendance/shifts/{id} | 班次详情 |
| POST | /api/attendance/shifts | 创建班次 |
| PUT | /api/attendance/shifts/{id} | 更新班次 |
| DELETE | /api/attendance/shifts/{id} | 删除班次 |

### 4.2 考勤规则接口 (AttendanceRuleController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/attendance/rules | 规则列表 |
| GET | /api/attendance/rules/{id} | 规则详情 |
| POST | /api/attendance/rules | 创建规则 |
| PUT | /api/attendance/rules/{id} | 更新规则 |
| DELETE | /api/attendance/rules/{id} | 删除规则 |

### 4.3 打卡记录接口 (AttendanceController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/attendance/records | 打卡记录列表 |
| GET | /api/attendance/records/my | 我的打卡记录 |
| POST | /api/attendance/clock-in | 上班打卡 |
| POST | /api/attendance/clock-out | 下班打卡 |
| POST | /api/attendance/supplement | 补卡申请 |
| GET | /api/attendance/summary | 考勤汇总 |
| GET | /api/attendance/summary/monthly | 月度考勤汇总 |

### 4.4 请假管理接口 (LeaveController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/attendance/leave/types | 假期类型列表 |
| GET | /api/attendance/leave/balance | 我的假期余额 |
| GET | /api/attendance/leave/requests | 请假申请列表 |
| GET | /api/attendance/leave/requests/{id} | 请假详情 |
| POST | /api/attendance/leave/requests | 创建请假申请 |
| PUT | /api/attendance/leave/requests/{id} | 更新请假申请 |
| POST | /api/attendance/leave/requests/{id}/submit | 提交审批 |
| POST | /api/attendance/leave/requests/{id}/approve | 审批通过 |
| POST | /api/attendance/leave/requests/{id}/reject | 审批驳回 |
| POST | /api/attendance/leave/requests/{id}/cancel | 取消申请 |

### 4.5 加班管理接口 (OvertimeController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/attendance/overtime/requests | 加班申请列表 |
| GET | /api/attendance/overtime/requests/{id} | 加班详情 |
| POST | /api/attendance/overtime/requests | 创建加班申请 |
| PUT | /api/attendance/overtime/requests/{id} | 更新加班申请 |
| POST | /api/attendance/overtime/requests/{id}/submit | 提交审批 |
| POST | /api/attendance/overtime/requests/{id}/approve | 审批通过 |
| POST | /api/attendance/overtime/requests/{id}/reject | 审批驳回 |

### 4.6 薪酬项目接口 (SalaryItemController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/payroll/items | 薪酬项目列表 |
| GET | /api/payroll/items/{id} | 薪酬项目详情 |
| POST | /api/payroll/items | 创建薪酬项目 |
| PUT | /api/payroll/items/{id} | 更新薪酬项目 |
| DELETE | /api/payroll/items/{id} | 删除薪酬项目 |

### 4.7 薪资档案接口 (SalaryArchiveController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/payroll/archives | 薪资档案列表 |
| GET | /api/payroll/archives/{employeeId} | 员工薪资档案 |
| POST | /api/payroll/archives | 创建薪资档案 |
| PUT | /api/payroll/archives/{id} | 更新薪资档案 |
| GET | /api/payroll/archives/{employeeId}/history | 薪资变更历史 |

### 4.8 算薪批次接口 (PayrollRunController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/payroll/runs | 算薪批次列表 |
| GET | /api/payroll/runs/{id} | 批次详情 |
| POST | /api/payroll/runs | 创建批次 |
| PUT | /api/payroll/runs/{id} | 更新批次 |
| POST | /api/payroll/runs/{id}/calculate | 执行算薪 |
| POST | /api/payroll/runs/{id}/recalculate | 重新算薪 |
| POST | /api/payroll/runs/{id}/submit-review | 提交复核 |
| POST | /api/payroll/runs/{id}/approve | 审批通过 |
| POST | /api/payroll/runs/{id}/reject | 审批驳回 |
| POST | /api/payroll/runs/{id}/pay | 标记已发放 |
| GET | /api/payroll/runs/{id}/details | 薪资明细列表 |
| GET | /api/payroll/runs/{id}/export | 导出薪资表 |

### 4.9 工资条接口 (PayslipController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/payroll/payslips | 工资条列表（管理员） |
| GET | /api/payroll/payslips/my | 我的工资条 |
| GET | /api/payroll/payslips/{id} | 工资条详情 |
| POST | /api/payroll/payslips/send | 批量发送工资条 |
| POST | /api/payroll/payslips/{id}/confirm | 员工确认 |

---

## 5. 前端页面设计（12个）

### 5.1 考勤假勤模块（6个页面）

| 需求ID | 页面 | 路径 | 说明 |
|--------|------|------|------|
| RQ0036 | 班次管理 | /attendance/shifts | 班次配置 |
| RQ0037 | 考勤规则 | /attendance/rules | 规则配置 |
| RQ0038 | 打卡记录 | /attendance/records | 打卡记录查询 |
| RQ0039 | 考勤汇总 | /attendance/summary | 月度汇总报表 |
| RQ0040 | 请假申请 | /attendance/leave | 请假管理 |
| RQ0041 | 加班申请 | /attendance/overtime | 加班管理 |

### 5.2 薪酬薪资模块（6个页面）

| 需求ID | 页面 | 路径 | 说明 |
|--------|------|------|------|
| RQ0042 | 薪酬项目 | /payroll/items | 薪酬项目配置 |
| RQ0043 | 薪资档案 | /payroll/archives | 员工薪资档案 |
| RQ0044 | 算薪批次 | /payroll/runs | 算薪管理 |
| RQ0045 | 薪资明细 | /payroll/runs/:id/details | 批次明细 |
| RQ0046 | 工资条管理 | /payroll/payslips | 工资条发放 |
| RQ0047 | 我的工资条 | /payroll/payslips/my | 员工自助查询 |

---

## 6. 核心算法

### 6.1 考勤计算器 (AttendanceCalculator)

```java
@Service
public class AttendanceCalculator {

    /**
     * 计算单日考勤状态
     */
    public AttendanceStatus calculateDailyStatus(
            AttendanceRecord record,
            AttendanceRule rule,
            AttendanceShift shift) {

        AttendanceStatus status = new AttendanceStatus();

        // 1. 检查是否是工作日
        if (!isWorkday(record.getAttendanceDate(), rule.getWorkdays())) {
            status.setStatus("rest");
            return status;
        }

        // 2. 检查是否请假
        if (hasLeaveApproved(record.getEmployeeId(), record.getAttendanceDate())) {
            status.setStatus("leave");
            return status;
        }

        // 3. 计算迟到
        if (record.getClockInTime() != null) {
            LocalTime clockIn = record.getClockInTime().toLocalTime();
            LocalTime workStart = shift.getWorkStart();
            int graceMinutes = rule.getLateGraceMinutes();

            if (clockIn.isAfter(workStart.plusMinutes(graceMinutes))) {
                int lateMinutes = (int) Duration.between(workStart, clockIn).toMinutes();
                status.setLateMinutes(lateMinutes);
            }
        } else {
            // 未打上班卡
            status.setStatus("absent");
            return status;
        }

        // 4. 计算早退
        if (record.getClockOutTime() != null) {
            LocalTime clockOut = record.getClockOutTime().toLocalTime();
            LocalTime workEnd = shift.getWorkEnd();
            int graceMinutes = rule.getEarlyGraceMinutes();

            if (clockOut.isBefore(workEnd.minusMinutes(graceMinutes))) {
                int earlyMinutes = (int) Duration.between(clockOut, workEnd).toMinutes();
                status.setEarlyMinutes(earlyMinutes);
            }
        }

        // 5. 计算工作时长
        if (record.getClockInTime() != null && record.getClockOutTime() != null) {
            long workMinutes = Duration.between(
                record.getClockInTime(),
                record.getClockOutTime()
            ).toMinutes();

            // 减去休息时间
            if (shift.getBreakStart() != null && shift.getBreakEnd() != null) {
                workMinutes -= Duration.between(
                    shift.getBreakStart(),
                    shift.getBreakEnd()
                ).toMinutes();
            }

            status.setWorkHours(workMinutes / 60.0);
        }

        // 6. 确定最终状态
        if (status.getLateMinutes() > 0 && status.getEarlyMinutes() > 0) {
            status.setStatus("late_early");
        } else if (status.getLateMinutes() > 0) {
            status.setStatus("late");
        } else if (status.getEarlyMinutes() > 0) {
            status.setStatus("early");
        } else {
            status.setStatus("normal");
        }

        return status;
    }
}
```

### 6.2 个税计算器 (TaxCalculator)

```java
@Service
public class TaxCalculator {

    // 2025年个税税率表（累计预扣法）
    private static final BigDecimal[][] TAX_BRACKETS = {
        {new BigDecimal("36000"), new BigDecimal("0.03"), new BigDecimal("0")},
        {new BigDecimal("144000"), new BigDecimal("0.10"), new BigDecimal("2520")},
        {new BigDecimal("300000"), new BigDecimal("0.20"), new BigDecimal("16920")},
        {new BigDecimal("420000"), new BigDecimal("0.25"), new BigDecimal("31920")},
        {new BigDecimal("660000"), new BigDecimal("0.30"), new BigDecimal("52920")},
        {new BigDecimal("960000"), new BigDecimal("0.35"), new BigDecimal("85920")},
        {null, new BigDecimal("0.45"), new BigDecimal("181920")}
    };

    // 基本减除费用
    private static final BigDecimal BASIC_DEDUCTION = new BigDecimal("5000");

    /**
     * 计算本月应缴个税（累计预扣法）
     *
     * @param cumulativeIncome     累计收入
     * @param cumulativeDeduction  累计扣除（社保+公积金+专项附加）
     * @param cumulativeTaxPaid    累计已缴税
     * @param months               累计月数
     * @return 本月应缴税额
     */
    public BigDecimal calculateMonthlyTax(
            BigDecimal cumulativeIncome,
            BigDecimal cumulativeDeduction,
            BigDecimal cumulativeTaxPaid,
            int months) {

        // 累计减除费用 = 5000 * 月数
        BigDecimal cumulativeBasic = BASIC_DEDUCTION.multiply(new BigDecimal(months));

        // 累计应纳税所得额 = 累计收入 - 累计扣除 - 累计基本减除
        BigDecimal taxableIncome = cumulativeIncome
            .subtract(cumulativeDeduction)
            .subtract(cumulativeBasic);

        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 查找适用税率
        BigDecimal rate = BigDecimal.ZERO;
        BigDecimal quickDeduction = BigDecimal.ZERO;

        for (BigDecimal[] bracket : TAX_BRACKETS) {
            if (bracket[0] == null || taxableIncome.compareTo(bracket[0]) <= 0) {
                rate = bracket[1];
                quickDeduction = bracket[2];
                break;
            }
        }

        // 累计应纳税额 = 累计应纳税所得额 × 税率 - 速算扣除数
        BigDecimal cumulativeTax = taxableIncome.multiply(rate).subtract(quickDeduction);

        // 本月应纳税额 = 累计应纳税额 - 累计已缴税额
        BigDecimal monthlyTax = cumulativeTax.subtract(cumulativeTaxPaid);

        return monthlyTax.max(BigDecimal.ZERO);
    }
}
```

---

## 7. 模块联动

### 7.1 考勤数据 -> 算薪

```java
@Service
public class AttendancePayrollIntegration {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 获取员工月度考勤数据（供算薪使用）
     */
    public AttendanceDataForPayroll getAttendanceData(
            Long employeeId,
            Date periodStart,
            Date periodEnd) {

        AttendanceDataForPayroll data = new AttendanceDataForPayroll();

        // 1. 获取应出勤天数
        int workDays = attendanceService.countWorkdays(periodStart, periodEnd);
        data.setWorkDays(workDays);

        // 2. 获取实际出勤天数
        List<AttendanceRecord> records = attendanceService.getRecords(
            employeeId, periodStart, periodEnd
        );

        int actualWorkDays = (int) records.stream()
            .filter(r -> "normal".equals(r.getStatus()) ||
                        "late".equals(r.getStatus()) ||
                        "early".equals(r.getStatus()))
            .count();
        data.setActualWorkDays(actualWorkDays);

        // 3. 统计请假天数
        BigDecimal leaveDays = leaveService.countLeaveDays(
            employeeId, periodStart, periodEnd
        );
        data.setLeaveDays(leaveDays);

        // 4. 统计加班时长
        BigDecimal overtimeHours = overtimeService.countOvertimeHours(
            employeeId, periodStart, periodEnd
        );
        data.setOvertimeHours(overtimeHours);

        // 5. 统计迟到/早退扣款
        // ...

        return data;
    }
}
```

---

## 8. 验证标准

### 8.1 功能验证

- [ ] 班次CRUD及规则配置正常
- [ ] 打卡功能正常（含位置信息）
- [ ] 迟到/早退/缺勤判定准确
- [ ] 请假申请及审批流程正常
- [ ] 请假天数扣减正确
- [ ] 加班申请及审批流程正常
- [ ] 算薪批次创建及计算正常
- [ ] 个税累计预扣计算准确
- [ ] 社保公积金计算准确
- [ ] 工资条发送及查看正常

### 8.2 数据验证

- [ ] 考勤数据与薪资数据一致
- [ ] 年度累计数据正确
- [ ] 多月数据连续性

### 8.3 性能验证

- [ ] 单次算薪（1000人）< 30s
- [ ] 考勤汇总查询 < 500ms
- [ ] 工资条批量发送 < 5s/100条

---

## 9. 交付物清单

| 类型 | 数量 | 说明 |
|------|------|------|
| 数据库表 | 12张 | attendance_shifts, payroll_runs 等 |
| Entity | 12个 | 对应数据库表 |
| Mapper | 12个 | MyBatis Plus Mapper |
| Service | 12个 | 业务服务类 |
| Controller | 8个 | REST接口 |
| StateMachine | 2个 | 请假+算薪状态机 |
| Calculator | 3个 | 考勤/个税/社保计算器 |
| 前端页面 | 12个 | React 页面组件 |

---

*Phase 3 完成后，系统将具备完整的考勤管理和薪酬管理能力。*
