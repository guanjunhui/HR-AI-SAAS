-- =====================================================
-- Phase 1: Core HR 初始化数据
-- 版本: V1.1
-- 作者: HR-AI-SAAS
-- 日期: 2026-02-05
-- =====================================================

USE hr_ai_saas;

-- 初始化考勤班次
INSERT INTO `attendance_shifts` (`tenant_id`, `shift_code`, `shift_name`, `start_time`, `end_time`, `work_hours`, `break_start`, `break_end`, `flexible_minutes`) VALUES
('tenant_default', 'STANDARD', '标准班', '09:00:00', '18:00:00', 8.00, '12:00:00', '13:00:00', 0),
('tenant_default', 'FLEXIBLE', '弹性班', '08:00:00', '17:00:00', 8.00, '12:00:00', '13:00:00', 60),
('tenant_default', 'NIGHT', '夜班', '22:00:00', '06:00:00', 8.00, '02:00:00', '02:30:00', 0);

-- 初始化假期类型
INSERT INTO `leave_types` (`tenant_id`, `type_code`, `type_name`, `unit`, `is_paid`, `annual_quota`, `min_apply_days`, `max_apply_days`, `need_proof`, `proof_threshold_days`, `description`) VALUES
('tenant_default', 'ANNUAL', '年假', 'day', 1, 5.00, 0.5, 15.00, 0, NULL, '法定年休假，根据工龄计算'),
('tenant_default', 'SICK', '病假', 'day', 1, 10.00, 0.5, 180.00, 1, 3.00, '需提供医院证明'),
('tenant_default', 'PERSONAL', '事假', 'day', 0, NULL, 0.5, 30.00, 0, NULL, '无薪事假'),
('tenant_default', 'MARRIAGE', '婚假', 'day', 1, 3.00, 1.0, 3.00, 1, 1.00, '需提供结婚证'),
('tenant_default', 'MATERNITY', '产假', 'day', 1, 98.00, 1.0, 158.00, 1, 1.00, '按国家规定执行'),
('tenant_default', 'PATERNITY', '陪产假', 'day', 1, 15.00, 1.0, 15.00, 1, 1.00, '配偶生育时可用'),
('tenant_default', 'BEREAVEMENT', '丧假', 'day', 1, 3.00, 1.0, 5.00, 1, 1.00, '直系亲属去世'),
('tenant_default', 'COMPENSATORY', '调休', 'hour', 1, NULL, 1.0, NULL, 0, NULL, '加班调休');

-- 初始化薪酬项目
INSERT INTO `salary_items` (`tenant_id`, `item_code`, `item_name`, `category`, `is_taxable`, `is_fixed`, `default_amount`, `sort_order`) VALUES
-- 基本工资
('tenant_default', 'BASE_SALARY', '基本工资', 'base', 1, 0, NULL, 1),
('tenant_default', 'POST_SALARY', '岗位工资', 'base', 1, 0, NULL, 2),
-- 津贴
('tenant_default', 'MEAL_ALLOWANCE', '餐补', 'allowance', 0, 1, 500.00, 10),
('tenant_default', 'TRANSPORT_ALLOWANCE', '交通补贴', 'allowance', 0, 1, 300.00, 11),
('tenant_default', 'PHONE_ALLOWANCE', '通讯补贴', 'allowance', 0, 1, 200.00, 12),
('tenant_default', 'HOUSING_ALLOWANCE', '住房补贴', 'allowance', 1, 0, NULL, 13),
-- 奖金
('tenant_default', 'PERFORMANCE_BONUS', '绩效奖金', 'bonus', 1, 0, NULL, 20),
('tenant_default', 'PROJECT_BONUS', '项目奖金', 'bonus', 1, 0, NULL, 21),
('tenant_default', 'YEAR_END_BONUS', '年终奖', 'bonus', 1, 0, NULL, 22),
-- 扣款
('tenant_default', 'SOCIAL_INSURANCE', '社保个人部分', 'deduction', 0, 0, NULL, 30),
('tenant_default', 'HOUSING_FUND', '公积金个人部分', 'deduction', 0, 0, NULL, 31),
('tenant_default', 'ABSENCE_DEDUCTION', '缺勤扣款', 'deduction', 0, 0, NULL, 32),
-- 税费
('tenant_default', 'INCOME_TAX', '个人所得税', 'tax', 0, 0, NULL, 40);

-- 初始化示例岗位
INSERT INTO `positions` (`tenant_id`, `position_code`, `position_name`, `position_level`, `job_family`, `description`, `requirements`) VALUES
('tenant_default', 'CEO', '首席执行官', 10, '管理', '公司最高管理者', '10年以上管理经验'),
('tenant_default', 'CTO', '首席技术官', 10, '技术', '技术团队最高负责人', '10年以上技术管理经验'),
('tenant_default', 'HR_DIRECTOR', '人力资源总监', 9, '人力资源', '人力资源部门负责人', '8年以上人力资源管理经验'),
('tenant_default', 'ENG_MANAGER', '技术经理', 7, '技术', '技术团队管理者', '5年以上技术经验，2年以上管理经验'),
('tenant_default', 'SENIOR_DEV', '高级开发工程师', 6, '技术', '高级软件开发', '5年以上开发经验'),
('tenant_default', 'DEV', '开发工程师', 4, '技术', '软件开发', '2年以上开发经验'),
('tenant_default', 'JUNIOR_DEV', '初级开发工程师', 2, '技术', '初级软件开发', '计算机相关专业'),
('tenant_default', 'HR_SPECIALIST', '人事专员', 3, '人力资源', '人事管理执行', '1年以上人事工作经验'),
('tenant_default', 'RECRUITER', '招聘专员', 3, '人力资源', '招聘执行', '1年以上招聘经验'),
('tenant_default', 'ACCOUNTANT', '会计', 4, '财务', '财务核算', '2年以上财务经验，会计证');
