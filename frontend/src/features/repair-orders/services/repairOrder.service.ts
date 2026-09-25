import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import {
  RepairOrderResponse,
  CreateRepairOrderRequest,
  CreateDirectRepairOrderRequest,
  UpdateRepairOrderRequest,
  UpdateRepairOrderStatusRequest,
  RepairItemResponse,
  CreateRepairItemRequest,
  UpdateRepairItemRequest,
  RepairPartResponse,
  CreateRepairPartRequest,
  UpdateRepairPartRequest,
  RepairProgressResponse,
  UpdateRepairProgressRequest,
} from '@/types/repair-order.types';

export const repairOrderService = {
  /**
   * Lấy danh sách phiếu sửa chữa (ADMIN, MANAGER, FRONT_DESK)
   */
  getAll: async (): Promise<RepairOrderResponse[]> => {
    const response = await apiClient.get<ApiResponse<RepairOrderResponse[]>>(
      API_ENDPOINTS.REPAIR_ORDERS
    );
    return response.data.data || [];
  },

  /**
   * Lấy chi tiết phiếu sửa chữa theo ID
   */
  getById: async (id: number): Promise<RepairOrderResponse> => {
    const response = await apiClient.get<ApiResponse<RepairOrderResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${id}`
    );
    return response.data.data;
  },

  /**
   * Tạo phiếu sửa chữa từ phiếu tiếp nhận
   */
  create: async (data: CreateRepairOrderRequest): Promise<RepairOrderResponse> => {
    const response = await apiClient.post<ApiResponse<RepairOrderResponse>>(
      API_ENDPOINTS.REPAIR_ORDERS,
      data
    );
    return response.data.data;
  },

  /**
   * Chủ động tạo phiếu sửa chữa (không từ lịch hẹn) hoặc tạo phiếu phát sinh sửa chữa
   */
  createDirect: async (data: CreateDirectRepairOrderRequest): Promise<RepairOrderResponse> => {
    const response = await apiClient.post<ApiResponse<RepairOrderResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/direct`,
      data
    );
    return response.data.data;
  },

  /**
   * Cập nhật thông tin phiếu sửa chữa
   */
  update: async (id: number, data: UpdateRepairOrderRequest): Promise<RepairOrderResponse> => {
    const response = await apiClient.put<ApiResponse<RepairOrderResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${id}`,
      data
    );
    return response.data.data;
  },

  /**
   * Cập nhật trạng thái phiếu sửa chữa
   */
  updateStatus: async (
    id: number,
    data: UpdateRepairOrderStatusRequest
  ): Promise<RepairOrderResponse> => {
    const response = await apiClient.patch<ApiResponse<RepairOrderResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${id}/status`,
      data
    );
    return response.data.data;
  },

  /**
   * Lấy danh sách dịch vụ trong phiếu sửa chữa
   */
  getItems: async (repairOrderId: number): Promise<RepairItemResponse[]> => {
    const response = await apiClient.get<ApiResponse<RepairItemResponse[]>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/items`
    );
    return response.data.data || [];
  },

  /**
   * Thêm dịch vụ vào phiếu sửa chữa
   */
  addItem: async (
    repairOrderId: number,
    data: CreateRepairItemRequest
  ): Promise<RepairItemResponse> => {
    const response = await apiClient.post<ApiResponse<RepairItemResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/items`,
      data
    );
    return response.data.data;
  },

  /**
   * Cập nhật dịch vụ trong phiếu sửa chữa
   */
  updateItem: async (
    repairOrderId: number,
    itemId: number,
    data: UpdateRepairItemRequest
  ): Promise<RepairItemResponse> => {
    const response = await apiClient.put<ApiResponse<RepairItemResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/items/${itemId}`,
      data
    );
    return response.data.data;
  },

  /**
   * Xóa dịch vụ khỏi phiếu sửa chữa
   */
  deleteItem: async (repairOrderId: number, itemId: number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/items/${itemId}`
    );
  },

  /**
   * Lấy danh sách phụ tùng trong phiếu sửa chữa
   */
  getParts: async (repairOrderId: number): Promise<RepairPartResponse[]> => {
    const response = await apiClient.get<ApiResponse<RepairPartResponse[]>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/parts`
    );
    return response.data.data || [];
  },

  /**
   * Thêm phụ tùng vào phiếu sửa chữa
   */
  addPart: async (
    repairOrderId: number,
    data: CreateRepairPartRequest
  ): Promise<RepairPartResponse> => {
    const response = await apiClient.post<ApiResponse<RepairPartResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/parts`,
      data
    );
    return response.data.data;
  },

  /**
   * Cập nhật số lượng phụ tùng
   */
  updatePart: async (
    repairOrderId: number,
    partDetailId: number,
    data: UpdateRepairPartRequest
  ): Promise<RepairPartResponse> => {
    const response = await apiClient.put<ApiResponse<RepairPartResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/parts/${partDetailId}`,
      data
    );
    return response.data.data;
  },

  /**
   * Xóa phụ tùng khỏi phiếu sửa chữa
   */
  deletePart: async (repairOrderId: number, partDetailId: number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/parts/${partDetailId}`
    );
  },

  // Technician Specific API
  /**
   * Danh sách phiếu sửa chữa được phân công cho Kỹ thuật viên hiện tại
   */
  getTechnicianOrders: async (): Promise<RepairOrderResponse[]> => {
    const response = await apiClient.get<ApiResponse<RepairOrderResponse[]>>(
      API_ENDPOINTS.TECHNICIAN_REPAIR_ORDERS
    );
    return response.data.data || [];
  },

  /**
   * Chi tiết phiếu sửa chữa cho Kỹ thuật viên
   */
  getTechnicianOrderDetail: async (repairOrderId: number): Promise<RepairOrderResponse> => {
    const response = await apiClient.get<ApiResponse<RepairOrderResponse>>(
      `${API_ENDPOINTS.TECHNICIAN_REPAIR_ORDERS}/${repairOrderId}`
    );
    return response.data.data;
  },

  /**
   * Kỹ thuật viên cập nhật tiến độ
   */
  updateTechnicianProgress: async (
    repairOrderId: number,
    data: UpdateRepairProgressRequest
  ): Promise<RepairProgressResponse> => {
    const response = await apiClient.patch<ApiResponse<RepairProgressResponse>>(
      `${API_ENDPOINTS.TECHNICIAN_REPAIR_ORDERS}/${repairOrderId}/progress`,
      data
    );
    return response.data.data;
  },

  /**
   * Lấy lịch sử tiến độ phiếu sửa chữa
   */
  getTechnicianProgressHistory: async (
    repairOrderId: number
  ): Promise<RepairProgressResponse[]> => {
    const response = await apiClient.get<ApiResponse<RepairProgressResponse[]>>(
      `${API_ENDPOINTS.TECHNICIAN_REPAIR_ORDERS}/${repairOrderId}/progress-history`
    );
    return response.data.data || [];
  },
};
