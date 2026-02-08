# CI 配置说明（iter-1）

- 质量门禁模板文件：`ci/hr-ai-web-ci.yml`
- 使用方式：
  1. 将该文件复制到仓库根目录 `.github/workflows/hr-ai-web-ci.yml`
  2. 在 CI 环境中设置 `PACT_BROKER_URL`、`PACT_PROVIDER_BASE_URL`（如需真实 Pact）
  3. 执行 `npm run quality:gate`

## 当前默认行为
- 若未安装 Jest/Pact/Playwright 相关依赖：
  - `npm test` 自动回退到 Node 原生测试。
  - `npm run contract:test` 执行 OpenAPI 契约基础校验。
  - `npm run test:e2e` 自动跳过并输出提示。
