# API 接口清单

> RESTful API 接口定义

## 通用规范

### 请求头

| Header | 说明 | 必须 |
|--------|------|------|
| Authorization | Bearer {token} | 是（除登录接口外） |
| X-Tenant-Id | 租户ID | 是 |
| X-User-Id | 用户ID | 否（系统自动获取） |
| X-Session-Id | 会话ID | 否（AI对话时使用） |
| Content-Type | application/json | 是 |

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "requestId": "uuid",
  "timestamp": 1706600000000
}
```

### 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |
| 10001 | 业务错误（具体见message） |

### 分页参数

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "sortField": "created_at",
  "sortOrder": "desc"
}
```

### 分页响应

```json
{
  "list": [],
  "total": 100,
  "pageNum": 1,
  "pageSize": 20,
  "pages": 5
}
```

---

## M01 组织与权限

### 组织管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/org/units/tree | 获取组织树 | org:unit:read |
| GET | /api/org/units/{id} | 获取组织详情 | org:unit:read |
| POST | /api/org/units | 创建组织 | org:unit:write |
| PUT | /api/org/units/{id} | 更新组织 | org:unit:write |
| DELETE | /api/org/units/{id} | 删除组织 | org:unit:write |

### 用户管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/org/users | 用户列表 | org:user:read |
| GET | /api/org/users/{id} | 用户详情 | org:user:read |
| POST | /api/org/users | 创建用户 | org:user:write |
| PUT | /api/org/users/{id} | 更新用户 | org:user:write |
| DELETE | /api/org/users/{id} | 删除用户 | org:user:write |
| PUT | /api/org/users/{id}/roles | 分配角色 | org:user:write |
| PUT | /api/org/users/{id}/password | 修改密码 | org:user:write |
| PUT | /api/org/users/{id}/status | 启用/禁用 | org:user:write |
| GET | /api/org/users/current | 获取当前用户 | - |

### 角色管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/org/roles | 角色列表 | org:role:read |
| GET | /api/org/roles/{id} | 角色详情 | org:role:read |
| POST | /api/org/roles | 创建角色 | org:role:write |
| PUT | /api/org/roles/{id} | 更新角色 | org:role:write |
| DELETE | /api/org/roles/{id} | 删除角色 | org:role:write |
| PUT | /api/org/roles/{id}/permissions | 分配权限 | org:role:write |
| GET | /api/org/permissions | 权限列表 | org:role:read |

### 审计日志

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/org/audit-logs | 审计日志列表 | org:audit:read |
| GET | /api/org/audit-logs/{id} | 审计日志详情 | org:audit:read |

---

## M02 Core HR

### 岗位管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/hr/positions | 岗位列表 | hr:position:read |
| GET | /api/hr/positions/{id} | 岗位详情 | hr:position:read |
| POST | /api/hr/positions | 创建岗位 | hr:position:write |
| PUT | /api/hr/positions/{id} | 更新岗位 | hr:position:write |
| DELETE | /api/hr/positions/{id} | 删除岗位 | hr:position:write |

### 编制管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/hr/headcounts | 编制列表 | hr:headcount:read |
| GET | /api/hr/headcounts/summary | 编制汇总 | hr:headcount:read |
| POST | /api/hr/headcounts | 创建编制 | hr:headcount:write |
| PUT | /api/hr/headcounts/{id} | 更新编制 | hr:headcount:write |
| DELETE | /api/hr/headcounts/{id} | 删除编制 | hr:headcount:write |

### 员工管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/hr/employees | 员工列表（花名册） | hr:employee:read |
| GET | /api/hr/employees/{id} | 员工详情 | hr:employee:read |
| GET | /api/hr/employees/{id}/detail | 员工完整档案 | hr:employee:read |
| POST | /api/hr/employees | 创建员工 | hr:employee:write |
| PUT | /api/hr/employees/{id} | 更新员工 | hr:employee:write |
| PUT | /api/hr/employees/{id}/detail | 更新员工详情 | hr:employee:write |
| DELETE | /api/hr/employees/{id} | 删除员工 | hr:employee:write |
| POST | /api/hr/employees/import | 批量导入 | hr:employee:write |
| GET | /api/hr/employees/export | 导出Excel | hr:employee:read |

