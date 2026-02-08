export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
  requestId?: string;
  timestamp?: number;
}

export interface ApiErrorDetail {
  field?: string;
  reason: string;
}

export interface ApiErrorModel {
  code: number;
  message: string;
  requestId?: string;
  timestamp?: number;
  status?: number;
  details?: ApiErrorDetail[];
}

export interface PageQuery {
  pageNo?: number;
  pageSize?: number;
  sortField?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PageResponse<T> {
  pageNo: number;
  pageSize: number;
  total: number;
  records: T[];
}

export interface RetryPolicy {
  retry?: number;
  retryDelayMs?: number;
}

export interface RequestMeta extends RetryPolicy {
  timeout?: number;
  skipAuth?: boolean;
  idempotent?: boolean;
  useFallback?: boolean;
  fallbackKey?: string;
}
