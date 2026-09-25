import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import { ServiceItem } from '@/types/service.types';

export const serviceCatalogService = {
  async getAll(): Promise<ServiceItem[]> {
    const response = await apiClient.get<ApiResponse<ServiceItem[]>>(API_ENDPOINTS.SERVICES);
    return response.data.data || [];
  },

  async getById(id: number): Promise<ServiceItem> {
    const response = await apiClient.get<ApiResponse<ServiceItem>>(`${API_ENDPOINTS.SERVICES}/${id}`);
    return response.data.data;
  },
};
