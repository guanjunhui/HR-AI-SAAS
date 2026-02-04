import request from './request';

export function getPermissionListApi(): Promise<string[]> {
  return request.get('/v1/org/permissions');
}

export function checkPermissionApi(userId: number, permission: string): Promise<boolean> {
  return request.get('/v1/org/permissions/check', { params: { userId, permission } });
}
