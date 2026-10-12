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
  maNhanVien?: number;
  hoTen?: string;
  soPhieuPhanCong?: number;
  soPhieuDangSua?: number;
  soPhieuHoanTat?: number;
  tenChiNhanh?: string;
  // Fallback / legacy english property names
  technicianId?: number;
  technicianName?: string;
  branchName?: string;
  totalAssigned?: number;
  inProgress?: number;
  completed?: number;
}

export interface InventoryReportResponseData {
  maPhuTung?: number;
  maPhuTungCode?: string;
  tenPhuTung?: string;
  donGia?: number;
  soLuongTon?: number;
  soLuongToiThieu?: number;
  giaTriTonKho?: number;
  sapHetHang?: boolean;
  tenChiNhanh?: string;
  // Fallback / legacy english property names
  partId?: number;
  partCode?: string;
  partName?: string;
  branchName?: string;
  stockQuantity?: number;
  minQuantity?: number;
  warningLowStock?: boolean;
}

export interface ServiceReportResponseData {
  maDichVu: number;
  tenDichVu: string;
  soLanSuDung: number;
  doanhThu: number;
}

export interface PartReportResponseData {
  maPhuTung: number;
  maPhuTungCode?: string;
  tenPhuTung: string;
  soLuongSuDung: number;
  doanhThu: number;
}

export interface ReportFilterParams {
  branchCode?: string;
  from?: string;
  to?: string;
  limit?: number;
}
