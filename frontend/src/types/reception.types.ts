export interface ReceptionResponse {
  maTiepNhan: number;
  maDatLich?: number;
  maXe: number;
  bienSoXe: string;
  hangXe?: string;
  modelXe?: string;
  maKhachHang: number;
  tenKhachHang: string;
  soDienThoaiKhachHang: string;
  maChiNhanh: number;
  tenChiNhanh: string;
  maNhanVienTiepNhan: number;
  tenNhanVienTiepNhan: string;
  thoiGianTiepNhan: string;
  soKm?: number;
  tinhTrangNgoaiThat?: string;
  yeuCauKhachHang?: string;
  trangThai: ReceptionStatus | string;
}

export type ReceptionStatus =
  | 'DA_TIEP_NHAN'
  | 'TIEP_NHAN'
  | 'CHO_XU_LY'
  | 'DANG_XU_LY'
  | 'CHO_SUA_CHUA'
  | 'DANG_SUA_CHUA'
  | 'DANG_SUA'
  | 'HOAN_TAT'
  | 'HUY';

export interface CheckInRequest {
  soKm?: number;
  tinhTrangNgoaiThat?: string;
  yeuCauKhachHang?: string;
}

export const ReceptionStatusLabels: Record<string, string> = {
  DA_TIEP_NHAN: 'Đã tiếp nhận',
  TIEP_NHAN: 'Đã tiếp nhận',
  CHO_XU_LY: 'Chờ xử lý',
  DANG_XU_LY: 'Đang xử lý',
  CHO_SUA_CHUA: 'Chờ sửa chữa',
  DANG_SUA_CHUA: 'Đang sửa chữa',
  DANG_SUA: 'Đang sửa chữa',
  HOAN_TAT: 'Hoàn tất',
  HUY: 'Đã hủy',
};

export const ReceptionStatusTone: Record<
  string,
  'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
  DA_TIEP_NHAN: 'primary',
  TIEP_NHAN: 'primary',
  CHO_XU_LY: 'warning',
  DANG_XU_LY: 'warning',
  CHO_SUA_CHUA: 'warning',
  DANG_SUA_CHUA: 'primary',
  DANG_SUA: 'primary',
  HOAN_TAT: 'success',
  HUY: 'danger',
};
