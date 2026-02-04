-- HR-AI-SAAS 组织鉴权服务初始化数据
-- 执行方式: mysql -uroot -phr_ai_2025 hr_ai_saas < init-org-data.sql

-- ============================================
-- 初始化角色
-- ============================================
INSERT INTO `sys_role` (`tenant_id`, `name`, `code`, `description`, `data_scope`, `status`, `sort_order`)
VALUES
    ('tenant_default', '超级管理员', 'super_admin', '系统超级管理员，拥有所有权限', 1, 1, 1),
    ('tenant_default', '租户管理员', 'tenant_admin', '租户管理员，管理租户内所有资源', 1, 1, 2),
    ('tenant_default', 'HR 专员', 'hr_specialist', 'HR 专员，使用 HR 相关功能', 3, 1, 3),
    ('tenant_default', '普通用户', 'user', '普通用户，基本使用权限', 4, 1, 4)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- ============================================
-- 初始化菜单/页面权限
-- ============================================
INSERT INTO `sys_menu` (`tenant_id`, `parent_id`, `name`, `type`, `path`, `permission_code`, `status`, `sort_order`)
VALUES
    ('tenant_default', 0, '全部权限', 'ACTION', NULL, '*', 1, 1),
    ('tenant_default', 0, '组织与权限', 'MENU', '/org', 'org:*', 1, 10),
    ('tenant_default', 0, '组织架构', 'PAGE', '/org/units', 'org:unit:read', 1, 11),
    ('tenant_default', 0, '组织架构-编辑', 'ACTION', NULL, 'org:unit:write', 1, 12),
    ('tenant_default', 0, '用户管理', 'PAGE', '/org/users', 'org:user:read', 1, 20),
    ('tenant_default', 0, '用户管理-编辑', 'ACTION', NULL, 'org:user:write', 1, 21),
    ('tenant_default', 0, '用户管理-查看(通用)', 'ACTION', NULL, 'user:read', 1, 22),
    ('tenant_default', 0, '角色管理', 'PAGE', '/org/roles', 'org:role:read', 1, 30),
    ('tenant_default', 0, '角色管理-编辑', 'ACTION', NULL, 'org:role:write', 1, 31),
    ('tenant_default', 0, '审计日志', 'PAGE', '/org/audit', 'org:audit:read', 1, 40),
    ('tenant_default', 0, '岗位管理-查看', 'ACTION', NULL, 'hr:position:read', 1, 50),
    ('tenant_default', 0, '岗位管理-编辑', 'ACTION', NULL, 'hr:position:write', 1, 51),
    ('tenant_default', 0, '编制管理-查看', 'ACTION', NULL, 'hr:headcount:read', 1, 52),
    ('tenant_default', 0, '编制管理-编辑', 'ACTION', NULL, 'hr:headcount:write', 1, 53),
    ('tenant_default', 0, '员工管理-查看', 'ACTION', NULL, 'hr:employee:read', 1, 54),
    ('tenant_default', 0, '员工管理-编辑', 'ACTION', NULL, 'hr:employee:write', 1, 55),
    ('tenant_default', 0, '人事事件-查看', 'ACTION', NULL, 'hr:event:read', 1, 56),
    ('tenant_default', 0, '人事事件-编辑', 'ACTION', NULL, 'hr:event:write', 1, 57),
    ('tenant_default', 0, '人事事件-审批', 'ACTION', NULL, 'hr:event:approve', 1, 58),
    ('tenant_default', 0, 'AI 能力中心', 'PAGE', '/ai', 'agent:use', 1, 60),
    ('tenant_default', 0, 'AI 能力中心-管理', 'ACTION', NULL, 'agent:*', 1, 61),
    ('tenant_default', 0, '知识库-管理', 'ACTION', NULL, 'knowledge:*', 1, 70),
    ('tenant_default', 0, '知识库-查看', 'ACTION', NULL, 'knowledge:read', 1, 71),
    ('tenant_default', 0, '知识库-编辑', 'ACTION', NULL, 'knowledge:write', 1, 72),
    ('tenant_default', 0, '聊天使用', 'ACTION', NULL, 'chat:use', 1, 80),
    ('tenant_default', 0, '用户模块-管理', 'ACTION', NULL, 'user:*', 1, 90),
    ('tenant_default', 0, '角色模块-管理', 'ACTION', NULL, 'role:*', 1, 91)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `type` = VALUES(`type`), `path` = VALUES(`path`),
    `status` = VALUES(`status`), `sort_order` = VALUES(`sort_order`);

-- ============================================
-- 初始化角色权限关联
-- ============================================
INSERT INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_code`)
SELECT 'tenant_default', r.id, rp.permission_code
FROM `sys_role` r
JOIN (
    SELECT 'super_admin' AS role_code, '*' AS permission_code
    UNION ALL SELECT 'tenant_admin', 'user:*'
    UNION ALL SELECT 'tenant_admin', 'role:*'
    UNION ALL SELECT 'tenant_admin', 'org:*'
    UNION ALL SELECT 'tenant_admin', 'agent:*'
    UNION ALL SELECT 'tenant_admin', 'knowledge:*'
    UNION ALL SELECT 'hr_specialist', 'user:read'
    UNION ALL SELECT 'hr_specialist', 'agent:use'
    UNION ALL SELECT 'hr_specialist', 'knowledge:read'
    UNION ALL SELECT 'user', 'agent:use'
    UNION ALL SELECT 'user', 'chat:use'
) rp ON r.code = rp.role_code
WHERE r.tenant_id = 'tenant_default' AND r.deleted = 0
ON DUPLICATE KEY UPDATE `permission_code` = VALUES(`permission_code`);

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
    ('tenant_default', 'admin', '$2a$10$cVqnj4XHelOFBrctgM6KFe6OOBhBt.GuJetm2GBxJFNiYF8boYuyq', '系统管理员', 'admin@hrai.com', '13800000000', 1, 1, 1, 'enterprise'),
    ('tenant_default', 'hr_admin', '$2a$10$cVqnj4XHelOFBrctgM6KFe6OOBhBt.GuJetm2GBxJFNiYF8boYuyq', 'HR 管理员', 'hr@hrai.com', '13800000001', 1, 3, 3, 'pro'),
    ('tenant_default', 'test_user', '$2a$10$cVqnj4XHelOFBrctgM6KFe6OOBhBt.GuJetm2GBxJFNiYF8boYuyq', '测试用户', 'test@hrai.com', '13800000002', 1, 4, 5, 'free')
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`);

-- 说明:
-- 默认密码: admin123
-- BCrypt hash: $2a$10$cVqnj4XHelOFBrctgM6KFe6OOBhBt.GuJetm2GBxJFNiYF8boYuyq
