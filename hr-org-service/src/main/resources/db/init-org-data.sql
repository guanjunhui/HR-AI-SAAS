-- HR-AI-SAAS 组织鉴权服务初始化数据
-- 执行方式: mysql -uroot -phr_ai_2025 hr_ai_saas < init-org-data.sql

-- ============================================
-- 初始化角色
-- ============================================
INSERT INTO `sys_role` (`tenant_id`, `name`, `code`, `description`, `permissions`, `data_scope`, `status`, `sort_order`)
VALUES
    ('tenant_default', '超级管理员', 'super_admin', '系统超级管理员，拥有所有权限', '["*"]', 1, 1, 1),
    ('tenant_default', '租户管理员', 'tenant_admin', '租户管理员，管理租户内所有资源', '["user:*", "role:*", "org:*", "agent:*", "knowledge:*"]', 1, 1, 2),
    ('tenant_default', 'HR 专员', 'hr_specialist', 'HR 专员，使用 HR 相关功能', '["user:read", "agent:use", "knowledge:read"]', 3, 1, 3),
    ('tenant_default', '普通用户', 'user', '普通用户，基本使用权限', '["agent:use", "chat:use"]', 4, 1, 4)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- ============================================
-- 初始化组织架构
-- ============================================
INSERT INTO `org_unit` (`tenant_id`, `parent_id`, `name`, `code`, `type`, `path`, `level`, `sort_order`, `status`)
VALUES
    ('tenant_default', 0, 'HR AI 科技有限公司', 'company_root', 'company', '/1/', 1, 1, 1),
    ('tenant_default', 1, '技术部', 'dept_tech', 'dept', '/1/2/', 2, 1, 1),
    ('tenant_default', 1, '人力资源部', 'dept_hr', 'dept', '/1/3/', 2, 2, 1),
    ('tenant_default', 1, '产品部', 'dept_product', 'dept', '/1/4/', 2, 3, 1),
    ('tenant_default', 2, '后端开发组', 'team_backend', 'team', '/1/2/5/', 3, 1, 1),
    ('tenant_default', 2, '前端开发组', 'team_frontend', 'team', '/1/2/6/', 3, 2, 1),
    ('tenant_default', 2, 'AI 算法组', 'team_ai', 'team', '/1/2/7/', 3, 3, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- ============================================
-- 初始化管理员用户
-- 密码: admin123 (BCrypt 加密)
-- ============================================
INSERT INTO `sys_user` (`tenant_id`, `username`, `password`, `real_name`, `email`, `phone`, `status`, `role_id`, `org_unit_id`, `plan_type`)
VALUES
    ('tenant_default', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@hrai.com', '13800000000', 1, 1, 1, 'enterprise'),
    ('tenant_default', 'hr_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'HR 管理员', 'hr@hrai.com', '13800000001', 1, 3, 3, 'pro'),
    ('tenant_default', 'test_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', 'test@hrai.com', '13800000002', 1, 4, 5, 'free')
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`);

-- 说明:
-- 默认密码: admin123
-- BCrypt hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
