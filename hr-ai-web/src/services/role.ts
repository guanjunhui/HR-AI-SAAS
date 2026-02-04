import request from './request';
import type {
  RoleCreateRequest,
  RoleDetailResponse,
  RolePermissionsUpdateRequest,
  RoleUpdateRequest,
} from '@/types/role';

export function getRolesApi(params?: { keyword?: string; status?: number }): Promise<RoleDetailResponse[]> {
  return request.get('/v1/org/roles', { params });
}

export function getRoleApi(id: number): Promise<RoleDetailResponse> {
  return request.get(`/v1/org/roles/${id}`);
}

export function createRoleApi(params: RoleCreateRequest): Promise<number> {
  return request.post('/v1/org/roles', params);
}

export function updateRoleApi(id: number, params: RoleUpdateRequest): Promise<void> {
  return request.put(`/v1/org/roles/${id}`, params);
}

export function deleteRoleApi(id: number): Promise<void> {
  return request.delete(`/v1/org/roles/${id}`);
}

export function updateRolePermissionsApi(id: number, params: RolePermissionsUpdateRequest): Promise<void> {
  return request.put(`/v1/org/roles/${id}/permissions`, params);
}
