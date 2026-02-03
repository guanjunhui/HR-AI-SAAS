# HR AI SaaS 方案文档（Codex 输出）

本目录基于 `docs/HR_AI_SaaS需求清单.xlsx` 输出前后端实现方案，按模块拆分，覆盖页面、字段、状态机、异常场景、模块联动与 AI 场景。技术选型已按你的回复固定：前端 React 18 + Vite + TypeScript + Ant Design；后端 Spring Boot + MyBatis Plus；认证 JWT；多租户多库。

## 全局约束与共性设计

### 前端技术与工程约定
- 技术栈：React 18 + Vite + TypeScript + Ant Design + React Router + React Query + Axios + Zustand（或等价轻量状态库）。
- 表单/列表字段：优先由“页面字段表”驱动（控件类型/数据类型/必填/可编辑/只读条件）。
- 权限：菜单级 + 按钮级 + 字段级；前端根据后端下发权限点和字段权限配置动态渲染。
- 导入/导出：统一上传组件与导出任务提示；大结果集走异步导出。
- 审计/埋点：按钮操作触发埋点（见按钮字典中的事件命名），与后端审计日志对齐。

### 后端技术与架构约定
- Spring Boot 3.x + MyBatis Plus；RESTful API；统一分页/错误码。
- Spring AI Alibaba 1.1.0.0-RC2：基础依赖使用 `spring-ai-alibaba-agent-framework`（替换 `starter` + `starter-agent`）。
- 认证鉴权：JWT 签发由 hr-org-service 提供，网关统一验签，服务内做细粒度授权。
- 多租户多库：JWT 中携带 `tenant_id`；数据源路由层按租户切库；租户注册表维护连接信息；关键操作写审计日志。
- 权限控制：RBAC + 组织范围（行级）+ 字段级权限（脱敏/隐藏）。
- 并发控制：关键对象支持版本号或更新时间校验；冲突按 EX0002 提示。
- 审计留痕：关键写操作写入 `AuditLog`；AI 输出写入 `AIAudit`。

### 通用接口约定（建议）
- 列表：`GET /api/{resource}`（分页、筛选、排序）
- 详情：`GET /api/{resource}/{id}`
- 创建：`POST /api/{resource}`
- 更新：`PUT /api/{resource}/{id}`
- 状态变更：`PATCH /api/{resource}/{id}/status`
- 导入：`POST /api/{resource}/import`
- 导出：`GET /api/{resource}/export`

> 以上为方案建议，具体以模块页面“按钮/操作”与权限点映射为准。

## 模块方案索引
- `M01-组织与权限.md`
- `M02-CoreHR.md`
- `M03-招聘管理.md`
- `M04-入职与Onboarding.md`
- `M05-考勤与假勤.md`
- `M06-薪酬与薪资.md`
- `M07-绩效管理.md`
- `M08-工单与知识库.md`
- `M09-报表与分析.md`
- `M10-AI能力中心.md`
- `RAG-检索服务.md`
- `JWT-认证与鉴权.md`
