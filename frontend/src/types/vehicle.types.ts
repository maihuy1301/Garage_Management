export interface VehicleItem {
  maXe: number;
  maKhachHang?: number;
  tenChuXe?: string;
  bienSo: string;
  maHangXe?: number;
  tenHangXe?: string;
  maModel?: number;
  tenModel?: string;
  hangXe?: string; // Tương thích ngược nếu có
  model?: string;  // Tương thích ngược nếu có
  namSanXuat?: number;
  mauXe?: string;
  soVIN?: string;
  soKmHienTai?: number;
  trangThai?: boolean;
  ngayTao?: string;
}

export type VehicleResponse = VehicleItem;

export interface CreateVehicleRequest {
  maKhachHang?: number;
  bienSo: string;
  maHangXe: number;
  maModel: number;
  namSanXuat?: number;
  mauXe?: string;
  soVIN?: string;
  soKmHienTai?: number;
}

export interface UpdateVehicleRequest {
  maHangXe?: number;
  maModel?: number;
  namSanXuat?: number;
  mauXe?: string;
  soVIN?: string;
  soKmHienTai?: number;
}
