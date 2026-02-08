# 安全报告（iter-1）

## 已实现
- 统一 Token 注入与 401 静默刷新：`src/services/request.ts`
- 路由级权限守卫：`src/components/PermissionGuard.tsx`
- 动态菜单权限裁剪：`src/layouts/BasicLayout.tsx`
- 幂等键注入（写请求）：`X-Idempotency-Key`
- 统一错误上报与通知：`AppErrorBoundary + NotificationCenter`

## 待补齐
- OWASP Top10 全量清单自动化检查（CI）
- SCA（Snyk/Dependabot）接入
- Pact provider 校验（待 CI 依赖与凭据）
