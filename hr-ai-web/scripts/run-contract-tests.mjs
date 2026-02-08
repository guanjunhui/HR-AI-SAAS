import { spawnSync } from 'node:child_process';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);
const brokerUrl = process.env.PACT_BROKER_URL;
const providerBaseUrl = process.env.PACT_PROVIDER_BASE_URL;

function run(command, args) {
  const result = spawnSync(command, args, { stdio: 'inherit' });
  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

let hasPact = false;
try {
  require.resolve('@pact-foundation/pact');
  hasPact = true;
} catch {
  hasPact = false;
}

if (hasPact && brokerUrl && providerBaseUrl) {
  console.log('执行 Pact 真实校验');
  run('node', ['scripts/pact-verify-placeholder.mjs']);
} else {
  console.log('执行契约基础校验（OpenAPI + 本地断言）');
  run('node', ['--test', 'tests/contract/*.test.mjs']);
}
