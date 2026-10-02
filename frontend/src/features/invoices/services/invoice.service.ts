import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import { InvoiceResponse, CreateInvoiceRequest } from '@/types/invoice.types';

export const invoiceService = {
  /**
   * Lấy danh sách hóa đơn (có thể lọc theo chi nhánh)
   */
  getAll: async (branchId?: number): Promise<InvoiceResponse[]> => {
    const params = branchId ? { branchId } : {};
    const response = await apiClient.get<ApiResponse<InvoiceResponse[]>>(
      API_ENDPOINTS.INVOICES,
      { params }
    );
    return response.data.data || [];
  },

  /**
   * Lấy chi tiết hóa đơn theo ID
   */
  getById: async (invoiceId: number): Promise<InvoiceResponse> => {
    const response = await apiClient.get<ApiResponse<InvoiceResponse>>(
      `${API_ENDPOINTS.INVOICES}/${invoiceId}`
    );
    return response.data.data;
  },

  /**
   * Lấy hóa đơn theo phiếu sửa chữa (kể cả phiếu con phát sinh)
   */
  getByRepairOrder: async (repairOrderId: number): Promise<InvoiceResponse> => {
    const response = await apiClient.get<ApiResponse<InvoiceResponse>>(
      `${API_ENDPOINTS.REPAIR_ORDERS}/${repairOrderId}/invoice`
    );
    return response.data.data;
  },

  /**
   * Xuất hóa đơn mới từ lệnh sửa chữa
   * POST /api/invoices/from-repair-order/{repairOrderId}
   */
  createFromRepairOrder: async (
    repairOrderId: number,
    data?: CreateInvoiceRequest
  ): Promise<InvoiceResponse> => {
    const response = await apiClient.post<ApiResponse<InvoiceResponse>>(
      `${API_ENDPOINTS.INVOICES}/from-repair-order/${repairOrderId}`,
      data || {}
    );
    return response.data.data;
  },
};
