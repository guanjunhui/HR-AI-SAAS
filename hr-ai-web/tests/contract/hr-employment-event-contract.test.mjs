import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const content = readFileSync('contracts/hr-business-service.openapi.yaml', 'utf-8');

test('任职事件审批接口契约应声明必填 approved 字段', () => {
  assert.equal(content.includes('/v1/hr/employment-events/{id}/approve'), true, '缺少审批路径定义');

  const approveBlockMatch = content.match(
    /\/v1\/hr\/employment-events\/\{id\}\/approve:[\s\S]*?(?=\n  \/v1\/|\ncomponents:)/,
  );

  assert.notEqual(approveBlockMatch, null, '未找到审批路径块');
  const approveBlock = approveBlockMatch?.[0] || '';
  assert.equal(approveBlock.includes('requestBody:'), true, '审批接口缺少 requestBody');
  assert.equal(approveBlock.includes('- approved'), true, '审批接口未声明 approved 必填');
  assert.equal(approveBlock.includes('type: boolean'), true, 'approved 字段类型应为 boolean');
});

