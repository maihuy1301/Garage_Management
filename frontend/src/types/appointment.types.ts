export type AppointmentStatus =
  | 'CHO_XAC_NHAN'
  | 'DA_XAC_NHAN'
  | 'DA_TIEP_NHAN'
  | 'DANG_XU_LY'
  | 'HOAN_TAT'
  | 'KHONG_DEN'
  | 'HUY'
  | 'DA_HUY';

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
  DANG_XU_LY: 'Đang xử lý',
  HOAN_TAT: 'Hoàn tất',
  KHONG_DEN: 'Không đến',
  HUY: 'Đã hủy',
  DA_HUY: 'Đã hủy',
};

export const AppointmentStatusTone: Record<string, 'warning' | 'success' | 'primary' | 'danger' | 'info' | 'gray'> = {
  CHO_XAC_NHAN: 'warning',
  DA_XAC_NHAN: 'info',
  DA_TIEP_NHAN: 'primary',
  DANG_XU_LY: 'primary',
  HOAN_TAT: 'success',
  HUY: 'danger',
  KHONG_DEN: 'gray',
  DA_HUY: 'danger',
};

export const APPOINTMENT_STATUSES: { value: AppointmentStatus | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'CHO_XAC_NHAN', label: 'Chờ xác nhận' },
  { value: 'DA_XAC_NHAN', label: 'Đã xác nhận' },
  { value: 'DA_TIEP_NHAN', label: 'Đã tiếp nhận' },
  { value: 'DANG_XU_LY', label: 'Đang xử lý' },
  { value: 'HOAN_TAT', label: 'Hoàn tất' },
  { value: 'HUY', label: 'Đã hủy' },
  { value: 'KHONG_DEN', label: 'Không đến' },
  { value: 'DA_HUY', label: 'Đã hủy (legacy)' },
];

export const isAppointmentStatus = (status: string): status is AppointmentStatus =>
  status in AppointmentStatusLabels;

export const getAppointmentStatusLabel = (status: string): string =>
  isAppointmentStatus(status) ? AppointmentStatusLabels[status] : status.replace(/_/g, ' ');

export const getAppointmentStatusTone = (
  status: string,
): 'warning' | 'success' | 'primary' | 'danger' | 'info' | 'gray' =>
  isAppointmentStatus(status) ? AppointmentStatusTone[status] : 'primary';

export const canCancelAppointment = (status: string): boolean =>
  status === 'CHO_XAC_NHAN' || status === 'DA_XAC_NHAN';
