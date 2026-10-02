import { apiClient } from '@/lib/api/client';
import { ApiResponse } from '@/types/api.types';
import {
  ServiceResponse,
  ServiceCategoryResponse,
  ServiceCategoryRequest,
  CreateServiceRequest,
  UpdateServiceRequest,
} from '@/types/service.types';

export const serviceCatalogService = {
  // Service APIs
  async getAll(onlyActive = false, categoryId?: number): Promise<ServiceResponse[]> {
    return this.getAllServices(onlyActive, categoryId);
  },

  async getAllServices(onlyActive = false, categoryId?: number): Promise<ServiceResponse[]> {
    const params: Record<string, any> = { onlyActive };
    if (categoryId) params.categoryId = categoryId;
    const res = await apiClient.get<ApiResponse<ServiceResponse[]>>('/services', { params });
    return res.data.data;
  },

  async getServiceById(id: number): Promise<ServiceResponse> {
    const res = await apiClient.get<ApiResponse<ServiceResponse>>(`/services/${id}`);
    return res.data.data;
  },

  async createService(data: CreateServiceRequest): Promise<ServiceResponse> {
    const res = await apiClient.post<ApiResponse<ServiceResponse>>('/services', data);
    return res.data.data;
  },

  async updateService(id: number, data: UpdateServiceRequest): Promise<ServiceResponse> {
    const res = await apiClient.put<ApiResponse<ServiceResponse>>(`/services/${id}`, data);
    return res.data.data;
  },

  async updateServiceStatus(id: number, trangThai: boolean): Promise<ServiceResponse> {
    const res = await apiClient.patch<ApiResponse<ServiceResponse>>(`/services/${id}/status`, { trangThai });
    return res.data.data;
  },

  async deleteService(id: number): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`/services/${id}`);
  },

  // Category APIs
  async getAllCategories(onlyActive = false): Promise<ServiceCategoryResponse[]> {
    const res = await apiClient.get<ApiResponse<ServiceCategoryResponse[]>>('/service-categories', {
      params: { onlyActive },
    });
    return res.data.data;
  },

  async getCategoryById(id: number): Promise<ServiceCategoryResponse> {
    const res = await apiClient.get<ApiResponse<ServiceCategoryResponse>>(`/service-categories/${id}`);
    return res.data.data;
  },

  async createCategory(data: ServiceCategoryRequest): Promise<ServiceCategoryResponse> {
    const res = await apiClient.post<ApiResponse<ServiceCategoryResponse>>('/service-categories', data);
    return res.data.data;
  },

  async updateCategory(id: number, data: ServiceCategoryRequest): Promise<ServiceCategoryResponse> {
    const res = await apiClient.put<ApiResponse<ServiceCategoryResponse>>(`/service-categories/${id}`, data);
    return res.data.data;
  },

  async updateCategoryStatus(id: number, trangThai: boolean): Promise<ServiceCategoryResponse> {
    const res = await apiClient.patch<ApiResponse<ServiceCategoryResponse>>(`/service-categories/${id}/status`, {
      trangThai,
    });
    return res.data.data;
  },

  async deleteCategory(id: number): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`/service-categories/${id}`);
  },
};

