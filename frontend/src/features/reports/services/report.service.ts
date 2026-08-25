import apiClient from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';
import {
  DashboardResponseData,
  RevenueReportResponseData,
  AppointmentReportResponseData,
  RepairOrderReportResponseData,
  BranchReportResponseData,
  TechnicianReportResponseData,
  InventoryReportResponseData,
  ReportFilterParams,
} from '../types/report.types';

export const reportService = {
  async getDashboard(params?: ReportFilterParams): Promise<DashboardResponseData> {
    const response = await apiClient.get<ApiResponse<DashboardResponseData>>(
      API_ENDPOINTS.REPORTS.DASHBOARD,
      { params }
    );
    return response.data.data;
  },

  async getRevenue(params?: ReportFilterParams): Promise<RevenueReportResponseData> {
    const response = await apiClient.get<ApiResponse<RevenueReportResponseData>>(
      API_ENDPOINTS.REPORTS.REVENUE,
      { params }
    );
    return response.data.data;
  },

  async getAppointments(params?: ReportFilterParams): Promise<AppointmentReportResponseData> {
    const response = await apiClient.get<ApiResponse<AppointmentReportResponseData>>(
      API_ENDPOINTS.REPORTS.APPOINTMENTS,
      { params }
    );
    return response.data.data;
  },

  async getRepairOrders(params?: ReportFilterParams): Promise<RepairOrderReportResponseData> {
    const response = await apiClient.get<ApiResponse<RepairOrderReportResponseData>>(
      API_ENDPOINTS.REPORTS.REPAIR_ORDERS,
      { params }
    );
    return response.data.data;
  },

  async getTechnicians(params?: ReportFilterParams): Promise<TechnicianReportResponseData[]> {
    const response = await apiClient.get<ApiResponse<TechnicianReportResponseData[]>>(
      API_ENDPOINTS.REPORTS.TECHNICIANS,
      { params }
    );
    return response.data.data;
  },

  async getBranches(params?: ReportFilterParams): Promise<BranchReportResponseData[]> {
    const response = await apiClient.get<ApiResponse<BranchReportResponseData[]>>(
      API_ENDPOINTS.REPORTS.BRANCHES,
      { params }
    );
    return response.data.data;
  },

  async getInventory(params?: ReportFilterParams): Promise<InventoryReportResponseData[]> {
    const response = await apiClient.get<ApiResponse<InventoryReportResponseData[]>>(
      API_ENDPOINTS.REPORTS.INVENTORY,
      { params }
    );
    return response.data.data;
  },
};
