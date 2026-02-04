-- 角色权限迁移脚本（从 sys_role.permissions JSON 迁移到 sys_role_permission）
-- 执行方式: mysql -uroot -phr_ai_2025 hr_ai_saas < migrate-role-permissions.sql

INSERT INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_code`)
SELECT r.tenant_id, r.id, jt.permission_code
FROM `sys_role` r
JOIN JSON_TABLE(r.permissions, '$[*]'
    COLUMNS (
        permission_code VARCHAR(128) PATH '$'
    )
) jt
WHERE r.permissions IS NOT NULL
  AND JSON_LENGTH(r.permissions) > 0
ON DUPLICATE KEY UPDATE `permission_code` = VALUES(`permission_code`);
