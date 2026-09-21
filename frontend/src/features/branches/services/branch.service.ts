import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import {
  BranchResponse,
  CreateBranchRequest,
  UpdateBranchRequest,
} from '@/types/branch.types';

export const branchService = {
  /**
   * Lấy danh sách chi nhánh (Master Data hoặc Admin toàn bộ)
   */
  async getAll(includeInactive: boolean = false): Promise<BranchResponse[]> {
    const response = await apiClient.get<ApiResponse<BranchResponse[]>>(
      API_ENDPOINTS.BRANCHES,
      {
        params: includeInactive ? { includeInactive: true } : undefined,
      }
    );
    return response.data.data || [];
  },

  /**
   * Lấy thông tin chi tiết một chi nhánh theo ID
   */
  async getById(id: number): Promise<BranchResponse> {
    const response = await apiClient.get<ApiResponse<BranchResponse>>(
      `${API_ENDPOINTS.BRANCHES}/${id}`
    );
    return response.data.data;
  },

  /**
   * Tạo mới chi nhánh (Admin only)
   */
  async create(data: CreateBranchRequest): Promise<BranchResponse> {
    const response = await apiClient.post<ApiResponse<BranchResponse>>(
      API_ENDPOINTS.BRANCHES,
      data
    );
    return response.data.data;
  },

  /**
   * Cập nhật thông tin chi nhánh (Admin only)
   */
  async update(id: number, data: UpdateBranchRequest): Promise<BranchResponse> {
    const response = await apiClient.put<ApiResponse<BranchResponse>>(
      `${API_ENDPOINTS.BRANCHES}/${id}`,
      data
    );
    return response.data.data;
  },

  /**
   * Bật/Tắt trạng thái hoạt động của chi nhánh (Admin only)
   */
  async updateStatus(id: number, trangThai: boolean): Promise<BranchResponse> {
    const response = await apiClient.patch<ApiResponse<BranchResponse>>(
      `${API_ENDPOINTS.BRANCHES}/${id}/status`,
      { trangThai }
    );
    return response.data.data;
  },

  /**
   * Xóa chi nhánh (Admin only)
   */
  async delete(id: number): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`${API_ENDPOINTS.BRANCHES}/${id}`);
  },
};
