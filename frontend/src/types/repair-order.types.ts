export interface RepairOrderResponse {
  maPhieuSuaChua: number;
  maPhieuCha?: number;
  maTiepNhan: number;
  maDatLich?: number;
  maXe: number;
  bienSoXe: string;
  maHangXe?: number;
  tenHangXe?: string;
  maModel?: number;
  tenModel?: string;
  hangXe?: string;
  modelXe?: string;
  maKhachHang: number;
  tenKhachHang: string;
  soDienThoaiKhachHang: string;
  maChiNhanh: number;
  tenChiNhanh: string;
  thoiGianBatDau: string;
  thoiGianHoanTat?: string;
  trangThai: RepairOrderStatus;
  ghiChu?: string;
}

export type RepairOrderStatus =
  | 'CHO_XU_LY'
  | 'DA_PHAN_CONG'
  | 'DANG_SUA'
  | 'CHO_KH_DUYET'
  | 'TAM_DUNG'
  | 'HOAN_TAT'
  | 'HUY';

export const RepairOrderStatusLabels: Record<string, string> = {
  CHO_XU_LY: 'Chờ xử lý',
  DA_PHAN_CONG: 'Đã phân công',
  DANG_SUA: 'Đang sửa chữa',
  CHO_KH_DUYET: 'Chờ KH duyệt',
  TAM_DUNG: 'Tạm dừng',
  HOAN_TAT: 'Hoàn tất',
  HUY: 'Đã hủy',
};

export const RepairOrderStatusTone: Record<
  string,
  'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
  CHO_XU_LY: 'warning',
  DA_PHAN_CONG: 'info',
  DANG_SUA: 'primary',
  CHO_KH_DUYET: 'warning',
  TAM_DUNG: 'danger',
  HOAN_TAT: 'success',
  HUY: 'danger',
};

export interface CreateRepairOrderRequest {
  maTiepNhan: number;
  maPhieuCha?: number;
  thoiGianBatDau?: string;
  ghiChu?: string;
}