### 人事事件

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/hr/events | 事件列表 | hr:event:read |
| GET | /api/hr/events/{id} | 事件详情 | hr:event:read |
| POST | /api/hr/events | 创建事件 | hr:event:write |
| PUT | /api/hr/events/{id} | 更新事件 | hr:event:write |
| POST | /api/hr/events/{id}/submit | 提交审批 | hr:event:write |
| POST | /api/hr/events/{id}/approve | 审批通过 | hr:event:approve |
| POST | /api/hr/events/{id}/reject | 审批驳回 | hr:event:approve |
| POST | /api/hr/events/{id}/withdraw | 撤回申请 | hr:event:write |

---

## M03 招聘管理

### 招聘需求

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/recruiting/requisitions | 需求列表 | recruiting:req:read |
| GET | /api/recruiting/requisitions/{id} | 需求详情 | recruiting:req:read |
| POST | /api/recruiting/requisitions | 创建需求 | recruiting:req:write |
| PUT | /api/recruiting/requisitions/{id} | 更新需求 | recruiting:req:write |
| POST | /api/recruiting/requisitions/{id}/submit | 提交审批 | recruiting:req:write |
| POST | /api/recruiting/requisitions/{id}/approve | 审批通过 | recruiting:req:approve |
| POST | /api/recruiting/requisitions/{id}/reject | 审批驳回 | recruiting:req:approve |
| POST | /api/recruiting/requisitions/{id}/pause | 暂停招聘 | recruiting:req:write |
| POST | /api/recruiting/requisitions/{id}/resume | 恢复招聘 | recruiting:req:write |
| POST | /api/recruiting/requisitions/{id}/close | 关闭招聘 | recruiting:req:write |

### 候选人

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/recruiting/candidates | 候选人列表 | recruiting:candidate:read |
| GET | /api/recruiting/candidates/{id} | 候选人详情 | recruiting:candidate:read |
| POST | /api/recruiting/candidates | 创建候选人 | recruiting:candidate:write |
| PUT | /api/recruiting/candidates/{id} | 更新候选人 | recruiting:candidate:write |
| POST | /api/recruiting/candidates/{id}/resume | 上传简历 | recruiting:candidate:write |
| POST | /api/recruiting/candidates/{id}/parse-resume | AI解析简历 | recruiting:candidate:write |
| POST | /api/recruiting/candidates/{id}/apply | 投递岗位 | recruiting:candidate:write |
| POST | /api/recruiting/candidates/{id}/reject | 淘汰候选人 | recruiting:candidate:write |
| GET | /api/recruiting/candidates/{id}/applications | 投递记录 | recruiting:candidate:read |
| GET | /api/recruiting/candidates/{id}/interviews | 面试记录 | recruiting:candidate:read |

### 面试

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/recruiting/interviews | 面试列表 | recruiting:interview:read |
| GET | /api/recruiting/interviews/{id} | 面试详情 | recruiting:interview:read |
| POST | /api/recruiting/interviews | 安排面试 | recruiting:interview:write |
| PUT | /api/recruiting/interviews/{id} | 更新面试 | recruiting:interview:write |
| POST | /api/recruiting/interviews/{id}/cancel | 取消面试 | recruiting:interview:write |
| POST | /api/recruiting/interviews/{id}/feedback | 提交反馈 | recruiting:interview:write |
| GET | /api/recruiting/interviews/{id}/feedbacks | 获取所有反馈 | recruiting:interview:read |
| GET | /api/recruiting/interviews/calendar | 面试日历 | recruiting:interview:read |

### Offer

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/recruiting/offers | Offer列表 | recruiting:offer:read |
| GET | /api/recruiting/offers/{id} | Offer详情 | recruiting:offer:read |
| POST | /api/recruiting/offers | 创建Offer | recruiting:offer:write |
| PUT | /api/recruiting/offers/{id} | 更新Offer | recruiting:offer:write |
| POST | /api/recruiting/offers/{id}/submit | 提交审批 | recruiting:offer:write |
| POST | /api/recruiting/offers/{id}/approve | 审批通过 | recruiting:offer:approve |
| POST | /api/recruiting/offers/{id}/send | 发送Offer | recruiting:offer:write |
| POST | /api/recruiting/offers/{id}/accept | 候选人接受 | recruiting:offer:write |
| POST | /api/recruiting/offers/{id}/reject | 候选人拒绝 | recruiting:offer:write |

