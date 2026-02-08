import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const content = readFileSync('contracts/hr-business-service.openapi.yaml', 'utf-8');

test('OpenAPI 契约应包含关键四功能接口', () => {
  const requiredPaths = [
    '/v1/onboarding/forms/autofill',
    '/v1/recruiting/candidates/{id}/parse-resume',
    '/v1/performance/predictions',
    '/v1/ai/risk/turnover/dashboard',
  ];

  requiredPaths.forEach((path) => {
    assert.equal(content.includes(path), true, `缺少路径: ${path}`);
  });
});
