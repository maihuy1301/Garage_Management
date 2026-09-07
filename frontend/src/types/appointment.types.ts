export type AppointmentStatus =
  | 'CHO_XAC_NHAN'
  | 'DA_XAC_NHAN'
  | 'DA_TIEP_NHAN'
  | 'DA_HUY';

export interface AppointmentItem {
  maDatLich: number;
  maKhachHang: number;
  tenKhachHang?: string;
  soDienThoaiKhachHang?: string;
  maXe: number;
  bienSoXe?: string;
  hangXe?: string;
  modelXe?: string;
  maChiNhanh: number;
  tenChiNhanh?: string;
  thoiGianHen: string;
  trangThai: AppointmentStatus | string;
  ghiChu?: string;
  ngayDat?: string;
}

export interface CreateAppointmentRequest {
  maKhachHang?: number;
  maXe: number;
  maChiNhanh: number;
  thoiGianHen: string;
  ghiChu?: string;
}

export interface AppointmentFilters {
  search: string;
  status: AppointmentStatus | 'ALL';
  branchId: number | 'ALL';
  fromDate: string;
  toDate: string;
}

export const AppointmentStatusLabels: Record<AppointmentStatus, string> = {
  CHO_XAC_NHAN: 'Chờ xác nhận',
  DA_XAC_NHAN: 'Đã xác nhận',
  DA_TIEP_NHAN: 'Đã tiếp nhận',
  DA_HUY: 'Đã hủy',
};

export const AppointmentStatusTone: Record<AppointmentStatus, 'warning' | 'success' | 'primary' | 'danger'> = {
  CHO_XAC_NHAN: 'warning',
  DA_XAC_NHAN: 'success',
  DA_TIEP_NHAN: 'primary',
  DA_HUY: 'danger',
};

export const APPOINTMENT_STATUSES: AppointmentStatus[] = [
  'CHO_XAC_NHAN',
  'DA_XAC_NHAN',
  'DA_TIEP_NHAN',
  'DA_HUY',
];
