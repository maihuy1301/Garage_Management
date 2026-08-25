import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import {
  EmployeeResponse,
  CreateEmployeeRequest,
  UpdateEmployeeRequest,
  UpdateEmployeeStatusRequest,
} from '@/types/employee.types';

export const employeeService = {
  async getAll(): Promise<EmployeeResponse[]> {
    const response = await apiClient.get<ApiResponse<EmployeeResponse[]>>(API_ENDPOINTS.EMPLOYEES);
    return response.data.data || [];
  },

  async getById(id: number): Promise<EmployeeResponse> {
    const response = await apiClient.get<ApiResponse<EmployeeResponse>>(`${API_ENDPOINTS.EMPLOYEES}/${id}`);
    return response.data.data;
  },

  async create(data: CreateEmployeeRequest): Promise<EmployeeResponse> {
    const response = await apiClient.post<ApiResponse<EmployeeResponse>>(API_ENDPOINTS.EMPLOYEES, data);
    return response.data.data;
  },

  async update(id: number, data: UpdateEmployeeRequest): Promise<EmployeeResponse> {
    const response = await apiClient.put<ApiResponse<EmployeeResponse>>(`${API_ENDPOINTS.EMPLOYEES}/${id}`, data);
    return response.data.data;
  },

  async updateStatus(id: number, status: boolean): Promise<EmployeeResponse> {
    const payload: UpdateEmployeeStatusRequest = { trangThai: status };
    const response = await apiClient.patch<ApiResponse<EmployeeResponse>>(
      `${API_ENDPOINTS.EMPLOYEES}/${id}/status`,
      payload
    );
    return response.data.data;
  },
};
