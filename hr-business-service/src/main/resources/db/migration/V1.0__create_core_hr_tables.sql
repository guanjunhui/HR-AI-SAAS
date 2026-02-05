-- =====================================================
-- Phase 1: Core HR 基础表 (8张)
-- 版本: V1.0
-- 作者: HR-AI-SAAS
-- 日期: 2026-02-05
-- =====================================================

USE hr_ai_saas;

-- 1. 岗位表 (positions)
-- 用途: 定义公司所有岗位，关联职族和职级
CREATE TABLE IF NOT EXISTS `positions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `position_code` VARCHAR(50) NOT NULL COMMENT '岗位编码',
    `position_name` VARCHAR(100) NOT NULL COMMENT '岗位名称',
    `position_level` INT DEFAULT NULL COMMENT '职级 (1-10)',
    `job_family` VARCHAR(50) DEFAULT NULL COMMENT '职族 (技术/产品/运营/销售等)',
    `org_unit_id` BIGINT DEFAULT NULL COMMENT '所属部门ID (关联org_unit表)',
    `description` TEXT DEFAULT NULL COMMENT '岗位描述',
    `requirements` TEXT DEFAULT NULL COMMENT '任职要求',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=停用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `position_code`, `deleted`),
    KEY `idx_org_unit` (`org_unit_id`),
    KEY `idx_tenant_status` (`tenant_id`, `status`),
    KEY `idx_job_family` (`tenant_id`, `job_family`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位表';

-- 2. 员工表 (employees)
-- 用途: 核心员工信息表，关联系统用户、部门、岗位
CREATE TABLE IF NOT EXISTS `employees` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `employee_code` VARCHAR(50) NOT NULL COMMENT '工号',
    `sys_user_id` BIGINT DEFAULT NULL COMMENT '关联系统用户ID (关联sys_user表)',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `gender` TINYINT DEFAULT NULL COMMENT '性别: 1=男, 2=女',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `org_unit_id` BIGINT DEFAULT NULL COMMENT '所属部门ID',
    `position_id` BIGINT DEFAULT NULL COMMENT '岗位ID',
    `direct_manager_id` BIGINT DEFAULT NULL COMMENT '直属上级员工ID',
    `entry_date` DATE DEFAULT NULL COMMENT '入职日期',
    `probation_end_date` DATE DEFAULT NULL COMMENT '试用期结束日期',
    `regular_date` DATE DEFAULT NULL COMMENT '转正日期',
    `resignation_date` DATE DEFAULT NULL COMMENT '离职日期',
    `employee_status` VARCHAR(20) NOT NULL DEFAULT 'trial' COMMENT '员工状态: trial=试用期, regular=正式, resigned=离职',
    `work_location` VARCHAR(100) DEFAULT NULL COMMENT '工作地点',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `employee_code`, `deleted`),
    KEY `idx_org_unit` (`org_unit_id`),
    KEY `idx_position` (`position_id`),
    KEY `idx_manager` (`direct_manager_id`),
    KEY `idx_sys_user` (`sys_user_id`),
    KEY `idx_tenant_status` (`tenant_id`, `employee_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工表';

