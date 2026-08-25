import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import {
  CustomerResponse,
  CreateCustomerRequest,
  UpdateCustomerRequest,
  UpdateCustomerStatusRequest,
} from '@/types/customer.types';
import { VehicleResponse } from '@/types/vehicle.types';

export const customerService = {
  async getAll(): Promise<CustomerResponse[]> {
    const response = await apiClient.get<ApiResponse<CustomerResponse[]>>(API_ENDPOINTS.CUSTOMERS);
    return response.data.data || [];
  },

  async getById(id: number): Promise<CustomerResponse> {
    const response = await apiClient.get<ApiResponse<CustomerResponse>>(`${API_ENDPOINTS.CUSTOMERS}/${id}`);
    return response.data.data;
  },

  async getCurrentProfile(): Promise<CustomerResponse> {
    const response = await apiClient.get<ApiResponse<CustomerResponse>>(`${API_ENDPOINTS.CUSTOMERS}/me`);
    return response.data.data;
  },

  async create(data: CreateCustomerRequest): Promise<CustomerResponse> {
    const response = await apiClient.post<ApiResponse<CustomerResponse>>(API_ENDPOINTS.CUSTOMERS, data);
    return response.data.data;
  },

  async update(id: number, data: UpdateCustomerRequest): Promise<CustomerResponse> {
    const response = await apiClient.put<ApiResponse<CustomerResponse>>(`${API_ENDPOINTS.CUSTOMERS}/${id}`, data);
    return response.data.data;
  },

  async updateCurrentProfile(data: UpdateCustomerRequest): Promise<CustomerResponse> {
    const response = await apiClient.put<ApiResponse<CustomerResponse>>(`${API_ENDPOINTS.CUSTOMERS}/me`, data);
    return response.data.data;
  },

  async updateStatus(id: number, status: boolean): Promise<CustomerResponse> {
    const payload: UpdateCustomerStatusRequest = { trangThai: status };
    const response = await apiClient.patch<ApiResponse<CustomerResponse>>(
      `${API_ENDPOINTS.CUSTOMERS}/${id}/status`,
      payload
    );
    return response.data.data;
  },

  async getVehicles(): Promise<VehicleResponse[]> {
    try {
      const response = await apiClient.get<ApiResponse<VehicleResponse[]>>(API_ENDPOINTS.VEHICLES);
      return response.data.data || [];
    } catch {
      return [];
    }
  },
};
