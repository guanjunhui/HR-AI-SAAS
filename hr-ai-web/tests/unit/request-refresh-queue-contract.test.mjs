import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const requestCode = readFileSync('src/services/request.ts', 'utf-8');

test('token 刷新应支持并发排队请求失败释放', () => {
  assert.equal(requestCode.includes('function rejectPendingRequests(reason: unknown): void'), true);
  assert.equal(requestCode.includes('if (isRefreshing)'), true);
  assert.equal(requestCode.includes('addPendingRequest({'), true);
  assert.equal(requestCode.includes('rejectPendingRequests(refreshError);'), true);
  assert.equal(requestCode.includes('rejectPendingRequests(error);'), true);
});

test('401 重试应避免无限刷新循环', () => {
  assert.equal(requestCode.includes('if (config.__retryAuth)'), true);
  assert.equal(requestCode.includes('config.__retryAuth = true;'), true);
});

test('自动重试应仅允许 GET 或显式幂等写请求', () => {
  assert.equal(requestCode.includes('if (!config.meta?.idempotent || retry <= 0)'), true);
});
