# hr-business-service 功能符合性全量审计报告（重跑）

> 审计时间：2026-02-06 23:28:00 GMT+8  
> 审计对象：`hr-business-service`  
> 对照基线：`docs/HR_AI_SaaS需求清单.xlsx` + `docs/实现计划/*.md`

---

## 1. 审计方法与证据

### 1.1 需求基线提取（来自 xlsx）
证据（命令输出）：
```text
total_requirements 72
modules:
M01 组织与权限 5
M02 Core HR 15
M03 招聘管理(ATS) 8
M04 入职与Onboarding 5
M05 考勤与假勤 6
M06 薪酬与薪资 5
M07 绩效管理 7
M08 员工关系/工单 5
M09 报表与分析 9
M10 AI 能力中心 7
```

### 1.2 服务实现范围判定
证据：`hr-business-service/src/main/java/com/hrai/business/BusinessServiceApplication.java:12`
```java
 * - 员工管理 (Employee)
 * - 岗位管理 (Position)
 * - 任职事件 (EmploymentEvent)
 * - 编制管理 (Headcount)
 * - 考勤班次 (AttendanceShift)
 * - 假期类型 (LeaveType)
 * - 薪酬项目 (SalaryItem)
```

判定口径：
- 重点核验模块：`M02`、`M05`、`M06`。
- 其他模块（`M01/M03/M04/M07/M08/M09/M10`）在本服务代码中无对应 Controller，标记 `Out of Scope`（服务边界外）。

证据（无对应 Controller）：
```text
rg "class (OrgUnitController|UserController|RoleController|AuditLogController|JobRequisitionController|CandidateController|OnboardingController|PerformanceCycleController|TicketController|ReportController|AIChatController|ShiftController|SalaryArchiveController|PayrollRunController)" ../hr-business-service/src/main/java
# (无结果)
```

---

## 2. 核心功能完整性（RQ逐项）

### 2.1 M02 Core HR（RQ0006-RQ0020）

| RQ | 需求点 | 结论 | 证据 |
|---|---|---|---|
| RQ0006 | 岗位管理列表 | 部分实现 | `PositionController` 仅 CRUD+enabled，缺导出/在岗人员视图：`hr-business-service/src/main/java/com/hrai/business/controller/PositionController.java:31` |
| RQ0007 | 岗位详情 | 部分实现 | 有详情接口，缺“导出在岗员工”：`hr-business-service/src/main/java/com/hrai/business/controller/PositionController.java:47` |
| RQ0008 | 编制配置 | 部分实现 | 有 CRUD，无 `/summary`：`hr-business-service/src/main/java/com/hrai/business/controller/HeadcountController.java:29`；规划要求 `/api/hr/headcounts/summary`：`docs/实现计划/10-API接口清单.md:130` |
| RQ0009 | 花名册 | 部分实现 | 有列表/新增/改删，无导入导出：`hr-business-service/src/main/java/com/hrai/business/controller/EmployeeController.java:29` |
| RQ0010 | 员工档案详情 | 部分实现 | 仅 `/employees/{id}`，缺 `/employees/{id}/detail` 与 detail 更新：规划见 `docs/实现计划/10-API接口清单.md:141` |
| RQ0011 | 入职任职事件创建 | 不符合 | 创建接口强制 `employeeId` 非空：`hr-business-service/src/main/java/com/hrai/business/dto/employmentevent/EmploymentEventCreateRequest.java:11`，与规划“入职可为空员工ID”冲突：`docs/实现计划/01-Phase1-基础业务模块.md:288` |
| RQ0012 | 入职任职事件详情操作 | 不符合 | 缺 withdraw/reject 独立接口；现为 approve(boolean)+cancel：`hr-business-service/src/main/java/com/hrai/business/controller/EmploymentEventController.java:72` |
| RQ0013 | 转正事件创建 | 部分实现 | 统一 `create` 可承载 `regular`，但无专用流程约束：`EmploymentEventServiceImpl.java:114` |
| RQ0014 | 转正事件详情操作 | 不符合 | 同 RQ0012，缺 withdraw/打印导出/重新发起 |
| RQ0015 | 调岗事件创建 | 部分实现 | 统一 `create` 支持 `transfer`，但缺流程约束 |
| RQ0016 | 调岗事件详情操作 | 不符合 | 同 RQ0012 |
| RQ0017 | 调薪事件创建 | 部分实现 | 统一 `create` 支持 `salary_change` |
| RQ0018 | 调薪事件详情操作 | 不符合 | 同 RQ0012 |
| RQ0019 | 离职事件创建 | 部分实现 | 统一 `create` 支持 `resignation` |
| RQ0020 | 离职事件详情操作 | 不符合 | 同 RQ0012 |

