import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import { AppointmentResponse, CreateAppointmentRequest } from '@/types/appointment.types';

export const appointmentService = {
  /**
   * Lấy danh sách lịch hẹn (Backend tự phân quyền theo ADMIN, MANAGER, FRONT_DESK, CUSTOMER)
   */
  async getAppointments(): Promise<AppointmentResponse[]> {
    const response = await apiClient.get<ApiResponse<AppointmentResponse[]>>(API_ENDPOINTS.APPOINTMENTS);
    return response.data.data || [];
  },

  /**
   * Lấy chi tiết lịch hẹn theo ID
   */
  async getAppointmentById(id: number): Promise<AppointmentResponse> {
    const response = await apiClient.get<ApiResponse<AppointmentResponse>>(`${API_ENDPOINTS.APPOINTMENTS}/${id}`);
    return response.data.data;
  },

  /**
   * Đặt lịch hẹn mới (Hỗ trợ ADMIN và CUSTOMER)
   */
  async createAppointment(data: CreateAppointmentRequest): Promise<AppointmentResponse> {
    const response = await apiClient.post<ApiResponse<AppointmentResponse>>(API_ENDPOINTS.APPOINTMENTS, data);
    return response.data.data;
  },

  /**
   * Hủy lịch hẹn (Chỉ được hủy khi trạng thái CHO_XAC_NHAN hoặc DA_XAC_NHAN)
   */
  async cancelAppointment(id: number): Promise<AppointmentResponse> {
    const response = await apiClient.patch<ApiResponse<AppointmentResponse>>(
      `${API_ENDPOINTS.APPOINTMENTS}/${id}/cancel`
    );
    return response.data.data;
  },

  /**
   * Xác nhận lịch hẹn (Chuyển trạng thái sang DA_XAC_NHAN - Dành cho ADMIN, MANAGER, FRONT_DESK)
   */
  async confirmAppointment(id: number): Promise<AppointmentResponse> {
    const response = await apiClient.patch<ApiResponse<AppointmentResponse>>(
      `${API_ENDPOINTS.APPOINTMENTS}/${id}/confirm`
    );
    return response.data.data;
  },

  /**
   * Tiếp nhận xe từ lịch hẹn (Chuyển trạng thái sang DA_TIEP_NHAN - Dành cho ADMIN, MANAGER, FRONT_DESK)
   */
  async receiveAppointment(id: number): Promise<AppointmentResponse> {
    const response = await apiClient.patch<ApiResponse<AppointmentResponse>>(
      `${API_ENDPOINTS.APPOINTMENTS}/${id}/receive`
    );
    return response.data.data;
  },

  /**
   * Cập nhật trạng thái lịch hẹn
   */
  async updateStatus(id: number, status: string): Promise<AppointmentResponse> {
    const response = await apiClient.patch<ApiResponse<AppointmentResponse>>(
      `${API_ENDPOINTS.APPOINTMENTS}/${id}/status`,
      { trangThai: status }
    );
    return response.data.data;
  },
};
