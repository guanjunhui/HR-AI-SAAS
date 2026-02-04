export interface OrgUnitTreeNode {
  id: number;
  parentId?: number | null;
  name: string;
  code: string;
  type: string;
  leaderId?: number | null;
  sortOrder?: number | null;
  status: number;
  level?: number | null;
  path?: string | null;
  children?: OrgUnitTreeNode[] | null;
}

export interface OrgUnitDetailResponse {
  id: number;
  parentId?: number | null;
  name: string;
  code: string;
  type: string;
  path?: string | null;
  level?: number | null;
  leaderId?: number | null;
  sortOrder?: number | null;
  status: number;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface OrgUnitCreateRequest {
  parentId?: number | null;
  name: string;
  code: string;
  type: string;
  leaderId?: number | null;
  sortOrder?: number | null;
  status: number;
}

export interface OrgUnitUpdateRequest {
  parentId: number | null;
  name: string;
  code: string;
  type: string;
  leaderId?: number | null;
  sortOrder?: number | null;
  status: number;
}