关键运行态证据（端到端脚本）：
```text
[health] 200 {"status":"UP"}
[create position] code=200
[update position] code=500
[create employee] code=200
[create employment event] code=200
[submit event] code=500
[approve event] code=500
[get employee] code=500
[create headcount] code=500
```

错误根因证据：
- `@PathVariable` 参数名不可反射导致 500：
```text
Name for argument of type [java.lang.Long] not specified, and parameter name information not available via reflection.
```
来源：运行日志（`GlobalExceptionHandler`）与以下代码形态一致：
`hr-business-service/src/main/java/com/hrai/business/controller/PositionController.java:48`
```java
public Result<PositionDetailResponse> getById(@PathVariable Long id)
```

- 编制创建 `tenant_id` 为空导致 500：
```text
Field 'tenant_id' doesn't have a default value
Parameters: null, 1(Long), 348(Long), 2026(Integer), 1(Integer)
```
对应代码：`hr-business-service/src/main/java/com/hrai/business/service/impl/HeadcountServiceImpl.java:86`
```java
String tenantId = TenantContext.getTenantId();
```

### 2.2 M05 考勤与假勤（RQ0034-RQ0039）

结论：`未实现`（仅基础实体/Mapper，缺 Controller/Service 流程）。

证据：
- 需求要求 API：`docs/实现计划/03-Phase3-考勤薪酬.md:576`（`/api/attendance/shifts` 等）
- 实际 Controller 数量仅 4：
```text
find ../hr-business-service/src/main/java/com/hrai/business/controller -type f | wc -l
4
```
- 仅有数据模型：`hr-business-service/src/main/java/com/hrai/business/entity/AttendanceShift.java:12`、`hr-business-service/src/main/java/com/hrai/business/entity/LeaveType.java:11`

### 2.3 M06 薪酬与薪资（RQ0040-RQ0044）

结论：`未实现`（仅 `salary_items` 基础模型，缺薪资档案/算薪批次/工资条全链路）。

证据：
- 需求 API：`docs/实现计划/03-Phase3-考勤薪酬.md:635`（`/api/payroll/items`、`/api/payroll/archives`、`/api/payroll/runs`）
- 实际无 `SalaryArchiveController/PayrollRunController/PayslipController`（全文检索无结果）
- 迁移脚本仅包含 `salary_items`，无 `salary_archives/payroll_runs/payroll_details/payslips`：`hr-business-service/src/main/resources/db/migration/V1.0__create_core_hr_tables.sql:199`

---

## 3. 模块规划一致性检查（文档 vs 实现）

### 3.1 接口定义一致性

1. 路径前缀不一致：
- 规划：`/api/hr/*`（`docs/实现计划/10-API接口清单.md:119`）
- 实现：`/api/v1/hr/*`（`hr-business-service/src/main/java/com/hrai/business/controller/PositionController.java:19`）

