import request from './request';
import type { PageResponse } from '@/types/common';
import type {
    PositionCreateRequest,
    PositionDetailResponse,
    PositionQueryParams,
    PositionUpdateRequest,
    HeadcountCreateRequest,
    HeadcountDetailResponse,
    HeadcountQueryParams,
    HeadcountUpdateRequest,
    EmployeeCreateRequest,
    EmployeeDetailResponse,
    EmployeeQueryParams,
    EmployeeUpdateRequest,
    EmploymentEventCreateRequest,
    EmploymentEventDetailResponse,
    EmploymentEventApproveRequest,
    EmploymentEventQueryParams,
    EmploymentEventStatisticsDTO,
} from '@/types/hr';

// ==================== Position 岗位 API ====================

export function getPositionsApi(params: PositionQueryParams): Promise<PageResponse<PositionDetailResponse>> {
    return request.get('/v1/hr/positions', { params });
}

export function getPositionApi(id: number): Promise<PositionDetailResponse> {
    return request.get(`/v1/hr/positions/${id}`);
}

export function getEnabledPositionsApi(): Promise<PositionDetailResponse[]> {
    return request.get('/v1/hr/positions/enabled');
}

export function createPositionApi(data: PositionCreateRequest): Promise<number> {
    return request.post('/v1/hr/positions', data);
}

export function updatePositionApi(id: number, data: PositionUpdateRequest): Promise<void> {
    return request.put(`/v1/hr/positions/${id}`, data);
}

export function deletePositionApi(id: number): Promise<void> {
    return request.delete(`/v1/hr/positions/${id}`);
}

// ==================== Headcount 编制 API ====================

export function getHeadcountsApi(params: HeadcountQueryParams): Promise<PageResponse<HeadcountDetailResponse>> {
    return request.get('/v1/hr/headcounts', { params });
}

export function getHeadcountApi(id: number): Promise<HeadcountDetailResponse> {
    return request.get(`/v1/hr/headcounts/${id}`);
}

export function createHeadcountApi(data: HeadcountCreateRequest): Promise<number> {
    return request.post('/v1/hr/headcounts', data);
}

export function updateHeadcountApi(id: number, data: HeadcountUpdateRequest): Promise<void> {
    return request.put(`/v1/hr/headcounts/${id}`, data);
}

export function deleteHeadcountApi(id: number): Promise<void> {
    return request.delete(`/v1/hr/headcounts/${id}`);
}

// ==================== Employee 员工 API ====================

export function getEmployeesApi(params: EmployeeQueryParams): Promise<PageResponse<EmployeeDetailResponse>> {
    return request.get('/v1/hr/employees', { params });
}

export function getEmployeeApi(id: number): Promise<EmployeeDetailResponse> {
    return request.get(`/v1/hr/employees/${id}`);
}

export function createEmployeeApi(data: EmployeeCreateRequest): Promise<number> {
    return request.post('/v1/hr/employees', data);
}

export function updateEmployeeApi(id: number, data: EmployeeUpdateRequest): Promise<void> {
    return request.put(`/v1/hr/employees/${id}`, data);
}

export function deleteEmployeeApi(id: number): Promise<void> {
    return request.delete(`/v1/hr/employees/${id}`);
}

// ==================== EmploymentEvent 任职事件 API ====================

export function getEmploymentEventsApi(params: EmploymentEventQueryParams): Promise<PageResponse<EmploymentEventDetailResponse>> {
    return request.get('/v1/hr/employment-events', { params });
}

export function getEmploymentEventApi(id: number): Promise<EmploymentEventDetailResponse> {
    return request.get(`/v1/hr/employment-events/${id}`);
}

export function createEmploymentEventApi(data: EmploymentEventCreateRequest): Promise<number> {
    return request.post('/v1/hr/employment-events', data);
}

export function submitEmploymentEventApi(id: number): Promise<void> {
    return request.post(`/v1/hr/employment-events/${id}/submit`);
}

export function approveEmploymentEventApi(id: number, data: EmploymentEventApproveRequest): Promise<void> {
    return request.post(`/v1/hr/employment-events/${id}/approve`, data);
}

export function cancelEmploymentEventApi(id: number): Promise<void> {
    return request.post(`/v1/hr/employment-events/${id}/cancel`);
}

export function deleteEmploymentEventApi(id: number): Promise<void> {
    return request.delete(`/v1/hr/employment-events/${id}`);
}

export function importEmploymentEventsApi(file: File): Promise<void> {
    const formData = new FormData();
    formData.append('file', file);
    return request.post('/v1/hr/employment-events/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
}

export function downloadEmploymentEventTemplateApi(): Promise<Blob> {
    return request.get('/v1/hr/employment-events/import/template', {
        responseType: 'blob',
    });
}

export function getEmploymentEventsStatisticsApi(): Promise<EmploymentEventStatisticsDTO> {
    return request.get('/v1/hr/employment-events/statistics');
}
