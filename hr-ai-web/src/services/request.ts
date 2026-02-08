import axios, { type AxiosError, type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/stores/useAuthStore';
import { useNotificationStore } from '@/stores/useNotificationStore';
import type { ApiErrorModel, ApiResult, RequestMeta } from '@/types/hr-business-service';

interface RequestConfig extends InternalAxiosRequestConfig {
  meta?: RequestMeta;
  __retryCount?: number;
  __retryAuth?: boolean;
}

const DEFAULT_TIMEOUT = 15000;
const AI_TIMEOUT = 30000;
const DEFAULT_RETRY = 1;

class AppServiceError extends Error {
  apiError: ApiErrorModel;

  constructor(apiError: ApiErrorModel) {
    super(apiError.message);
    this.apiError = apiError;
  }
}

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: DEFAULT_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

let isRefreshing = false;
interface PendingRequest {
  config: RequestConfig;
  resolve: (value: unknown) => void;
  reject: (reason?: unknown) => void;
}

let pendingRequests: PendingRequest[] = [];

function resolvePendingRequests(newToken: string): void {
  pendingRequests.forEach(({ config, resolve }) => {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${newToken}`;
    resolve(request(config));
  });
  pendingRequests = [];
}

function rejectPendingRequests(reason: unknown): void {
  pendingRequests.forEach(({ reject }) => reject(reason));
  pendingRequests = [];
}

function addPendingRequest(requestItem: PendingRequest): void {
  pendingRequests.push(requestItem);
}

function buildIdempotencyKey(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function shouldRetry(error: AxiosError, config: RequestConfig): boolean {
  const method = (config.method || 'get').toLowerCase();
  const retry = config.meta?.retry ?? DEFAULT_RETRY;
  const current = config.__retryCount ?? 0;
  if (current >= retry) {
    return false;
  }

  if (method !== 'get') {
    // 非幂等写请求禁止自动重试，避免副作用重复执行
    if (!config.meta?.idempotent || retry <= 0) {
      return false;
    }
  }

  if (!error.response) {
    return true;
  }

  const status = error.response.status;
  return status === 429 || status >= 500;
}

function toApiError(error: AxiosError): ApiErrorModel {
  const status = error.response?.status;
  const responseData = error.response?.data as Partial<ApiResult<unknown>> | undefined;

  if (responseData && typeof responseData.code === 'number') {
    return {
      code: responseData.code,
      message: responseData.message || '请求失败',
      requestId: responseData.requestId,
      timestamp: responseData.timestamp,
      status,
    };
  }

  return {
    code: status || 500,
    message: error.message || '网络异常',
    status,
  };
}

function notifyApiError(apiError: ApiErrorModel): void {
  useNotificationStore.getState().pushNotice({
    type: 'error',
    title: `请求失败 (${apiError.code})`,
    description: apiError.message,
  });
}

request.interceptors.request.use(
  (config: RequestConfig) => {
    const { token, user } = useAuthStore.getState();

    if (!config.meta?.skipAuth && token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    if (!config.headers['X-Tenant-Id'] && user?.tenantId) {
      config.headers['X-Tenant-Id'] = user.tenantId;
    }

    const method = (config.method || 'get').toLowerCase();
    const isWriteMethod = ['post', 'put', 'patch', 'delete'].includes(method);
    if ((config.meta?.idempotent ?? isWriteMethod) && !config.headers['X-Idempotency-Key']) {
      config.headers['X-Idempotency-Key'] = buildIdempotencyKey();
    }

    if (typeof config.meta?.timeout === 'number') {
      config.timeout = config.meta.timeout;
    } else if (typeof config.url === 'string' && config.url.includes('/ai/')) {
      config.timeout = AI_TIMEOUT;
    }

    return config;
  },
  (error: AxiosError) => Promise.reject(error),
);

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const payload = response.data as Partial<ApiResult<unknown>>;

    if (typeof payload?.code === 'number') {
      if (payload.code === 200) {
        return payload.data;
      }

      const apiError: ApiErrorModel = {
        code: payload.code,
        message: payload.message || '请求失败',
        requestId: payload.requestId,
        timestamp: payload.timestamp,
        status: response.status,
      };
      notifyApiError(apiError);
      return Promise.reject(new AppServiceError(apiError));
    }

    return response.data;
  },
  async (error: AxiosError) => {
    const config = (error.config || {}) as RequestConfig;
    const response = error.response;

    if (response && response.status === 401 && config) {
      const { refreshToken, logout, setTokens } = useAuthStore.getState();

      if (config.__retryAuth) {
        logout();
        window.location.href = '/login';
        return Promise.reject(error);
      }
      config.__retryAuth = true;

      if (!refreshToken) {
        logout();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          addPendingRequest({
            config,
            resolve,
            reject,
          });
        });
      }

      isRefreshing = true;

      try {
        const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
        const refreshResp = await axios.post(`${baseURL}/v1/auth/refresh`, { refreshToken });
        const refreshResult = refreshResp.data as ApiResult<{ accessToken: string; refreshToken: string; expiresIn: number }>;

        if (refreshResult.code === 200 && refreshResult.data) {
          const { accessToken, refreshToken: nextRefreshToken, expiresIn } = refreshResult.data;
          setTokens(accessToken, nextRefreshToken, expiresIn);
          resolvePendingRequests(accessToken);
          config.headers.Authorization = `Bearer ${accessToken}`;
          return request(config);
        }

        rejectPendingRequests(error);
        logout();
        window.location.href = '/login';
        return Promise.reject(error);
      } catch (refreshError) {
        rejectPendingRequests(refreshError);
        logout();
        window.location.href = '/login';
        return Promise.reject(error);
      } finally {
        isRefreshing = false;
      }
    }

    if (shouldRetry(error, config)) {
      const current = config.__retryCount ?? 0;
      const delayMs = config.meta?.retryDelayMs ?? 300;
      config.__retryCount = current + 1;
      await new Promise((resolve) => {
        window.setTimeout(resolve, delayMs * (current + 1));
      });
      return request(config);
    }

    const apiError = toApiError(error);

    if (apiError.status === 403) {
      message.error('没有权限访问该资源');
    } else if (apiError.status === 500) {
      message.error('服务器错误，请稍后重试');
    } else if (!apiError.status) {
      message.error('网络异常，请检查连接');
    }

    notifyApiError(apiError);

    return Promise.reject(new AppServiceError(apiError));
  },
);

export default request;
export { AppServiceError };
