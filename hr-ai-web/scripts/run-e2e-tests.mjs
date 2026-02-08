import { createRequire } from 'node:module';
import { spawnSync } from 'node:child_process';

const require = createRequire(import.meta.url);

function run(command, args) {
  const result = spawnSync(command, args, { stdio: 'inherit' });
  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

let hasPlaywright = false;
let hasCypress = false;

try {
  require.resolve('@playwright/test');
  hasPlaywright = true;
} catch {
  hasPlaywright = false;
}

try {
  require.resolve('cypress');
  hasCypress = true;
} catch {
  hasCypress = false;
}

if (hasPlaywright) {
  run('npx', ['playwright', 'test']);
} else if (hasCypress) {
  run('npx', ['cypress', 'run']);
} else {
  console.log('未安装 Playwright/Cypress，跳过 E2E。');
}