---

## M04 入职Onboarding

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/onboarding/list | 入职记录列表 | onboarding:read |
| GET | /api/onboarding/{id} | 入职详情 | onboarding:read |
| POST | /api/onboarding | 创建入职记录 | onboarding:write |
| PUT | /api/onboarding/{id} | 更新入职信息 | onboarding:write |
| POST | /api/onboarding/{id}/complete | 完成入职 | onboarding:write |
| POST | /api/onboarding/{id}/cancel | 取消入职 | onboarding:write |
| GET | /api/onboarding/{id}/materials | 获取资料清单 | onboarding:read |
| POST | /api/onboarding/{id}/materials | 上传资料 | onboarding:write |
| PUT | /api/onboarding/materials/{materialId} | 更新资料 | onboarding:write |
| POST | /api/onboarding/materials/{materialId}/verify | 审核资料 | onboarding:verify |
| GET | /api/onboarding/{id}/tasks | 获取任务清单 | onboarding:read |
| PUT | /api/onboarding/tasks/{taskId} | 更新任务 | onboarding:write |

---

## M05 考勤假勤

### 班次管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/attendance/shifts | 班次列表 | attendance:shift:read |
| GET | /api/attendance/shifts/{id} | 班次详情 | attendance:shift:read |
| POST | /api/attendance/shifts | 创建班次 | attendance:shift:write |
| PUT | /api/attendance/shifts/{id} | 更新班次 | attendance:shift:write |
| DELETE | /api/attendance/shifts/{id} | 删除班次 | attendance:shift:write |

### 考勤规则

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/attendance/rules | 规则列表 | attendance:rule:read |
| GET | /api/attendance/rules/{id} | 规则详情 | attendance:rule:read |
| POST | /api/attendance/rules | 创建规则 | attendance:rule:write |
| PUT | /api/attendance/rules/{id} | 更新规则 | attendance:rule:write |
| DELETE | /api/attendance/rules/{id} | 删除规则 | attendance:rule:write |

### 打卡记录

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/attendance/records | 打卡记录列表 | attendance:record:read |
| GET | /api/attendance/records/my | 我的打卡记录 | - |
| POST | /api/attendance/clock-in | 上班打卡 | - |
| POST | /api/attendance/clock-out | 下班打卡 | - |
| POST | /api/attendance/supplement | 补卡申请 | attendance:record:write |
| GET | /api/attendance/summary | 考勤汇总 | attendance:record:read |
| GET | /api/attendance/summary/monthly | 月度考勤汇总 | attendance:record:read |

### 请假管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/attendance/leave/types | 假期类型列表 | attendance:leave:read |
| GET | /api/attendance/leave/balance | 我的假期余额 | - |
| GET | /api/attendance/leave/requests | 请假申请列表 | attendance:leave:read |
| GET | /api/attendance/leave/requests/{id} | 请假详情 | attendance:leave:read |
| POST | /api/attendance/leave/requests | 创建请假申请 | attendance:leave:write |
| PUT | /api/attendance/leave/requests/{id} | 更新请假申请 | attendance:leave:write |
| POST | /api/attendance/leave/requests/{id}/submit | 提交审批 | attendance:leave:write |
| POST | /api/attendance/leave/requests/{id}/approve | 审批通过 | attendance:leave:approve |
| POST | /api/attendance/leave/requests/{id}/reject | 审批驳回 | attendance:leave:approve |
| POST | /api/attendance/leave/requests/{id}/cancel | 取消申请 | attendance:leave:write |

### 加班管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/attendance/overtime/requests | 加班申请列表 | attendance:overtime:read |
| GET | /api/attendance/overtime/requests/{id} | 加班详情 | attendance:overtime:read |
| POST | /api/attendance/overtime/requests | 创建加班申请 | attendance:overtime:write |
| PUT | /api/attendance/overtime/requests/{id} | 更新加班申请 | attendance:overtime:write |
| POST | /api/attendance/overtime/requests/{id}/submit | 提交审批 | attendance:overtime:write |
| POST | /api/attendance/overtime/requests/{id}/approve | 审批通过 | attendance:overtime:approve |
| POST | /api/attendance/overtime/requests/{id}/reject | 审批驳回 | attendance:overtime:approve |

