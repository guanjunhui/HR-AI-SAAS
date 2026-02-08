// ==================== Position 岗位相关 ====================

export interface PositionDetailResponse {
    id: number;
    tenantId?: string;
    positionCode: string;
    positionName: string;
    positionLevel?: string;
    jobFamily?: string;
    orgUnitId?: number;
    description?: string;
    requirements?: string;
    status: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface PositionQueryParams {
    pageNo?: number;
    pageSize?: number;
    keyword?: string;
    orgUnitId?: number;
    status?: number;
}

export interface PositionCreateRequest {
    positionCode: string;
    positionName: string;
    positionLevel?: string;
    jobFamily?: string;
    orgUnitId?: number;
    description?: string;
    requirements?: string;
    status?: number;
}

export interface PositionUpdateRequest {
    positionCode?: string;
    positionName?: string;
    positionLevel?: string;
    jobFamily?: string;
    orgUnitId?: number;
    description?: string;
    requirements?: string;
    status?: number;
}

// ==================== Headcount 编制相关 ====================

export interface HeadcountDetailResponse {
    id: number;
    tenantId?: string;
    orgUnitId: number;
    orgUnitName?: string;
    positionId: number;
    positionName?: string;
    budgetCount: number;
    actualCount: number;
    year?: number;
    quarter?: number;
    status: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface HeadcountQueryParams {
    pageNo?: number;
    pageSize?: number;
    orgUnitId?: number;
    positionId?: number;
    year?: number;
    quarter?: number;
    status?: number;
}

export interface HeadcountCreateRequest {
    orgUnitId: number;
    positionId: number;
    budgetCount: number;
    year?: number;
    quarter?: number;
    status?: number;
}

export interface HeadcountUpdateRequest {
    budgetCount?: number;
    year?: number;
    quarter?: number;
    status?: number;
}

// ==================== Employee 员工相关 ====================

export interface EmployeeDetailResponse {
    id: number;
    tenantId?: string;
    employeeCode: string;
    sysUserId?: number;
    realName: string;
    gender?: string;
    phone?: string;
    email?: string;
    idCard?: string;
    orgUnitId?: number;
    orgUnitName?: string;
    positionId?: number;
    positionName?: string;
    directManagerId?: number;
    directManagerName?: string;
    entryDate?: string;
    probationEndDate?: string;
    regularDate?: string;
    resignationDate?: string;
    employeeStatus: string;
    workLocation?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface EmployeeQueryParams {
    pageNo?: number;
    pageSize?: number;
    keyword?: string;
    orgUnitId?: number;
    positionId?: number;
    employeeStatus?: string;
}

export interface EmployeeCreateRequest {
    employeeCode: string;
    sysUserId?: number;
    realName: string;
    gender?: string;
    phone?: string;
    email?: string;
    idCard?: string;
    orgUnitId?: number;
    positionId?: number;
    directManagerId?: number;
    entryDate?: string;
    probationEndDate?: string;
    employeeStatus?: string;
    workLocation?: string;
}

export interface EmployeeUpdateRequest {
    employeeCode?: string;
    realName?: string;
    gender?: string;
    phone?: string;
    email?: string;
    idCard?: string;
    orgUnitId?: number;
    positionId?: number;
    directManagerId?: number;
    entryDate?: string;
    probationEndDate?: string;
    regularDate?: string;
    resignationDate?: string;
    employeeStatus?: string;
    workLocation?: string;
}

// ==================== EmploymentEvent 任职事件相关 ====================

export type EmploymentEventType = 'entry' | 'regular' | 'transfer' | 'promotion' | 'demotion' | 'salary_change' | 'resignation';
export type EmploymentEventStatus = 'draft' | 'pending' | 'approved' | 'rejected' | 'cancelled';

export interface EmploymentEventDetailResponse {
    id: number;
    tenantId?: string;
    employeeId: number;
    employeeName?: string;
    eventType: EmploymentEventType;
    eventDate: string;
    reason?: string;
    fromOrgUnitId?: number;
    fromOrgUnitName?: string;
    fromPositionId?: number;
    fromPositionName?: string;
    fromSalaryGrade?: string;
    toOrgUnitId?: number;
    toOrgUnitName?: string;
    toPositionId?: number;
    toPositionName?: string;
    toSalaryGrade?: string;
    status: EmploymentEventStatus;
    applicantId?: number;
    applicantName?: string;
    approverId?: number;
    approverName?: string;
    approvedAt?: string;
    rejectReason?: string;
    remark?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface EmploymentEventQueryParams {
    pageNo?: number;
    pageSize?: number;
    employeeId?: number;
    eventType?: EmploymentEventType;
    status?: EmploymentEventStatus;
    keyword?: string;
}

export interface EmploymentEventCreateRequest {
    employeeId: number;
    eventType: EmploymentEventType;
    eventDate: string;
    reason?: string;
    fromOrgUnitId?: number;
    fromPositionId?: number;
    fromSalaryGrade?: string;
    toOrgUnitId?: number;
    toPositionId?: number;
    toSalaryGrade?: string;
    remark?: string;
}

export interface EmploymentEventApproveRequest {
    approved: boolean;
    rejectReason?: string;
}

export interface EmploymentEventStatisticsStatItem {
    key: string;
    count: number;
}

export interface EmploymentEventStatisticsDTO {
    byType: EmploymentEventStatisticsStatItem[];
    byStatus: EmploymentEventStatisticsStatItem[];
}

// ==================== 常量 ====================

export const EMPLOYEE_STATUS_MAP: Record<string, string> = {
    trial: '试用期',
    regular: '正式',
    resigned: '离职',
};

export const EVENT_TYPE_MAP: Record<EmploymentEventType, string> = {
    entry: '入职',
    regular: '转正',
    transfer: '调岗',
    promotion: '晋升',
    demotion: '降级',
    salary_change: '调薪',
    resignation: '离职',
};

export const EVENT_STATUS_MAP: Record<EmploymentEventStatus, string> = {
    draft: '草稿',
    pending: '待审批',
    approved: '已通过',
    rejected: '已驳回',
    cancelled: '已取消',
};
