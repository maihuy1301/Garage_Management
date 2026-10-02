import { apiClient } from '@/lib/api/client';
import { ApiResponse } from '@/types/api.types';
import {
  PartResponse,
  CreatePartRequest,
  UpdatePartRequest,
  InventoryResponse,
  StockImportRequest,
  StockTransactionResponse,
} from '@/types/part.types';

export const inventoryService = {
  // Part Catalog APIs
  async getAll(onlyActive = false): Promise<PartResponse[]> {
    return this.getAllParts(onlyActive);
  },

  async getAllParts(onlyActive = false): Promise<PartResponse[]> {
    const res = await apiClient.get<ApiResponse<PartResponse[]>>('/parts', {
      params: { onlyActive },
    });
    return res.data.data;
  },

  async getPartById(id: number): Promise<PartResponse> {
    const res = await apiClient.get<ApiResponse<PartResponse>>(`/parts/${id}`);
    return res.data.data;
  },

  async createPart(data: CreatePartRequest): Promise<PartResponse> {
    const res = await apiClient.post<ApiResponse<PartResponse>>('/parts', data);
    return res.data.data;
  },

  async updatePart(id: number, data: UpdatePartRequest): Promise<PartResponse> {
    const res = await apiClient.put<ApiResponse<PartResponse>>(`/parts/${id}`, data);
    return res.data.data;
  },

  async updatePartStatus(id: number, trangThai: boolean): Promise<PartResponse> {
    const res = await apiClient.patch<ApiResponse<PartResponse>>(`/parts/${id}/status`, { trangThai });
    return res.data.data;
  },

  async deletePart(id: number): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`/parts/${id}`);
  },

  // Branch Inventory APIs
  async getBranchInventory(branchId?: number): Promise<InventoryResponse[]> {
    const params: Record<string, any> = {};
    if (branchId) params.branchId = branchId;
    const res = await apiClient.get<ApiResponse<InventoryResponse[]>>('/inventory', { params });
    return res.data.data;
  },

  async getInventoryDetail(partId: number, branchId?: number): Promise<InventoryResponse> {
    const params: Record<string, any> = {};
    if (branchId) params.branchId = branchId;
    const res = await apiClient.get<ApiResponse<InventoryResponse>>(`/inventory/${partId}`, { params });
    return res.data.data;
  },

  // Stock Import API (Manager & Admin)
  async importStock(data: StockImportRequest): Promise<InventoryResponse> {
    const res = await apiClient.post<ApiResponse<InventoryResponse>>('/inventory/import', data);
    return res.data.data;
  },

  // Transactions History
  async getStockTransactions(branchId?: number): Promise<StockTransactionResponse[]> {
    const params: Record<string, any> = {};
    if (branchId) params.branchId = branchId;
    const res = await apiClient.get<ApiResponse<StockTransactionResponse[]>>('/inventory/transactions', { params });
    return res.data.data;
  },
};
