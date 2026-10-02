export interface ServiceCategoryResponse {
  maLoaiDichVu: number;
  tenLoai: string;
  moTa?: string;
  trangThai: boolean;
  soLuongDichVu?: number;
}

export interface ServiceCategoryRequest {
  tenLoai: string;
  moTa?: string;
  trangThai?: boolean;
}

export interface ServicePartResponse {
  maPhuTung: number;
  tenPhuTung: string;
  soLuong: number;
  donGia: number;
  thanhTien: number;
  donViTinh?: string;
}

export interface ServicePartItemRequest {
  maPhuTung: number;
  soLuong: number;
}

export interface ServiceResponse {
  maDichVu: number;
  maLoaiDichVu?: number;
  tenLoaiDichVu?: string;
  tenDichVu: string;
  moTa?: string;
  donGia: number;
  thoiGianDuKien?: number;
  trangThai: boolean;
  parts?: ServicePartResponse[];
  tienPhuTungDuKien?: number;
  tongGiaDuKien?: number;
  defaultParts?: ServicePartResponse[];
  totalPartPrice?: number;
  estimatedTotal?: number;
}

export type ServiceItem = ServiceResponse;

export interface CreateServiceRequest {
  tenDichVu: string;
  maLoaiDichVu: number;
  moTa?: string;
  donGia: number;
  thoiGianDuKien?: number;
  defaultParts?: ServicePartItemRequest[];
}

export interface UpdateServiceRequest {
  tenDichVu: string;
  maLoaiDichVu: number;
  moTa?: string;
  donGia: number;
  thoiGianDuKien?: number;
  defaultParts?: ServicePartItemRequest[];
}
