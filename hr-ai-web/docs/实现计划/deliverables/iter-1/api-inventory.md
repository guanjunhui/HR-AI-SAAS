# API 清单（iter-1）

## REST 接口

### Core HR（已对接）
- `GET /api/v1/hr/positions`
- `POST /api/v1/hr/positions`
- `GET /api/v1/hr/headcounts`
- `POST /api/v1/hr/headcounts`
- `GET /api/v1/hr/employees`
- `POST /api/v1/hr/employees`
- `GET /api/v1/hr/employment-events`
- `POST /api/v1/hr/employment-events`
- `POST /api/v1/hr/employment-events/{id}/submit`
- `POST /api/v1/hr/employment-events/{id}/approve`（body: `approved`, `rejectReason`）
- `POST /api/v1/hr/employment-events/{id}/cancel`

### 四功能闭环（新增契约）
- `POST /api/v1/onboarding/forms/autofill`
- `POST /api/v1/recruiting/candidates/{id}/parse-resume`
- `GET /api/v1/performance/predictions`
- `POST /api/v1/performance/predictions/{id}/calibrate`
- `GET /api/v1/ai/risk/turnover/dashboard`
- `POST /api/v1/ai/risk/turnover/{id}/feedback`

## GraphQL 接口
- 当前仓库未发现 GraphQL endpoint（基于代码检索）。