2. 规划接口缺失（M02）：
- `GET /api/hr/headcounts/summary`（规划 `10-API接口清单.md:130`）
- `GET/PUT /api/hr/employees/{id}/detail`（规划 `10-API接口清单.md:141`）
- `POST /api/hr/employees/import`、`GET /api/hr/employees/export`（规划 `10-API接口清单.md:146`）
- `PUT /api/hr/events/{id}`、`POST /api/hr/events/{id}/reject`、`POST /api/hr/events/{id}/withdraw`（规划 `10-API接口清单.md:156`）

### 3.2 技术架构一致性

1. 状态机语义不一致：
- 规划状态：`withdrawn`（`docs/实现计划/01-Phase1-基础业务模块.md:421`）
- 实现状态：`cancelled`（`hr-business-service/src/main/java/com/hrai/business/enums/EmploymentEventStatus.java:11`）

2. 事件数据模型不一致：
- 规划含 `event_code/change_content/approve_records`：`docs/实现计划/01-Phase1-基础业务模块.md:286`
- 实现表结构无上述字段：`hr-business-service/src/main/resources/db/migration/V1.0__create_core_hr_tables.sql:118`

3. 编制模型不一致：
- 规划字段 `current_count/effective_date/expire_date`：`docs/实现计划/01-Phase1-基础业务模块.md:204`
- 实现字段 `actual_count/year/quarter/status`：`hr-business-service/src/main/resources/db/migration/V1.0__create_core_hr_tables.sql:73`

4. M05/M06 规划交付与实现差距：
- 规划 Phase3 共 12 张表（含 attendance_rules、payroll_runs 等）：`docs/实现计划/03-Phase3-考勤薪酬.md:16`
- 实际迁移仅 8 张基础表：`hr-business-service/src/main/resources/db/migration/V1.0__create_core_hr_tables.sql:12`

---

## 4. 关键业务流程测试

### 4.1 运行态验证

执行命令：
```bash
mvn -q -DskipTests spring-boot:run   # 在 hr-business-service 目录
```

结果：成功启动。证据：
```text
Tomcat started on port 8082 (http)
Started BusinessServiceApplication in 2.381 seconds
```

### 4.2 流程脚本结果

执行流程：健康检查 -> 创建岗位 -> 更新岗位 -> 创建员工 -> 创建任职事件 -> 提交/审批 -> 查询员工 -> 创建编制。

结果：
- 成功：健康检查、创建岗位、创建员工、创建任职事件。
- 失败：岗位更新、事件提交、事件审批、员工查询、编制创建（均返回 `code=500`）。

根因：
1. `@PathVariable` 名称缺失触发 Spring 参数解析异常。  
2. `HeadcountServiceImpl` 取租户时无 fallback，导致 `tenant_id` 写入空值。

---

## 5. 非功能性需求验证

### 5.1 安全要求
结论：`不符合`。

证据：
- API 规划要求权限点与 `Authorization`：`docs/实现计划/10-API接口清单.md:11`
- 本服务未发现安全配置/权限注解（检索无结果）：
```text
rg "SecurityConfig|@PreAuthorize|RequirePermission|spring-boot-starter-security" ../hr-business-service/src/main/java ../hr-business-service/pom.xml
# (无结果)
```

### 5.2 性能指标
结论：`不确定`（缺压测证据）。

已有证据：仅有基础分页实现，如 `selectPage`（`hr-business-service/src/main/java/com/hrai/business/service/impl/EmployeeServiceImpl.java:71`）。

建议执行（按优先级）：
1. `mvn -q -pl hr-business-service -am -DskipTests=true package`（已完成构建）
2. `wrk`/`k6` 对 `GET /api/v1/hr/employees`、`GET /api/v1/hr/employment-events` 做基准压测
3. 打开慢 SQL 日志并采集 95/99 分位

### 5.3 可扩展性
结论：`部分不符合`。

证据：
- 组织名称查询仍是 TODO（未接入跨服务）：`hr-business-service/src/main/java/com/hrai/business/service/impl/HeadcountServiceImpl.java:174`
- 未发现模块联动实现（`LK0001/LK0003/LK0004` 对应事件/集成类缺失）。

