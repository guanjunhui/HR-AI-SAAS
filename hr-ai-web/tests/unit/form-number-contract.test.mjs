import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const onboardingPageCode = readFileSync('src/pages/onboarding/autofill/index.tsx', 'utf-8');
const resumePageCode = readFileSync('src/pages/recruiting/resume-parser/index.tsx', 'utf-8');

test('入职自动补全页面应监听 unresolvedFields 并使用数值输入组件', () => {
  assert.equal(onboardingPageCode.includes("Form.useWatch('unresolvedFields', draftForm)"), true);
  assert.equal(onboardingPageCode.includes('name="candidateId"'), true);
  assert.equal(onboardingPageCode.includes('name="orgUnitId"'), true);
  assert.equal(onboardingPageCode.includes('name="positionId"'), true);
  assert.equal(onboardingPageCode.includes('<InputNumber style={{ width: \'100%\' }}'), true);
});

test('简历解析页面候选人ID应使用数值输入组件', () => {
  assert.equal(resumePageCode.includes('name="candidateId"'), true);
  assert.equal(resumePageCode.includes('<InputNumber style={{ width: \'100%\' }} placeholder="候选人 ID" />'), true);
});
