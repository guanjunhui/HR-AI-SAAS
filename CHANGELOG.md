# 变更记录

用途：记录每次代码变更内容，新增条目置顶。

格式：
- 日期（YYYY-MM-DD）
- 范围
- 概要
- 新增/修改/删除文件
- 测试（如有）

## 2026-02-04
范围：M01 组织与权限（`hr-org-service`）。
概要：移除权限硬编码，改为从数据库菜单/权限关系表读取；新增菜单与角色权限表及迁移脚本；更新角色权限写入逻辑并补齐单测。

新增文件：
- `hr-org-service/src/main/java/com/hrai/org/entity/SysMenu.java`
- `hr-org-service/src/main/java/com/hrai/org/entity/SysRolePermission.java`
- `hr-org-service/src/main/java/com/hrai/org/mapper/SysMenuMapper.java`
- `hr-org-service/src/main/java/com/hrai/org/mapper/SysRolePermissionMapper.java`
- `hr-org-service/src/main/resources/db/migrate-role-permissions.sql`

修改文件：
- `hr-org-service/src/main/java/com/hrai/org/service/impl/PermissionServiceImpl.java`
- `hr-org-service/src/main/java/com/hrai/org/service/impl/RoleServiceImpl.java`
- `hr-org-service/src/main/resources/db/schema-org.sql`
- `hr-org-service/src/main/resources/db/init-org-data.sql`
- `hr-org-service/src/test/java/com/hrai/org/service/impl/PermissionServiceImplTest.java`
- `hr-org-service/src/test/java/com/hrai/org/service/impl/RoleServiceImplTest.java`

删除文件：
- `hr-org-service/src/main/java/com/hrai/org/util/PermissionCatalog.java`

测试：
- `mvn -q test`
- `mvn -q -DskipTests=true package`

## 2026-02-04
范围：M01 组织与权限（`hr-org-service`）。
概要：修复 JDBC 连接编码配置为 `UTF-8`，避免 `utf8mb4` 触发的 UnsupportedEncodingException。

修改文件：
- `hr-org-service/src/main/resources/application.yml`

测试：
- `mvn -pl hr-org-service -am test`
- `mvn -pl hr-org-service -am -DskipTests=true package`

## 2026-02-04
范围：仓库文档。
概要：新增贡献指南 `AGENTS.md`。

新增文件：
- `AGENTS.md`

测试：
- 未运行（仅文档变更）。

## 2026-02-04
范围：M01 组织与权限（`hr-ai-web`）。
概要：新增组织/用户/角色/审计前端页面、类型与服务；完善路由与菜单结构；前端路由懒加载并优化构建分包以消除体积告警；修复组织权限接口响应中文乱码并调整测试运行配置；补充数据库连接 utf8mb4 编码配置。

新增文件：
- `hr-ai-web/src/components/ProtectedRoute.tsx`
- `hr-ai-web/src/pages/org/audit/index.tsx`
- `hr-ai-web/src/pages/org/roles/detail.tsx`
- `hr-ai-web/src/pages/org/roles/index.tsx`
- `hr-ai-web/src/pages/org/units/detail.tsx`
- `hr-ai-web/src/pages/org/units/index.tsx`
- `hr-ai-web/src/pages/org/users/detail.tsx`
- `hr-ai-web/src/pages/org/users/index.tsx`
- `hr-ai-web/src/pages/placeholder.tsx`
- `hr-ai-web/src/services/audit.ts`
- `hr-ai-web/src/services/org.ts`
- `hr-ai-web/src/services/permission.ts`
- `hr-ai-web/src/services/role.ts`
- `hr-ai-web/src/services/user.ts`
- `hr-ai-web/src/types/audit.ts`
- `hr-ai-web/src/types/common.ts`
- `hr-ai-web/src/types/org.ts`
- `hr-ai-web/src/types/role.ts`
- `hr-ai-web/src/types/user.ts`
- `hr-ai-web/src/utils/dicts.ts`
- `hr-org-service/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`

修改文件：
- `hr-ai-web/src/App.tsx`
- `hr-ai-web/src/config/routes.tsx`
- `hr-ai-web/src/layouts/BasicLayout.tsx`
- `hr-ai-web/vite.config.ts`
- `hr-gateway/src/main/java/com/hrai/gateway/filter/AuthGlobalFilter.java`
- `hr-org-service/src/main/resources/application.yml`
- `pom.xml`

测试：
- `npm run lint`
- `npm run build`
- `mvn -q -pl hr-gateway,hr-org-service -am test`
- `mvn -q -pl hr-gateway,hr-org-service -am -DskipTests=true package`
- `mvn -q -pl hr-org-service -am test`
- `mvn -q -pl hr-org-service -am -DskipTests=true package`