---

## M06 薪酬薪资

### 薪酬项目

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/payroll/items | 薪酬项目列表 | payroll:item:read |
| GET | /api/payroll/items/{id} | 薪酬项目详情 | payroll:item:read |
| POST | /api/payroll/items | 创建薪酬项目 | payroll:item:write |
| PUT | /api/payroll/items/{id} | 更新薪酬项目 | payroll:item:write |
| DELETE | /api/payroll/items/{id} | 删除薪酬项目 | payroll:item:write |

### 薪资档案

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/payroll/archives | 薪资档案列表 | payroll:archive:read |
| GET | /api/payroll/archives/{employeeId} | 员工薪资档案 | payroll:archive:read |
| POST | /api/payroll/archives | 创建薪资档案 | payroll:archive:write |
| PUT | /api/payroll/archives/{id} | 更新薪资档案 | payroll:archive:write |
| GET | /api/payroll/archives/{employeeId}/history | 薪资变更历史 | payroll:archive:read |

### 算薪批次

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/payroll/runs | 算薪批次列表 | payroll:run:read |
| GET | /api/payroll/runs/{id} | 批次详情 | payroll:run:read |
| POST | /api/payroll/runs | 创建批次 | payroll:run:write |
| PUT | /api/payroll/runs/{id} | 更新批次 | payroll:run:write |
| POST | /api/payroll/runs/{id}/calculate | 执行算薪 | payroll:run:write |
| POST | /api/payroll/runs/{id}/recalculate | 重新算薪 | payroll:run:write |
| POST | /api/payroll/runs/{id}/submit-review | 提交复核 | payroll:run:write |
| POST | /api/payroll/runs/{id}/approve | 审批通过 | payroll:run:approve |
| POST | /api/payroll/runs/{id}/reject | 审批驳回 | payroll:run:approve |
| POST | /api/payroll/runs/{id}/pay | 标记已发放 | payroll:run:write |
| GET | /api/payroll/runs/{id}/details | 薪资明细列表 | payroll:run:read |
| GET | /api/payroll/runs/{id}/export | 导出薪资表 | payroll:run:read |

### 工资条

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/payroll/payslips | 工资条列表 | payroll:payslip:read |
| GET | /api/payroll/payslips/my | 我的工资条 | - |
| GET | /api/payroll/payslips/{id} | 工资条详情 | payroll:payslip:read |
| POST | /api/payroll/payslips/send | 批量发送工资条 | payroll:payslip:write |
| POST | /api/payroll/payslips/{id}/confirm | 员工确认 | - |

---

## M07 绩效管理

### 绩效周期

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/performance/cycles | 周期列表 | performance:cycle:read |
| GET | /api/performance/cycles/{id} | 周期详情 | performance:cycle:read |
| POST | /api/performance/cycles | 创建周期 | performance:cycle:write |
| PUT | /api/performance/cycles/{id} | 更新周期 | performance:cycle:write |
| POST | /api/performance/cycles/{id}/start-goal-setting | 开始目标设定 | performance:cycle:write |
| POST | /api/performance/cycles/{id}/start-execution | 开始执行 | performance:cycle:write |
| POST | /api/performance/cycles/{id}/start-self-review | 开始自评 | performance:cycle:write |
| POST | /api/performance/cycles/{id}/start-manager-review | 开始上级评估 | performance:cycle:write |
| POST | /api/performance/cycles/{id}/start-calibration | 开始校准 | performance:cycle:write |
| POST | /api/performance/cycles/{id}/complete | 完成周期 | performance:cycle:write |
| GET | /api/performance/cycles/{id}/statistics | 周期统计 | performance:cycle:read |

