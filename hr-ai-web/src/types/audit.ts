export interface AuditLogDetailResponse {
  id: number;
  tenantId?: string | null;
  userId?: number | null;
  username?: string | null;
  action?: string | null;
  resource?: string | null;
  resourceId?: string | null;
  resourceName?: string | null;
  detail?: string | null;
  result?: string | null;
  errorMessage?: string | null;
  ip?: string | null;
  userAgent?: string | null;
  duration?: number | null;
  traceId?: string | null;
  createdAt?: string | null;
}

export interface AuditLogQueryParams {
  pageNo?: number;
  pageSize?: number;
  userId?: number;
  action?: string;
  resource?: string;
  keyword?: string;
  startTime?: string;
  endTime?: string;
}
