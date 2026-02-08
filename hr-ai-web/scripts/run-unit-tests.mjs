import { spawnSync } from 'node:child_process';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);
const withCoverage = process.argv.includes('--coverage');

function run(command, args) {
  const result = spawnSync(command, args, { stdio: 'inherit' });
  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

let hasJest = false;
try {
  require.resolve('jest/bin/jest.js');
  hasJest = true;
} catch {
  hasJest = false;
}

if (hasJest) {
  const args = ['jest'];
  if (withCoverage) {
    args.push('--coverage');
  }
  run('npx', args);
} else {
  const args = ['--test', 'tests/**/*.test.mjs'];
  if (withCoverage) {
    args.splice(1, 0, '--experimental-test-coverage');
  }
  run('node', args);
}
