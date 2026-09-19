import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import {
  AssignmentResponse,
  CreateAssignmentRequest,
  RejectAssignmentRequest,
} from '@/types/repair-order.types';

export const assignmentService = {
  /**
   * Lấy danh sách phân công của một phiếu sửa chữa (ADMIN, MANAGER, FRONT_DESK)
   */
  getAssignments: async (repairOrderId: number): Promise<AssignmentResponse[]> => {
    const response = await apiClient.get<ApiResponse<AssignmentResponse[]>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/assignments`
    );
    return response.data.data || [];
  },

  /**
   * Lấy danh sách tất cả phân công đang chờ duyệt (CHO_DUYET) (ADMIN, MANAGER)
   */
  getPendingAssignments: async (): Promise<AssignmentResponse[]> => {
    const response = await apiClient.get<ApiResponse<AssignmentResponse[]>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/assignments/pending`
    );
    return response.data.data || [];
  },

  /**
   * Phân công kỹ thuật viên cho phiếu sửa chữa (ADMIN, MANAGER, FRONT_DESK)
   * - MANAGER / ADMIN: Phân công được AUTO APPROVED (DA_DUYET) ngay lập tức
   * - FRONT_DESK: Phân công ở trạng thái PENDING APPROVAL (CHO_DUYET) chờ MANAGER duyệt
   */
  createAssignment: async (
    repairOrderId: number,
    data: CreateAssignmentRequest
  ): Promise<AssignmentResponse> => {
    const response = await apiClient.post<ApiResponse<AssignmentResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/assignments`,
      data
    );
    return response.data.data;
  },

  /**
   * Quản lý duyệt phân công kỹ thuật viên (ADMIN, MANAGER)
   */
  approveAssignment: async (
    repairOrderId: number,
    assignmentId: number
  ): Promise<AssignmentResponse> => {
    const response = await apiClient.put<ApiResponse<AssignmentResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/assignments/${assignmentId}/approve`
    );
    return response.data.data;
  },

  /**
   * Quản lý từ chối phân công kỹ thuật viên (ADMIN, MANAGER)
   */
  rejectAssignment: async (
    repairOrderId: number,
    assignmentId: number,
    data?: RejectAssignmentRequest
  ): Promise<AssignmentResponse> => {
    const response = await apiClient.put<ApiResponse<AssignmentResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/assignments/${assignmentId}/reject`,
      data || {}
    );
    return response.data.data;
  },

  /**
   * Hủy/xóa phân công kỹ thuật viên (ADMIN, MANAGER)
   */
  deleteAssignment: async (
    repairOrderId: number,
    assignmentId: number
  ): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/assignments/${assignmentId}`
    );
  },
};
