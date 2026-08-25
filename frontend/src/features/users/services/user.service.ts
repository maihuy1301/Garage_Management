import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import {
  UserResponse,
  CreateUserRequest,
  UpdateUserRequest,
  UpdateUserStatusRequest,
} from '@/types/user.types';

export const userService = {
  async getAll(): Promise<UserResponse[]> {
    const response = await apiClient.get<ApiResponse<UserResponse[]>>(API_ENDPOINTS.ADMIN_USERS);
    return response.data.data || [];
  },

  async getById(id: number): Promise<UserResponse> {
    const response = await apiClient.get<ApiResponse<UserResponse>>(`${API_ENDPOINTS.ADMIN_USERS}/${id}`);
    return response.data.data;
  },

  async create(data: CreateUserRequest): Promise<UserResponse> {
    const response = await apiClient.post<ApiResponse<UserResponse>>(API_ENDPOINTS.ADMIN_USERS, data);
    return response.data.data;
  },

  async update(id: number, data: UpdateUserRequest): Promise<UserResponse> {
    const response = await apiClient.put<ApiResponse<UserResponse>>(`${API_ENDPOINTS.ADMIN_USERS}/${id}`, data);
    return response.data.data;
  },

  async updateStatus(id: number, status: boolean): Promise<UserResponse> {
    const payload: UpdateUserStatusRequest = { trangThai: status };
    const response = await apiClient.patch<ApiResponse<UserResponse>>(
      `${API_ENDPOINTS.ADMIN_USERS}/${id}/status`,
      payload
    );
    return response.data.data;
  },
};
