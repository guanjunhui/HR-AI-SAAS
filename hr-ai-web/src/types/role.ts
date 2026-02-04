export interface RoleDetailResponse {
  id: number;
  name: string;
  code: string;
  description?: string | null;
  permissions?: string[] | null;
  dataScope: number;
  status: number;
  sortOrder?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface RoleCreateRequest {
  name: string;
  code: string;
  description?: string;
  permissions?: string[];
  dataScope: number;
  status: number;
  sortOrder?: number;
}

export interface RoleUpdateRequest {
  name: string;
  code: string;
  description?: string;
  permissions?: string[];
  dataScope: number;
  status: number;
  sortOrder?: number;
}

export interface RolePermissionsUpdateRequest {
  permissions: string[];
}
