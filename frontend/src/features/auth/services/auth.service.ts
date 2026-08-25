import apiClient from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import { LoginRequest, LoginResponse } from '@/types/auth.types';

export interface MeResponseData {
  maNguoiDung: number;
  tenDangNhap: string;
  hoTen: string;
  email?: string;
}

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.post<ApiResponse<LoginResponse>>(
      API_ENDPOINTS.AUTH.LOGIN,
      credentials
    );
    return response.data.data;
  },

  async getCurrentUser(): Promise<MeResponseData> {
    const response = await apiClient.get<ApiResponse<MeResponseData>>(
      API_ENDPOINTS.AUTH.ME
    );
    return response.data.data;
  },
};
