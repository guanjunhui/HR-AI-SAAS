-- HR-AI-SAAS 组织鉴权服务数据库 Schema
-- 执行方式: mysql -uroot -phr_ai_2025 hr_ai_saas < schema-org.sql

-- ============================================
-- 系统用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` VARCHAR(64) NOT NULL DEFAULT 'tenant_default' COMMENT '租户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名（登录账号）',
    `password` VARCHAR(128) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    `role_id` BIGINT DEFAULT NULL COMMENT '角色ID',
    `org_unit_id` BIGINT DEFAULT NULL COMMENT '组织单元ID',
    `plan_type` VARCHAR(32) NOT NULL DEFAULT 'free' COMMENT '套餐类型: free/pro/enterprise',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_username` (`tenant_id`, `username`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_org_unit_id` (`org_unit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ============================================
-- 系统角色表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` VARCHAR(64) NOT NULL DEFAULT 'tenant_default' COMMENT '租户ID',
    `name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(64) NOT NULL COMMENT '角色编码（唯一标识）',
    `description` VARCHAR(256) DEFAULT NULL COMMENT '角色描述',
    `permissions` JSON DEFAULT NULL COMMENT '权限列表（JSON数组）',
    `data_scope` TINYINT NOT NULL DEFAULT 1 COMMENT '数据范围: 1=全部, 2=本部门及子部门, 3=仅本部门, 4=仅本人',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- ============================================
-- 组织单元表（部门/公司/团队）
-- ============================================
CREATE TABLE IF NOT EXISTS `org_unit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` VARCHAR(64) NOT NULL DEFAULT 'tenant_default' COMMENT '租户ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID（0表示顶级）',
    `name` VARCHAR(128) NOT NULL COMMENT '组织名称',
    `code` VARCHAR(64) NOT NULL COMMENT '组织编码（唯一标识）',
    `type` VARCHAR(32) NOT NULL DEFAULT 'dept' COMMENT '类型: company=公司, dept=部门, team=团队',
    `path` VARCHAR(512) DEFAULT NULL COMMENT '组织全路径（如: /1/2/3/）',
    `level` INT NOT NULL DEFAULT 1 COMMENT '层级深度（从1开始）',
    `leader_id` BIGINT DEFAULT NULL COMMENT '负责人用户ID',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_path` (`path`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织单元表';

-- ============================================
-- 审计日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` VARCHAR(64) NOT NULL DEFAULT 'tenant_default' COMMENT '租户ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(64) DEFAULT NULL COMMENT '用户名',
    `action` VARCHAR(32) NOT NULL COMMENT '操作类型: LOGIN/LOGOUT/CREATE/UPDATE/DELETE/EXPORT/IMPORT',
    `resource` VARCHAR(64) NOT NULL COMMENT '资源类型: USER/ROLE/ORG/KNOWLEDGE/AGENT等',
    `resource_id` VARCHAR(64) DEFAULT NULL COMMENT '资源ID',
    `resource_name` VARCHAR(256) DEFAULT NULL COMMENT '资源名称',
    `detail` JSON DEFAULT NULL COMMENT '操作详情（JSON格式）',
    `result` VARCHAR(16) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果: SUCCESS/FAILURE',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `ip` VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
    `user_agent` VARCHAR(512) DEFAULT NULL COMMENT 'User-Agent',
    `duration` BIGINT DEFAULT NULL COMMENT '请求耗时（毫秒）',
    `trace_id` VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_action` (`action`),
    KEY `idx_resource` (`resource`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';
