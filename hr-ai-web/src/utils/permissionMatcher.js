function normalizePermission(input) {
  return typeof input === 'string' ? input.trim() : '';
}

const ORG_USER_PREFIX = 'org:user:';
const USER_PREFIX = 'user:';
const ORG_ROLE_PREFIX = 'org:role:';
const ROLE_PREFIX = 'role:';

function addAliasCandidate(candidates, permission, sourcePrefix, targetPrefix) {
  if (permission.startsWith(sourcePrefix)) {
    candidates.add(`${targetPrefix}${permission.slice(sourcePrefix.length)}`);
  }
}

function expandAliasCandidates(permission) {
  const candidates = new Set([permission]);
  addAliasCandidate(candidates, permission, ORG_USER_PREFIX, USER_PREFIX);
  addAliasCandidate(candidates, permission, USER_PREFIX, ORG_USER_PREFIX);
  addAliasCandidate(candidates, permission, ORG_ROLE_PREFIX, ROLE_PREFIX);
  addAliasCandidate(candidates, permission, ROLE_PREFIX, ORG_ROLE_PREFIX);
  return [...candidates];
}

function matchSingleRule(granted, required) {
  if (granted === '*' || granted === required) {
    return true;
  }

  if (granted.endsWith('*')) {
    const prefix = granted.slice(0, -1);
    return required.startsWith(prefix);
  }

  return false;
}

export function matchesPermissionRule(grantedPermission, requiredPermission) {
  const granted = normalizePermission(grantedPermission);
  const required = normalizePermission(requiredPermission);

  if (!required) {
    return true;
  }

  if (!granted) {
    return false;
  }

  const grantedCandidates = expandAliasCandidates(granted);
  const requiredCandidates = expandAliasCandidates(required);
  for (const grantedCandidate of grantedCandidates) {
    for (const requiredCandidate of requiredCandidates) {
      if (matchSingleRule(grantedCandidate, requiredCandidate)) {
        return true;
      }
    }
  }

  return false;
}

export function hasPermissionByList(permissions, requiredPermission) {
  const required = normalizePermission(requiredPermission);
  if (!required) {
    return true;
  }

  if (!Array.isArray(permissions) || permissions.length === 0) {
    return false;
  }

  return permissions.some((permission) => matchesPermissionRule(permission, required));
}