-- 3. 编制表 (headcounts)
-- 用途: 管理各部门/岗位的人员编制
CREATE TABLE IF NOT EXISTS `headcounts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `org_unit_id` BIGINT NOT NULL COMMENT '部门ID',
    `position_id` BIGINT DEFAULT NULL COMMENT '岗位ID (可选，空表示部门整体编制)',
    `budget_count` INT NOT NULL DEFAULT 0 COMMENT '编制人数',
    `actual_count` INT NOT NULL DEFAULT 0 COMMENT '在职人数',
    `year` INT NOT NULL COMMENT '编制年度',
    `quarter` TINYINT DEFAULT NULL COMMENT '季度 (1-4)，空表示全年',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=有效, 0=无效',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_org_position_year` (`tenant_id`, `org_unit_id`, `position_id`, `year`, `quarter`, `deleted`),
    KEY `idx_org_unit` (`org_unit_id`),
    KEY `idx_year` (`tenant_id`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编制表';

-- 4. 员工详细信息表 (employee_details)
-- 用途: 存储员工扩展信息（个人信息、紧急联系人等）
CREATE TABLE IF NOT EXISTS `employee_details` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `birthday` DATE DEFAULT NULL COMMENT '出生日期',
    `native_place` VARCHAR(100) DEFAULT NULL COMMENT '籍贯',
    `marital_status` TINYINT DEFAULT NULL COMMENT '婚姻状态: 0=未婚, 1=已婚',
    `education` VARCHAR(20) DEFAULT NULL COMMENT '最高学历',
    `university` VARCHAR(100) DEFAULT NULL COMMENT '毕业院校',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `graduation_date` DATE DEFAULT NULL COMMENT '毕业日期',
    `home_address` VARCHAR(255) DEFAULT NULL COMMENT '家庭住址',
    `emergency_contact_name` VARCHAR(50) DEFAULT NULL COMMENT '紧急联系人姓名',
    `emergency_contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系人电话',
    `emergency_contact_relation` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系人关系',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户银行',
    `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '银行账号',
    `social_security_number` VARCHAR(50) DEFAULT NULL COMMENT '社保号',
    `housing_fund_number` VARCHAR(50) DEFAULT NULL COMMENT '公积金号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee` (`tenant_id`, `employee_id`, `deleted`),
    KEY `idx_employee` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工详细信息表';

-- 5. 任职事件表 (employment_events)
-- 用途: 记录员工的任职变动（入职/转正/调岗/晋升/离职等），支持审批流程
CREATE TABLE IF NOT EXISTS `employment_events` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `event_type` VARCHAR(30) NOT NULL COMMENT '事件类型: entry=入职, regular=转正, transfer=调岗, promotion=晋升, demotion=降级, resignation=离职',
    `event_date` DATE NOT NULL COMMENT '事件生效日期',
    `reason` TEXT DEFAULT NULL COMMENT '变动原因',
    -- 变动前信息
    `from_org_unit_id` BIGINT DEFAULT NULL COMMENT '原部门ID',
    `from_position_id` BIGINT DEFAULT NULL COMMENT '原岗位ID',
    `from_salary_grade` VARCHAR(20) DEFAULT NULL COMMENT '原薪资等级',
    -- 变动后信息
    `to_org_unit_id` BIGINT DEFAULT NULL COMMENT '新部门ID',
    `to_position_id` BIGINT DEFAULT NULL COMMENT '新岗位ID',
    `to_salary_grade` VARCHAR(20) DEFAULT NULL COMMENT '新薪资等级',
    -- 审批相关
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: draft=草稿, pending=待审批, approved=已通过, rejected=已驳回, cancelled=已取消',
    `applicant_id` BIGINT DEFAULT NULL COMMENT '申请人ID',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
    `approved_at` DATETIME DEFAULT NULL COMMENT '审批时间',
    `reject_reason` TEXT DEFAULT NULL COMMENT '驳回原因',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_employee` (`employee_id`),
    KEY `idx_tenant_type` (`tenant_id`, `event_type`),
    KEY `idx_tenant_status` (`tenant_id`, `status`),
    KEY `idx_event_date` (`event_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任职事件表';

-- 6. 考勤班次表 (attendance_shifts)
-- 用途: 定义工作班次
CREATE TABLE IF NOT EXISTS `attendance_shifts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `shift_code` VARCHAR(50) NOT NULL COMMENT '班次编码',
    `shift_name` VARCHAR(100) NOT NULL COMMENT '班次名称',
    `start_time` TIME NOT NULL COMMENT '上班时间',
    `end_time` TIME NOT NULL COMMENT '下班时间',
    `work_hours` DECIMAL(4,2) NOT NULL COMMENT '工作时长(小时)',
    `break_start` TIME DEFAULT NULL COMMENT '休息开始时间',
    `break_end` TIME DEFAULT NULL COMMENT '休息结束时间',
    `flexible_minutes` INT DEFAULT 0 COMMENT '弹性时间(分钟)',
    `is_overnight` TINYINT DEFAULT 0 COMMENT '是否跨天: 0=否, 1=是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=停用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `shift_code`, `deleted`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤班次表';

-- 7. 假期类型表 (leave_types)
-- 用途: 定义各种假期类型及规则
CREATE TABLE IF NOT EXISTS `leave_types` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `type_code` VARCHAR(50) NOT NULL COMMENT '假期类型编码',
    `type_name` VARCHAR(100) NOT NULL COMMENT '假期类型名称',
    `unit` VARCHAR(10) NOT NULL DEFAULT 'day' COMMENT '计量单位: day=天, hour=小时',
    `is_paid` TINYINT NOT NULL DEFAULT 1 COMMENT '是否带薪: 1=是, 0=否',
    `annual_quota` DECIMAL(5,2) DEFAULT NULL COMMENT '年度额度',
    `min_apply_days` DECIMAL(5,2) DEFAULT 0.5 COMMENT '最小申请时长',
    `max_apply_days` DECIMAL(5,2) DEFAULT NULL COMMENT '最大连续申请时长',
    `need_proof` TINYINT DEFAULT 0 COMMENT '是否需要证明: 0=否, 1=是',
    `proof_threshold_days` DECIMAL(5,2) DEFAULT NULL COMMENT '需要证明的天数阈值',
    `description` TEXT DEFAULT NULL COMMENT '说明',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=停用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `type_code`, `deleted`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='假期类型表';

-- 8. 薪酬项目表 (salary_items)
-- 用途: 定义工资条中的各个项目
CREATE TABLE IF NOT EXISTS `salary_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `item_code` VARCHAR(50) NOT NULL COMMENT '项目编码',
    `item_name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `category` VARCHAR(20) NOT NULL COMMENT '分类: base=基本工资, allowance=津贴, bonus=奖金, deduction=扣款, tax=税费',
    `is_taxable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否计税: 1=是, 0=否',
    `is_fixed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否固定金额: 1=是, 0=否',
    `default_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '默认金额',
    `calculation_formula` TEXT DEFAULT NULL COMMENT '计算公式',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=停用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `item_code`, `deleted`),
    KEY `idx_tenant_category` (`tenant_id`, `category`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='薪酬项目表';
