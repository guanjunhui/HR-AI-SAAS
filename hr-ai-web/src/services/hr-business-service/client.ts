import type { AxiosRequestConfig } from 'axios';
import request from '@/services/request';
import type { RequestMeta } from '@/types/hr-business-service';

interface ExtendedConfig extends AxiosRequestConfig {
  meta?: RequestMeta;
}

export const hrClient = {
  get<T>(url: string, config?: ExtendedConfig): Promise<T> {
    return request.get(url, config);
  },
  post<T>(url: string, data?: unknown, config?: ExtendedConfig): Promise<T> {
    return request.post(url, data, config);
  },
  put<T>(url: string, data?: unknown, config?: ExtendedConfig): Promise<T> {
    return request.put(url, data, config);
  },
  delete<T>(url: string, config?: ExtendedConfig): Promise<T> {
    return request.delete(url, config);
  },
};

export type { ExtendedConfig };
