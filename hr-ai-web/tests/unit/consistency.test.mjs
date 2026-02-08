import test from 'node:test';
import assert from 'node:assert/strict';
import { compareMetrics, computeDiffRate } from '../../scripts/lib/consistency.mjs';

test('computeDiffRate 应正确计算差异率', () => {
  assert.equal(computeDiffRate(101, 100), 0.01);
  assert.equal(computeDiffRate(0, 0), 0);
  assert.equal(computeDiffRate(1, 0), 1);
  assert.equal(computeDiffRate('x', 0), 1);
});

test('compareMetrics 仅返回超阈值项', () => {
  const diffs = compareMetrics(
    { totalEmployees: 100, activeEmployees: 90 },
    { totalEmployees: 100, activeEmployees: 100 },
    0.05,
  );

  assert.equal(diffs.length, 1);
  assert.equal(diffs[0].key, 'activeEmployees');
});
