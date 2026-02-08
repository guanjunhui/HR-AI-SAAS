const brokerUrl = process.env.PACT_BROKER_URL;
const providerBaseUrl = process.env.PACT_PROVIDER_BASE_URL;

if (!brokerUrl || !providerBaseUrl) {
  console.log('PACT 校验占位: 缺少 PACT_BROKER_URL 或 PACT_PROVIDER_BASE_URL，跳过。');
  process.exit(0);
}

console.log('PACT 校验占位: 当前仓库未安装 pact 依赖，后续在 CI 镜像补齐后启用真实校验。');
process.exit(0);