### 绩效目标

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/performance/goals | 目标列表 | performance:goal:read |
| GET | /api/performance/goals/my | 我的目标 | - |
| GET | /api/performance/goals/{id} | 目标详情 | performance:goal:read |
| POST | /api/performance/goals | 创建目标 | performance:goal:write |
| PUT | /api/performance/goals/{id} | 更新目标 | performance:goal:write |
| POST | /api/performance/goals/{id}/submit | 提交审批 | performance:goal:write |
| POST | /api/performance/goals/{id}/approve | 审批通过 | performance:goal:approve |
| POST | /api/performance/goals/{id}/reject | 审批驳回 | performance:goal:approve |
| PUT | /api/performance/goals/{id}/progress | 更新进度 | performance:goal:write |
| GET | /api/performance/goals/team | 团队目标 | performance:goal:read |

### 绩效评估

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/performance/reviews | 评估列表 | performance:review:read |
| GET | /api/performance/reviews/pending | 待评估列表 | - |
| GET | /api/performance/reviews/{id} | 评估详情 | performance:review:read |
| POST | /api/performance/reviews | 创建评估 | performance:review:write |
| PUT | /api/performance/reviews/{id} | 更新评估 | performance:review:write |
| POST | /api/performance/reviews/{id}/submit | 提交评估 | performance:review:write |
| POST | /api/performance/reviews/{id}/ai-generate | AI生成评语 | performance:review:write |
| GET | /api/performance/reviews/employee/{employeeId} | 员工所有评估 | performance:review:read |

### 绩效结果

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/performance/results | 结果列表 | performance:result:read |
| GET | /api/performance/results/my | 我的绩效结果 | - |
| GET | /api/performance/results/{id} | 结果详情 | performance:result:read |
| POST | /api/performance/results/calibrate | 批量校准 | performance:result:calibrate |
| POST | /api/performance/results/{id}/confirm | 员工确认 | - |
| GET | /api/performance/results/distribution | 分布统计 | performance:result:read |
| GET | /api/performance/results/export | 导出结果 | performance:result:read |

---

## M08 工单系统

### 工单类型

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/tickets/types | 工单类型列表 | ticket:type:read |
| GET | /api/tickets/types/tree | 工单类型树 | ticket:type:read |
| GET | /api/tickets/types/{id} | 类型详情 | ticket:type:read |
| POST | /api/tickets/types | 创建类型 | ticket:type:write |
| PUT | /api/tickets/types/{id} | 更新类型 | ticket:type:write |
| DELETE | /api/tickets/types/{id} | 删除类型 | ticket:type:write |

### 工单管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/tickets | 工单列表 | ticket:read |
| GET | /api/tickets/my | 我的工单 | - |
| GET | /api/tickets/assigned | 分配给我的工单 | - |
| GET | /api/tickets/{id} | 工单详情 | ticket:read |
| POST | /api/tickets | 创建工单 | - |
| PUT | /api/tickets/{id} | 更新工单 | ticket:write |
| POST | /api/tickets/{id}/assign | 分配工单 | ticket:assign |
| POST | /api/tickets/{id}/start | 开始处理 | ticket:process |
| POST | /api/tickets/{id}/reply | 回复工单 | ticket:process |
| POST | /api/tickets/{id}/resolve | 解决工单 | ticket:process |
| POST | /api/tickets/{id}/close | 关闭工单 | ticket:process |
| POST | /api/tickets/{id}/reopen | 重新打开 | ticket:process |
| POST | /api/tickets/{id}/comment | 添加评论 | ticket:process |
| GET | /api/tickets/{id}/comments | 获取评论 | ticket:read |
| POST | /api/tickets/{id}/satisfaction | 满意度评价 | - |
| GET | /api/tickets/statistics | 工单统计 | ticket:read |

### 知识库

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/knowledge/articles | 文章列表 | knowledge:read |
| GET | /api/knowledge/articles/{id} | 文章详情 | knowledge:read |
| POST | /api/knowledge/articles | 创建文章 | knowledge:write |
| PUT | /api/knowledge/articles/{id} | 更新文章 | knowledge:write |
| POST | /api/knowledge/articles/{id}/publish | 发布文章 | knowledge:write |
| POST | /api/knowledge/articles/{id}/archive | 归档文章 | knowledge:write |
| POST | /api/knowledge/articles/{id}/helpful | 标记有帮助 | - |
| POST | /api/knowledge/articles/{id}/not-helpful | 标记无帮助 | - |
| GET | /api/knowledge/articles/search | 搜索文章 | knowledge:read |
| GET | /api/knowledge/categories | 分类列表 | knowledge:read |

