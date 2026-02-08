# 测试报告（iter-1）

## 执行时间
- 2026-02-06

## 命令
- `npm run lint`
- `npm run typecheck`
- `npm run test:coverage`
- `npm run build`
- `npm run contract:test`

## 结果摘要
- lint: 通过
- typecheck: 通过
- unit test: 通过（3/3）
- coverage: line 100%、branch 85.71%、func 100%
- contract smoke test: 通过（1/1）
- build: 通过
- pact verify: 未启用真实校验（未配置 Broker 环境变量）

## 备注
- 当前测试框架采用 Node 原生 `node:test`，并通过 `scripts/run-unit-tests.mjs` 支持自动切换到 Jest（若已安装）。
- 依赖安装探测证据：`npm ls jest @testing-library/react msw @pact-foundation/pact --depth=0` 输出 `└── (empty)`。
