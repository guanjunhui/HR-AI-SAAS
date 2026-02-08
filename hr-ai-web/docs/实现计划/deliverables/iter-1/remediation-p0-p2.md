# 修复清单（P0/P1/P2）

## P0（已执行）
- 任职事件审批接口前后端契约不一致修复
  - 前端 `approve` 调用补齐请求体 `approved/rejectReason`
  - 页面新增驳回动作并强制输入驳回原因
  - OpenAPI 增补 `/v1/hr/employment-events/{id}/approve` 契约并声明必填字段
- 新增自动化回归测试
  - 契约测试校验 `approve` 路径与请求体约束
  - 服务层静态契约测试校验审批请求体透传

## P1（已执行）
- Core HR 接口清单补全
  - API inventory 增补任职事件 `submit/approve/cancel` 三个动作接口
- 前端路由与类型一致性修复
  - 离职风险看板主路由统一为 `/ai/insights/turnover`，并保留 `/ai/risk-turnover` 兼容跳转
  - 入职自动补全/简历解析页面数值字段切换为 `InputNumber`，避免字符串类型透传后端
  - 入职自动补全页面改为监听 `unresolvedFields` 实时渲染“仍需人工确认字段”

## P0（本轮已执行）
- Token 刷新并发队列修复
  - 刷新失败时显式释放排队请求（reject），避免页面请求悬挂
  - 增加 `__retryAuth` 标记，避免 401 场景无限刷新循环
- 服务降级策略修复
  - 仅对“服务不可用”（网络异常/5xx/408/429）启用本地兜底
  - 4xx 与业务失败码不再误降级，避免吞掉真实业务错误
- 新增契约测试
  - `request.ts` 刷新队列与重试保护
  - `fallback.ts` 降级触发条件
  - 页面数值输入契约

## P2（待后端协同）
- `onboarding/recruiting/performance/risk` 四条 AI 接口后端仍返回业务失败码，前端当前依赖本地兜底数据保障可用
- 需要在 `hr-business-service` 完成真实接口实现后，开启 CI 的真实 Pact provider 校验并补充跨模块 E2E