数据：
- 全库洗库后，补充多租户组织/角色/用户数据（`tenant_default`、`tenant_demo`）。
- 角色新增（两租户）：`finance_manager`、`it_admin`、`dept_manager`、`recruiter`、`auditor`。
- 角色补齐（`tenant_demo`）：`super_admin`、`tenant_admin`、`hr_specialist`、`user`。
- 组织新增（`tenant_default`）：`dept_fin`、`dept_marketing`、`dept_ops`、`dept_cs`、`dept_data`、`dept_hrssc`、`team_tax`、`team_cash`、`team_brand`、`team_growth`、`team_bi`、`team_data_eng`。
- 组织新增（`tenant_demo`）：`dept_demo_rd`、`dept_demo_sales`、`dept_demo_hr`、`dept_demo_fin`、`dept_demo_ops`、`team_demo_backend`、`team_demo_frontend`、`team_demo_sales_a`、`team_demo_ops_a`（含根组织 `company_root`）。
- 用户新增（`tenant_default`）：`fin_mgr`、`fin_cashier`、`fin_tax`、`mkt_mgr`、`mkt_brand`、`mkt_growth`、`ops_mgr`、`cs_lead`、`data_lead`、`data_bi`、`data_eng`、`it_admin_1`、`hr_recruiter`、`auditor_1`。
- 用户新增（`tenant_demo`）：`demo_admin`、`demo_hr`、`demo_recruiter`、`demo_sales_mgr`、`demo_sales_1`、`demo_rd_mgr`、`demo_backend`、`demo_frontend`、`demo_fin`、`demo_auditor`、`demo_ops`。
- 组织层级扩展（两租户）：新增 `事业部(一级)` → `部门(二级)` → `团队(三级)` → `小队(四级)`，示例：`tdf_div_a` → `tdf_div_a_prod` → `tdf_div_a_prod_t1` → `tdf_div_a_prod_t1_s1`。
- 用户量扩充：`tenant_default` 137 人、`tenant_demo` 131 人（均 100+），随机分配到团队/小队节点。

## 2026-02-04
范围：M01 组织与权限（`hr-org-service`）。
概要：新增组织/用户/角色/审计接口；补充数据范围控制；提供权限目录与权限校验接口；为核心服务补充单测。

新增文件：
- `hr-org-service/src/main/java/com/hrai/org/config/MyBatisPlusConfig.java`
- `hr-org-service/src/main/java/com/hrai/org/config/WebConfig.java`
- `hr-org-service/src/main/java/com/hrai/org/controller/AuditLogController.java`
- `hr-org-service/src/main/java/com/hrai/org/controller/OrgUnitController.java`
- `hr-org-service/src/main/java/com/hrai/org/controller/PermissionController.java`
- `hr-org-service/src/main/java/com/hrai/org/controller/RoleController.java`
- `hr-org-service/src/main/java/com/hrai/org/controller/UserController.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/AuditLogDetailResponse.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/AuditLogQueryRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/OrgUnitCreateRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/OrgUnitDetailResponse.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/OrgUnitTreeNode.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/OrgUnitUpdateRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/PageResponse.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/RoleCreateRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/RoleDetailResponse.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/RolePermissionsUpdateRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/RoleUpdateRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/UserCreateRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/UserDetailResponse.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/UserPasswordUpdateRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/UserQueryRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/UserRoleUpdateRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/UserStatusUpdateRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/dto/UserUpdateRequest.java`
- `hr-org-service/src/main/java/com/hrai/org/interceptor/TenantInterceptor.java`
- `hr-org-service/src/main/java/com/hrai/org/service/AuditLogService.java`
- `hr-org-service/src/main/java/com/hrai/org/service/DataScopeContext.java`
- `hr-org-service/src/main/java/com/hrai/org/service/DataScopeService.java`
- `hr-org-service/src/main/java/com/hrai/org/service/OrgUnitService.java`
- `hr-org-service/src/main/java/com/hrai/org/service/PermissionService.java`
- `hr-org-service/src/main/java/com/hrai/org/service/RoleService.java`
- `hr-org-service/src/main/java/com/hrai/org/service/UserService.java`
- `hr-org-service/src/main/java/com/hrai/org/service/impl/AuditLogServiceImpl.java`
- `hr-org-service/src/main/java/com/hrai/org/service/impl/DataScopeServiceImpl.java`
- `hr-org-service/src/main/java/com/hrai/org/service/impl/OrgUnitServiceImpl.java`
- `hr-org-service/src/main/java/com/hrai/org/service/impl/PermissionServiceImpl.java`
- `hr-org-service/src/main/java/com/hrai/org/service/impl/RoleServiceImpl.java`
- `hr-org-service/src/main/java/com/hrai/org/service/impl/UserServiceImpl.java`
- `hr-org-service/src/main/java/com/hrai/org/util/PermissionCatalog.java`
- `hr-org-service/src/test/java/com/hrai/org/service/impl/AuditLogServiceImplTest.java`
- `hr-org-service/src/test/java/com/hrai/org/service/impl/DataScopeServiceImplTest.java`
- `hr-org-service/src/test/java/com/hrai/org/service/impl/OrgUnitServiceImplTest.java`
- `hr-org-service/src/test/java/com/hrai/org/service/impl/PermissionServiceImplTest.java`
- `hr-org-service/src/test/java/com/hrai/org/service/impl/RoleServiceImplTest.java`
- `hr-org-service/src/test/java/com/hrai/org/service/impl/UserServiceImplTest.java`

删除文件：
- `AGENTS.md`

测试：
- 未运行（本次仅更新变更记录）。
