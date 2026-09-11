import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import { ReceptionResponse, CheckInRequest } from '@/types/reception.types';
import { RepairOrderResponse } from '@/types/repair-order.types';

export const receptionService = {
  /**
   * Lấy danh sách tất cả phiếu tiếp nhận xe
   */
  getReceptionSlips: async (): Promise<ReceptionResponse[]> => {
    const response = await apiClient.get<ApiResponse<ReceptionResponse[]>>(
      API_ENDPOINTS.RECEPTIONS
    );
    return response.data.data;
  },

  /**
   * Lấy chi tiết phiếu tiếp nhận theo ID
   */
  getReceptionSlipById: async (id: number): Promise<ReceptionResponse> => {
    const response = await apiClient.get<ApiResponse<ReceptionResponse>>(
      `${API_ENDPOINTS.RECEPTIONS}/${id}`
    );
    return response.data.data;
  },

  /**
   * Check-in / Tiếp nhận xe từ lịch hẹn
   */
  checkIn: async (
    appointmentId: number,
    data?: CheckInRequest
  ): Promise<ReceptionResponse> => {
    const response = await apiClient.post<ApiResponse<ReceptionResponse>>(
      `${API_ENDPOINTS.RECEPTIONS}/check-in/${appointmentId}`,
      data || {}
    );
    return response.data.data;
  },

  /**
   * Tạo phiếu sửa chữa từ phiếu tiếp nhận
   */
  createRepairOrder: async (data: {
    maTiepNhan: number;
    ghiChu?: string;
    thoiGianBatDau?: string;
  }): Promise<RepairOrderResponse> => {
    const response = await apiClient.post<ApiResponse<RepairOrderResponse>>(
      API_ENDPOINTS.REPAIR_ORDERS,
      data
    );
    return response.data.data;
  },

  /**
   * Lấy danh sách các phiếu sửa chữa hiện có
   */
  getRepairOrders: async (): Promise<RepairOrderResponse[]> => {
    const response = await apiClient.get<ApiResponse<RepairOrderResponse[]>>(
      API_ENDPOINTS.REPAIR_ORDERS
    );
    return response.data.data;
  },
};
