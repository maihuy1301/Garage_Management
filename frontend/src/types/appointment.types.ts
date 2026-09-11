export type AppointmentStatus =
  | 'CHO_XAC_NHAN'
  | 'DA_XAC_NHAN'
  | 'DA_TIEP_NHAN'
  | 'HOAN_TAT'
  | 'HUY'
  | 'KHONG_DEN';

export interface AppointmentResponse {
  maDatLich: number;
  maKhachHang?: number;
  tenKhachHang?: string;
  soDienThoaiKhachHang?: string;
  maNhanVienXacNhan?: number;
  maXe?: number;
  bienSoXe?: string;
  hangXe?: string;
  modelXe?: string;
  maChiNhanh?: number;
  tenChiNhanh?: string;
  thoiGianHen: string;
  trangThai: AppointmentStatus | string;
  ghiChu?: string;
  ngayDat?: string;
}

// Alias for backward compatibility
export type AppointmentItem = AppointmentResponse;

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

export const AppointmentStatusLabels: Record<string, string> = {
  CHO_XAC_NHAN: 'Chờ xác nhận',
  DA_XAC_NHAN: 'Đã xác nhận',
  DA_TIEP_NHAN: 'Đã tiếp nhận',
  HOAN_TAT: 'Hoàn tất',
  HUY: 'Đã hủy',
  KHONG_DEN: 'Không đến',
};

export const AppointmentStatusTone: Record<string, 'warning' | 'success' | 'primary' | 'danger' | 'info' | 'gray'> = {
  CHO_XAC_NHAN: 'warning',
  DA_XAC_NHAN: 'info',
  DA_TIEP_NHAN: 'primary',
  HOAN_TAT: 'success',
  HUY: 'danger',
  KHONG_DEN: 'gray',
};

export const APPOINTMENT_STATUSES: { value: AppointmentStatus | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'CHO_XAC_NHAN', label: 'Chờ xác nhận' },
  { value: 'DA_XAC_NHAN', label: 'Đã xác nhận' },
  { value: 'DA_TIEP_NHAN', label: 'Đã tiếp nhận' },
  { value: 'HOAN_TAT', label: 'Hoàn tất' },
  { value: 'HUY', label: 'Đã hủy' },
  { value: 'KHONG_DEN', label: 'Không đến' },
];
