import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const hrServiceCode = readFileSync('src/services/hr.ts', 'utf-8');
const eventPageCode = readFileSync('src/pages/hr/events/index.tsx', 'utf-8');

test('任职事件审批服务方法应透传请求体', () => {
  assert.match(
    hrServiceCode,
    /approveEmploymentEventApi\(id: number, data: EmploymentEventApproveRequest\): Promise<void>/,
  );
  assert.match(
    hrServiceCode,
    /request\.post\(`\/v1\/hr\/employment-events\/\$\{id\}\/approve`, data\)/,
  );
});

test('任职事件页面审批与驳回应携带 approved 字段', () => {
  assert.equal(eventPageCode.includes('approveEmploymentEventApi(record.id, { approved: true })'), true);
  assert.equal(
    eventPageCode.includes('approveEmploymentEventApi(record.id, { approved: false, rejectReason: rejectReason.trim() })'),
    true,
  );
});

