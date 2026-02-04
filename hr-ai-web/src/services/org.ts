import request from './request';
import type {
  OrgUnitCreateRequest,
  OrgUnitDetailResponse,
  OrgUnitTreeNode,
  OrgUnitUpdateRequest,
} from '@/types/org';

export function getOrgUnitTreeApi(): Promise<OrgUnitTreeNode[]> {
  return request.get('/v1/org/units/tree');
}

export function getOrgUnitApi(id: number): Promise<OrgUnitDetailResponse> {
  return request.get(`/v1/org/units/${id}`);
}

export function createOrgUnitApi(params: OrgUnitCreateRequest): Promise<number> {
  return request.post('/v1/org/units', params);
}

export function updateOrgUnitApi(id: number, params: OrgUnitUpdateRequest): Promise<void> {
  return request.put(`/v1/org/units/${id}`, params);
}

export function deleteOrgUnitApi(id: number): Promise<void> {
  return request.delete(`/v1/org/units/${id}`);
}
