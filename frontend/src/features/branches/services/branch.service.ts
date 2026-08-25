import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import { BranchResponse } from '@/types/branch.types';

export const branchService = {
  /**
   * Lấy danh sách chi nhánh (Master Data)
   */
  async getAll(): Promise<BranchResponse[]> {
    const response = await apiClient.get<ApiResponse<BranchResponse[]>>(API_ENDPOINTS.BRANCHES);
    return response.data.data || [];
  },

  /**
   * Lấy thông tin chi tiết một chi nhánh theo ID
   */
  async getById(id: number): Promise<BranchResponse> {
    const response = await apiClient.get<ApiResponse<BranchResponse>>(`${API_ENDPOINTS.BRANCHES}/${id}`);
    return response.data.data;
  },
};