---

## M09 报表与分析

### 报表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/reports | 报表列表 | report:read |
| GET | /api/reports/{code} | 报表详情 | report:read |
| GET | /api/reports/{code}/data | 获取报表数据 | report:read |
| GET | /api/reports/{code}/export | 导出报表 | report:read |
| POST | /api/reports | 创建报表配置 | report:write |
| PUT | /api/reports/{code} | 更新报表配置 | report:write |

### 看板

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/dashboards | 看板列表 | dashboard:read |
| GET | /api/dashboards/{id} | 看板详情 | dashboard:read |
| GET | /api/dashboards/{id}/data | 看板数据 | dashboard:read |
| POST | /api/dashboards | 创建看板 | dashboard:write |
| PUT | /api/dashboards/{id} | 更新看板 | dashboard:write |
| DELETE | /api/dashboards/{id} | 删除看板 | dashboard:write |
| PUT | /api/dashboards/{id}/default | 设为默认 | dashboard:write |

---

## M10 AI能力中心

### AI对话

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/ai/chat | 发送消息（同步） | - |
| GET | /api/ai/chat/stream | SSE流式对话 | - |
| GET | /api/ai/chat/history | 获取对话历史 | - |
| DELETE | /api/ai/chat/history | 清除对话历史 | - |
| POST | /api/ai/chat/feedback | 提交反馈 | - |

### AI洞察

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/ai/insights/team-overview | 团队概览 | ai:insight:read |
| GET | /api/ai/insights/stability | 稳定性分析 | ai:insight:read |
| GET | /api/ai/insights/org-health | 组织健康度 | ai:insight:read |
| GET | /api/ai/insights/headcount | 编制分析 | ai:insight:read |
| GET | /api/ai/insights/turnover-risk | 离职风险 | ai:insight:read |

### AI建议

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/ai/suggestions | 建议列表 | ai:suggestion:read |
| GET | /api/ai/suggestions/{id} | 建议详情 | ai:suggestion:read |
| POST | /api/ai/suggestions/{id}/review | 审核建议 | ai:suggestion:review |
| POST | /api/ai/suggestions/{id}/adopt | 采纳建议 | ai:suggestion:adopt |
| POST | /api/ai/suggestions/{id}/dismiss | 忽略建议 | ai:suggestion:review |

### AI行动

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/ai/actions | 行动列表 | ai:action:read |
| GET | /api/ai/actions/{id} | 行动详情 | ai:action:read |
| POST | /api/ai/actions/{id}/execute | 执行行动 | ai:action:execute |
| POST | /api/ai/actions/{id}/cancel | 取消行动 | ai:action:execute |

### What-if模拟

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/ai/whatif/salary | 薪资调整模拟 | ai:whatif:read |
| POST | /api/ai/whatif/headcount | 编制变更模拟 | ai:whatif:read |
| POST | /api/ai/whatif/resign | 批量离职模拟 | ai:whatif:read |
| GET | /api/ai/whatif/history | 模拟历史 | ai:whatif:read |
| GET | /api/ai/whatif/{id} | 模拟详情 | ai:whatif:read |

### AI审计

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/ai/audit/logs | 审计日志列表 | ai:audit:read |
| GET | /api/ai/audit/logs/{id} | 日志详情 | ai:audit:read |
| POST | /api/ai/audit/logs/{id}/mark-review | 标记需审核 | ai:audit:write |
| GET | /api/ai/audit/statistics | 统计数据 | ai:audit:read |

---

## 接口统计

| 模块 | 接口数 |
|------|--------|
| M01 组织与权限 | 20 |
| M02 Core HR | 25 |
| M03 招聘管理 | 35 |
| M04 入职Onboarding | 12 |
| M05 考勤假勤 | 28 |
| M06 薪酬薪资 | 22 |
| M07 绩效管理 | 25 |
| M08 工单系统 | 25 |
| M09 报表与分析 | 13 |
| M10 AI能力中心 | 25 |
| **总计** | **~230** |

---

*本文档包含系统所有 API 接口定义*
