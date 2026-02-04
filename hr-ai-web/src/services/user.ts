import request from './request';
import type { PageResponse } from '@/types/common';
import type {
  UserCreateRequest,
  UserDetailResponse,
  UserPasswordUpdateRequest,
  UserQueryParams,
  UserRoleUpdateRequest,
  UserStatusUpdateRequest,
  UserUpdateRequest,
} from '@/types/user';

export function getUsersApi(params: UserQueryParams): Promise<PageResponse<UserDetailResponse>> {
  return request.get('/v1/org/users', { params });
}

export function getUserApi(id: number): Promise<UserDetailResponse> {
  return request.get(`/v1/org/users/${id}`);
}

export function createUserApi(params: UserCreateRequest): Promise<number> {
  return request.post('/v1/org/users', params);
}

export function updateUserApi(id: number, params: UserUpdateRequest): Promise<void> {
  return request.put(`/v1/org/users/${id}`, params);
}

export function deleteUserApi(id: number): Promise<void> {
  return request.delete(`/v1/org/users/${id}`);
}

export function updateUserStatusApi(id: number, params: UserStatusUpdateRequest): Promise<void> {
  return request.put(`/v1/org/users/${id}/status`, params);
}

export function updateUserPasswordApi(id: number, params: UserPasswordUpdateRequest): Promise<void> {
  return request.put(`/v1/org/users/${id}/password`, params);
}

export function updateUserRoleApi(id: number, params: UserRoleUpdateRequest): Promise<void> {
  return request.put(`/v1/org/users/${id}/roles`, params);
}
