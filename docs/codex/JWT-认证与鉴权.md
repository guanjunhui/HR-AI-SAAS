# JWT 认证与鉴权 - 实现方案

## 模块范围与页面
该模块用于“账号登录 + JWT 鉴权”，回滚单点登录方案，认证统一走网关验签；用户/角色/权限数据归属 hr-org-service。

| 需求ID | PageID | 功能 | 页面类型 | 页面/区块 | 按钮/操作 | 关键字段 | 校验/规则 | 状态/口径 | 输入/输出 | 异常索引 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| AUTH001 | PG-AUTH-01 | 登录入口 | 登录页 | 登录表单 | 登录；退出 | 租户、账号、密码/验证码 | 租户必填；验证码可选 | 登录态：未登录/已登录 | LoginRequest(写)/Token(读) | EX0001,EX0003 |
| AUTH002 | PG-AUTH-02 | 会话管理 | 运维页 | 会话列表 | 强制下线 | user_id, tenant_id, 登录时间 | 仅管理员可操作 | 会话状态：有效/失效 | Session(读) | EX0001 |

## 前端实现方案
- 登录页仅提供账号/验证码登录；不提供 SSO 跳转。
- Token 存储与刷新由统一 Auth Hook 处理；退出后清理本地会话。
- 请求统一携带 `Authorization: Bearer {token}`。

## 后端实现方案
- 认证入口由 hr-org-service 提供，网关统一验签。
- API 设计（建议）：
  - `POST /auth/login` 账号/验证码登录（hr-org-service）
  - `POST /auth/logout` 退出登录
  - `POST /auth/refresh` 刷新 Token
  - `GET /auth/keys` JWT 公钥（可选，非对称签名时）
- JWT 载荷建议包含：`tenant_id`、`user_id`、`roles`、`perms`、`token_version`。
- 网关负责验签与基础鉴权；业务服务做资源级授权。

## 时序与流程
**登录/换取 Token**：
```
用户/前端 -> hr-org-service : /auth/login
hr-org-service -> hr-org-service : 校验账号与权限
hr-org-service -> 用户/前端 : access_token + refresh_token
网关 -> 业务服务 : 透传鉴权信息
```

**刷新 Token**：
```
用户/前端 -> hr-org-service : /auth/refresh
hr-org-service -> hr-org-service : 校验 refresh_token
hr-org-service -> 用户/前端 : new access_token
```

**退出**：
```
用户/前端 -> hr-org-service : /auth/logout
hr-org-service -> hr-org-service : 失效 session/refresh_token
网关/服务侧 : 令牌过期或 token_version 不匹配拒绝
```

## 权限拉取与缓存策略
- 登录时从 hr-org-service 拉取用户/角色/权限并写入 JWT（可含 `roles`、`perms`、`org_scope`）。
- 权限变更通过“权限版本号/更新时间”触发强制刷新或重新登录。
- 网关可维护短时缓存（5~15 分钟）降低鉴权开销。

## 关键规则
- JWT 必须包含 `tenant_id`、`user_id`、`roles`；令牌过期策略统一配置。
- Token 版本号（`token_version`）用于强制失效。
- 重要操作写入审计日志。

## 异常场景
- EX0001：未认证/无权限
- EX0003：参数非法/必填缺失

## 验收与测试要点
- 未登录请求统一 401；登录后可访问授权资源。
- Token 刷新与过期策略一致；权限变更能及时生效。
