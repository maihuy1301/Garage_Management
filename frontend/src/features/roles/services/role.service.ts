import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import { RoleResponse } from '@/types/role.types';

export const roleService = {
  /**
   * Lấy danh sách Vai trò (Master Data) từ GET /api/roles
   */
  async getAll(): Promise<RoleResponse[]> {
    const response = await apiClient.get<ApiResponse<RoleResponse[]>>(API_ENDPOINTS.ROLES);
    return response.data.data || [];
  },
};
