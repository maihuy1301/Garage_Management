export interface VehicleItem {
  maXe: number;
  maKhachHang?: number;
  maKhachHangCode?: string;
  tenChuXe?: string;
  bienSo: string;
  hangXe?: string;
  model?: string;
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
  hangXe?: string;
  model?: string;
  namSanXuat?: number;
  mauXe?: string;
  soVIN?: string;
  soKmHienTai?: number;
}

export interface UpdateVehicleRequest {
  hangXe?: string;
  model?: string;
  namSanXuat?: number;
  mauXe?: string;
  soVIN?: string;
  soKmHienTai?: number;
}
