import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import {
  BrandResponse,
  CreateBrandRequest,
  ModelResponse,
  CreateModelRequest,
} from '@/types/brand-model.types';

export const brandModelService = {
  /**
   * Lấy danh sách tất cả các Hãng xe đang hoạt động (cho dropdown & quản lý)
   */
  async getActiveBrands(): Promise<BrandResponse[]> {
    const response = await apiClient.get<ApiResponse<BrandResponse[]>>(API_ENDPOINTS.BRANDS);
    return response.data.data || [];
  },

  /**
   * Lấy chi tiết một hãng xe
   */
  async getBrandById(brandId: number): Promise<BrandResponse> {
    const response = await apiClient.get<ApiResponse<BrandResponse>>(`${API_ENDPOINTS.BRANDS}/${brandId}`);
    return response.data.data;
  },

  /**
   * Tạo Hãng xe mới
   */
  async createBrand(data: CreateBrandRequest): Promise<BrandResponse> {
    const response = await apiClient.post<ApiResponse<BrandResponse>>(API_ENDPOINTS.BRANDS, data);
    return response.data.data;
  },

  /**
   * Lấy danh sách Model theo Hãng xe (Cascading Dropdown)
   */
  async getModelsByBrand(brandId: number): Promise<ModelResponse[]> {
    const response = await apiClient.get<ApiResponse<ModelResponse[]>>(
      `${API_ENDPOINTS.BRANDS}/${brandId}/models`
    );
    return response.data.data || [];
  },

  /**
   * Tạo Model xe mới thuộc Hãng xe
   */
  async createModel(brandId: number, data: CreateModelRequest): Promise<ModelResponse> {
    const response = await apiClient.post<ApiResponse<ModelResponse>>(
      `${API_ENDPOINTS.BRANDS}/${brandId}/models`,
      data
    );
    return response.data.data;
  },
};
