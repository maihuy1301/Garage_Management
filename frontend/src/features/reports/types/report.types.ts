export interface DashboardResponseData {
  totalRevenue: number;
  totalInvoices: number;
  totalPayments: number;
  totalAppointments: number;
  totalRepairOrders: number;
  repairOrdersInProgress: number;
  repairOrdersCompleted: number;
  totalCustomers: number;
  totalVehicles: number;
}

export interface RevenuePeriodResponseData {
  period: string;
  revenue: number;
  invoiceCount: number;
}

export interface RevenueReportResponseData {
  totalRevenue: number;
  totalInvoices: number;
  totalPaid: number;
  totalUnpaid: number;
  periods: RevenuePeriodResponseData[];
}

export interface AppointmentReportResponseData {
  total: number;
  choXacNhan: number;
  daXacNhan: number;
  daTiepNhan: number;
  daHuy: number;
}

export interface RepairOrderReportResponseData {
  total: number;
  choXuLy: number;
  daPhanCong: number;
  dangSua: number;
  choKhachDuyet: number;
  tamDung: number;
  hoanTat: number;
  huy: number;
}

export interface BranchReportResponseData {
  maChiNhanh: number;
  tenChiNhanh: string;
  doanhThu: number;
  soLichHen: number;
  soPhieuSuaChua: number;
  soPhieuHoanTat: number;
}

export interface TechnicianReportResponseData {
  technicianId: number;
  technicianName: string;
  branchName: string;
  totalAssigned: number;
  inProgress: number;
  completed: number;
}

export interface InventoryReportResponseData {
  partId: number;
  partCode: string;
  partName: string;
  branchName: string;
  stockQuantity: number;
  minQuantity: number;
  warningLowStock: boolean;
}

export interface ReportFilterParams {
  branchCode?: string;
  from?: string;
  to?: string;
  limit?: number;
}
