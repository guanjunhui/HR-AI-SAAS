import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const fallbackCode = readFileSync('src/services/resilience/fallback.ts', 'utf-8');

test('降级策略应仅在服务不可用时生效', () => {
  assert.equal(fallbackCode.includes('function isServiceUnavailableError(error: unknown): boolean'), true);
  assert.equal(fallbackCode.includes('status >= 500 || status === 429 || status === 408'), true);
  assert.equal(fallbackCode.includes('return false;'), true);
});

test('业务错误不应触发本地兜底', () => {
  assert.equal(fallbackCode.includes('if (!isServiceUnavailableError(error))'), true);
  assert.equal(fallbackCode.includes('throw error;'), true);
});
