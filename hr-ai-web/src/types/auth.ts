/**
 * 认证相关类型定义 - 对齐后端 LoginRequest / LoginResponse
 */

/** 登录请求参数 */
export interface LoginParams {
  username: string;
  password: string;
  tenantId?: string;
  rememberMe?: boolean;
}

/** 用户信息（对齐后端 LoginResponse.UserInfo） */
export interface UserInfo {
  userId: number;
  username: string;
  realName: string;
  email: string;
  phone: string;
  avatar: string;
  tenantId: string;
  roleCode: string;
  roleName: string;
  planType: string;
}

/** 登录响应（对齐后端 LoginResponse） */
export interface LoginResult {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userInfo: UserInfo;
}

/** 刷新 Token 请求参数 */
export interface RefreshTokenParams {
  refreshToken: string;
}
