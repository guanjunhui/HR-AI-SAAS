/**
 * 认证相关 API
 *
 * 路径使用 /v1/auth/xxx，拼接 baseURL 后为 /api/v1/auth/xxx
 * → Vite 代理到网关 → 网关转发到 hr-org-service
 */
import request from './request';
import type { LoginParams, LoginResult, RefreshTokenParams, UserInfo } from '@/types/auth';

/** 登录 */
export function loginApi(params: LoginParams): Promise<LoginResult> {
  return request.post('/v1/auth/login', params);
}

/** 刷新 Token */
export function refreshTokenApi(params: RefreshTokenParams): Promise<LoginResult> {
  return request.post('/v1/auth/refresh', params);
}

/** 登出 */
export function logoutApi(): Promise<void> {
  return request.post('/v1/auth/logout');
}

/** 获取当前用户信息 */
export function getCurrentUserApi(): Promise<UserInfo> {
  return request.get('/v1/auth/me');
}
