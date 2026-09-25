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

export const RepairOrderStatusLabels: Record<RepairOrderStatus | string, string> = {
  CHO_XU_LY: 'Chờ xử lý',
  DA_PHAN_CONG: 'Đã phân công',
  DANG_SUA: 'Đang sửa chữa',
  CHO_KH_DUYET: 'Chờ KH duyệt',
  TAM_DUNG: 'Tạm dừng',
  HOAN_TAT: 'Hoàn tất',
  HUY: 'Đã hủy',
};

export const RepairOrderStatusTone: Record<
  RepairOrderStatus | string,
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

export interface CreateDirectRepairOrderRequest {
  maKhachHang: number;
  maXe: number;
  maPhieuCha?: number;
  serviceIds: number[];
  thoiGianBatDau?: string;
  ghiChu?: string;
  soKm?: number;
  yeuCauKhachHang?: string;
}

export interface UpdateRepairOrderRequest {
  thoiGianBatDau?: string;
  thoiGianHoanTat?: string;
  ghiChu?: string;
}

export interface UpdateRepairOrderStatusRequest {
  trangThai: RepairOrderStatus;
  ghiChu?: string;
}

// Assignment Types
export type AssignmentStatus = 'CHO_DUYET' | 'DA_DUYET' | 'TU_CHOI' | 'DA_HUY';

export const AssignmentStatusLabels: Record<AssignmentStatus | string, string> = {
  CHO_DUYET: 'Chờ duyệt',
  DA_DUYET: 'Đã duyệt',
  TU_CHOI: 'Từ chối',
  DA_HUY: 'Đã hủy',
};

export const AssignmentStatusTone: Record<
  AssignmentStatus | string,
  'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
  CHO_DUYET: 'warning',
  DA_DUYET: 'success',
  TU_CHOI: 'danger',
  DA_HUY: 'default',
};

export interface AssignmentResponse {
  maPhanCong: number;
  maPhieuSuaChua: number;
  maNguoiPhanCong?: number;
  tenNguoiPhanCong?: string;
  maNguoiDuyet?: number;
  tenNguoiDuyet?: string;
  maNhanVien: number;
  tenNhanVien?: string;
  maChiNhanh?: number;
  tenChiNhanh?: string;
  ghiChu?: string;
  thoiGianTao?: string;
  thoiGianDuyet?: string;
  trangThai: AssignmentStatus;
}

export interface CreateAssignmentRequest {
  technicianId: number;
  ghiChu?: string;
}

export interface RejectAssignmentRequest {
  ghiChu?: string;
}

// Repair Items (Services) & Parts
export interface RepairItemResponse {
  maChiTiet: number;
  maPhieuSuaChua: number;
  maDichVu: number;
  tenDichVu: string;
  maLoaiDichVu?: number;
  tenLoaiDichVu?: string;
  soLuong: number;
  donGia: number;
  thanhTien: number;
  trangThai?: string;
}

export interface CreateRepairItemRequest {
  maDichVu: number;
  soLuong: number;
  donGia?: number;
}

export interface UpdateRepairItemRequest {
  soLuong?: number;
  donGia?: number;
  trangThai?: string;
}

export interface RepairPartResponse {
  maChiTiet: number;
  maPhieuSuaChua: number;
  maPhuTung: number;
  maDichVuChiTiet?: number;
  tenDichVuChiTiet?: string;
  maPhuTungCode?: string;
  tenPhuTung: string;
  donViTinh?: string;
  soLuong: number;
  donGia: number;
  thanhTien: number;
}

export interface CreateRepairPartRequest {
  maPhuTung: number;
  soLuong: number;
  maDichVuChiTiet?: number;
}

export interface UpdateRepairPartRequest {
  soLuong: number;
}

// Progress
export interface RepairProgressResponse {
  maLichSu?: number;
  maPhieuSuaChua?: number;
  trangThaiCu?: string;
  trangThaiMoi: string;
  ghiChu?: string;
  thoiGianCapNhat?: string;
  nguoiCapNhat?: string;
}

export interface UpdateRepairProgressRequest {
  trangThaiMoi: string;
  ghiChu?: string;
}
