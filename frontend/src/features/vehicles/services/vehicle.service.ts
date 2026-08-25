import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import {
  VehicleResponse,
  CreateVehicleRequest,
  UpdateVehicleRequest,
} from '@/types/vehicle.types';

export const vehicleService = {
  /**
   * Lấy danh sách phương tiện
   * - ROLE_ADMIN: Lấy toàn bộ xe trong hệ thống
   * - ROLE_CUSTOMER: Lấy danh sách xe thuộc sở hữu của chính mình
   */
  async getAll(): Promise<VehicleResponse[]> {
    const response = await apiClient.get<ApiResponse<VehicleResponse[]>>(API_ENDPOINTS.VEHICLES);
    return response.data.data || [];
  },

  /**
   * Lấy thông tin chi tiết một phương tiện
   */
  async getById(id: number): Promise<VehicleResponse> {
    const response = await apiClient.get<ApiResponse<VehicleResponse>>(`${API_ENDPOINTS.VEHICLES}/${id}`);
    return response.data.data;
  },

  /**
   * Tạo mới phương tiện
   * - ROLE_ADMIN: Phải truyền maKhachHang
   * - ROLE_CUSTOMER: Backend tự động gắn owner từ JWT context
   */
  async create(data: CreateVehicleRequest): Promise<VehicleResponse> {
    const response = await apiClient.post<ApiResponse<VehicleResponse>>(API_ENDPOINTS.VEHICLES, data);
    return response.data.data;
  },

  /**
   * Cập nhật thông số phương tiện (không thay đổi chủ sở hữu và biển số)
   */
  async update(id: number, data: UpdateVehicleRequest): Promise<VehicleResponse> {
    const response = await apiClient.put<ApiResponse<VehicleResponse>>(`${API_ENDPOINTS.VEHICLES}/${id}`, data);
    return response.data.data;
  },

  /**
   * Xóa phương tiện (nếu không có lịch hẹn hoặc lệnh sửa chữa liên quan)
   */
  async delete(id: number): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`${API_ENDPOINTS.VEHICLES}/${id}`);
  },
};
