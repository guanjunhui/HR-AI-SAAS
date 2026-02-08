import test from 'node:test';
import assert from 'node:assert/strict';
import { hasPermissionByList, matchesPermissionRule } from '../../src/utils/permissionMatcher.js';

test('通配权限应覆盖具体权限点', () => {
  assert.equal(matchesPermissionRule('*', 'org:user:read'), true);
  assert.equal(matchesPermissionRule('org:*', 'org:user:read'), true);
  assert.equal(matchesPermissionRule('org:*', 'hr:employee:read'), false);
});

test('权限列表判断应支持通配与精确匹配', () => {
  const permissions = ['org:*', 'hr:employee:read'];
  assert.equal(hasPermissionByList(permissions, 'org:role:read'), true);
  assert.equal(hasPermissionByList(permissions, 'hr:employee:read'), true);
  assert.equal(hasPermissionByList(permissions, 'performance:review:read'), false);
});

test('权限匹配应兼容 org/user 与 org/role 命名别名', () => {
  assert.equal(matchesPermissionRule('user:*', 'org:user:read'), true);
  assert.equal(matchesPermissionRule('org:user:*', 'user:write'), true);
  assert.equal(matchesPermissionRule('role:read', 'org:role:read'), true);
  assert.equal(matchesPermissionRule('org:role:*', 'role:write'), true);
});

test('空权限点应默认放行，空权限列表应拒绝受限资源', () => {
  assert.equal(hasPermissionByList([], undefined), true);
  assert.equal(hasPermissionByList([], ''), true);
  assert.equal(hasPermissionByList([], 'org:user:read'), false);
});
