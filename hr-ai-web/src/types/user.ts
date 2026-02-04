export interface UserDetailResponse {
  id: number;
  username: string;
  realName?: string | null;
  email?: string | null;
  phone?: string | null;
  avatar?: string | null;
  status: number;
  roleId?: number | null;
  roleName?: string | null;
  roleCode?: string | null;
  orgUnitId?: number | null;
  planType?: string | null;
  lastLoginTime?: string | null;
  lastLoginIp?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface UserQueryParams {
  pageNo?: number;
  pageSize?: number;
  keyword?: string;
  orgUnitId?: number;
  roleId?: number;
  status?: number;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  realName?: string;
  email?: string;
  phone?: string;
  avatar?: string;
  orgUnitId?: number | null;
  roleId: number;
  status: number;
  planType?: string;
}

export interface UserUpdateRequest {
  username: string;
  realName?: string;
  email?: string;
  phone?: string;
  avatar?: string;
  orgUnitId?: number | null;
  status: number;
  planType?: string;
}

export interface UserStatusUpdateRequest {
  status: number;
}

export interface UserPasswordUpdateRequest {
  newPassword: string;
}

export interface UserRoleUpdateRequest {
  roleId: number;
}
