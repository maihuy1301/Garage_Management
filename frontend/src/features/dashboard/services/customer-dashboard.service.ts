import apiClient from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import { VehicleItem } from '@/types/vehicle.types';
import { AppointmentItem } from '@/types/appointment.types';

export const customerDashboardService = {
  async getMyVehicles(): Promise<VehicleItem[]> {
    const response = await apiClient.get<ApiResponse<VehicleItem[]>>(API_ENDPOINTS.VEHICLES);
    return response.data.data || [];
  },

  async getMyAppointments(): Promise<AppointmentItem[]> {
    const response = await apiClient.get<ApiResponse<AppointmentItem[]>>(API_ENDPOINTS.APPOINTMENTS);
    return response.data.data || [];
  },
};
