import request from './request';
import type { PageResponse } from '@/types/common';
import type { AuditLogDetailResponse, AuditLogQueryParams } from '@/types/audit';

export function getAuditLogsApi(params: AuditLogQueryParams): Promise<PageResponse<AuditLogDetailResponse>> {
  return request.get('/v1/org/audit-logs', { params });
}

export function getAuditLogApi(id: number): Promise<AuditLogDetailResponse> {
  return request.get(`/v1/org/audit-logs/${id}`);
}
