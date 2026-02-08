import axios from 'axios';
import { AppServiceError } from '@/services/request';

const FALLBACK_PREFIX = 'hr-ai-web:fallback:';

function isServiceUnavailableError(error: unknown): boolean {
  if (error instanceof AppServiceError) {
    const status = error.apiError.status;
    if (typeof status === 'number') {
      if (status >= 500 || status === 429 || status === 408) {
        return true;
      }
      // 4xx 或业务码错误（通常为 HTTP 200 + 业务失败码）不应触发降级兜底
      return false;
    }
    return true;
  }

  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    if (typeof status === 'number') {
      if (status >= 500 || status === 429 || status === 408) {
        return true;
      }
      return false;
    }
    return true;
  }

  return false;
}

export function loadFallback<T>(key: string): T | null {
  try {
    const raw = window.localStorage.getItem(`${FALLBACK_PREFIX}${key}`);
    if (!raw) {
      return null;
    }
    return JSON.parse(raw) as T;
  } catch {
    return null;
  }
}

export function saveFallback<T>(key: string, value: T): void {
  try {
    window.localStorage.setItem(`${FALLBACK_PREFIX}${key}`, JSON.stringify(value));
  } catch {
    // 本地缓存失败不影响主流程
  }
}

export async function withFallback<T>(
  key: string,
  fn: () => Promise<T>,
  fallbackFactory?: () => T,
): Promise<{ data: T; fromFallback: boolean }> {
  try {
    const data = await fn();
    saveFallback(key, data);
    return { data, fromFallback: false };
  } catch (error) {
    if (!isServiceUnavailableError(error)) {
      throw error;
    }

    const cache = loadFallback<T>(key);
    if (cache) {
      return { data: cache, fromFallback: true };
    }
    if (fallbackFactory) {
      const data = fallbackFactory();
      return { data, fromFallback: true };
    }
    throw error;
  }
}