---

## 6. 自查（构建/测试）

按仓库规范执行：

1. 静态/格式检查插件识别  
命令：
```bash
rg -n "spotless|checkstyle|spotbugs" ../pom.xml ../hr-business-service/pom.xml || true
```
结论：未发现对应插件配置。

2. 单测  
命令A（失败）：
```bash
mvn -q -pl hr-business-service -am test
```
关键错误：
```text
Java 24 (68) is not supported by the current version of Byte Buddy...
Mockito cannot mock ... EmploymentEventStateMachine
```
命令B（通过）：
```bash
mvn -q -pl hr-business-service -am -Dnet.bytebuddy.experimental=true test
```
证据：`hr-business-service/target/surefire-reports/com.hrai.business.service.impl.EmploymentEventServiceImplTest.txt`
```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

3. 构建打包  
命令：
```bash
mvn -q -pl hr-business-service -am -DskipTests=true package
```
证据：产物存在 `hr-business-service/target/hr-business-service-1.0.0-SNAPSHOT.jar`

---

## 7. 差异分析与修复优先级

### P0（立即修复，阻断核心流程）
1. `@PathVariable` 参数解析异常（多个接口 500）。
- 证据：运行日志 `Name for argument ... not specified`。
- 建议：
  - 方案A：所有路径参数改为 `@PathVariable("id")`。
  - 方案B：在 `maven-compiler-plugin` 增加 `-parameters`。

2. 编制创建 `tenant_id` 为空。
- 证据：`Field 'tenant_id' doesn't have a default value`。
- 建议：`HeadcountServiceImpl` 对齐 `resolveTenantId()` fallback（与 `PositionServiceImpl`、`EmployeeServiceImpl` 一致），并在请求入口注入 TenantContext。

3. Headcount 读写缺租户隔离校验。
- 证据：`getById/update/delete` 仅按主键，不校验租户：`hr-business-service/src/main/java/com/hrai/business/service/impl/HeadcountServiceImpl.java:75`
- 建议：所有查询与写操作增加 tenant 条件。

### P1（高优先，需求不符合）
1. 补齐 M02 规划接口：`/headcounts/summary`、`/employees/{id}/detail`、员工导入导出、事件 reject/withdraw/update。
2. 任职事件模型与状态机对齐：支持 `withdrawn`、审批记录、事件编码、变更内容结构。
3. 入职事件放开 `employeeId` 前置强校验（按规划支持“入职时员工ID可为空”）。

### P2（中优先，模块缺失）
1. M05 完整落地：班次规则、打卡、请假、加班的 Controller/Service/状态机。  
2. M06 完整落地：薪资档案、算薪批次、工资条全链路。  
3. 数据库迁移补齐：`attendance_rules`、`attendance_records`、`leave_balances`、`leave_requests`、`overtime_requests`、`salary_archives`、`payroll_runs`、`payroll_details`、`payslips`。

### P3（优化项）
1. 接入鉴权与权限点控制（与 API 清单权限点一致）。
2. 增加性能基线与容量压测（员工列表、事件列表、算薪批次）。
3. 完成模块联动（LK0001/LK0003/LK0004）事件化实现与回滚策略。

---

## 8. 全量结论

- 总需求 72 项中，本服务重点范围（`M02+M05+M06`）共 26 项：
  - `已实现`：0
  - `部分实现`：9
  - `不符合`：6
  - `未实现`：11
- 其余 46 项属于本服务边界外模块（`M01/M03/M04/M07/M08/M09/M10`），本次标记 `Out of Scope`，未计入本服务实现率。

综合判定：`hr-business-service` 当前不满足“完全符合需求清单与实现计划”的目标，存在 P0 级阻断问题与大范围模块缺失，需要先完成 P0/P1 再推进 M05/M06 功能补齐。
