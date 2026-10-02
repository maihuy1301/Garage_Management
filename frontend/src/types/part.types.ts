export interface PartResponse {
  maPhuTung: number;
  maPhuTungCode: string;
  tenPhuTung: string;
  donViTinh?: string;
  giaNhap?: number;
  giaBan?: number;
  trangThai: boolean;
}

export interface CreatePartRequest {
  maPhuTungCode: string;
  tenPhuTung: string;
  donViTinh?: string;
  giaNhap?: number;
  giaBan?: number;
}

export interface UpdatePartRequest {
  tenPhuTung: string;
  donViTinh?: string;
  giaNhap?: number;
  giaBan?: number;
}

export interface InventoryResponse {
  maChiNhanh?: number;
  tenChiNhanh?: string;
  maPhuTung?: number;
  maPhuTungCode?: string;
  tenPhuTung?: string;
  donViTinh?: string;
  giaBan?: number;
  soLuongTon: number;
  soLuongToiThieu: number;
}

export interface StockImportRequest {
  maPhuTung: number;
  soLuong: number;
  branchId?: number;
  ghiChu?: string;
}

export interface StockTransactionResponse {
  maGiaoDich: number;
  maChiNhanh?: number;
  tenChiNhanh?: string;
  maPhuTung?: number;
  maPhuTungCode?: string;
  tenPhuTung?: string;
  donViTinh?: string;
  loaiGiaoDich: string;
  soLuong: number;
  maPhieuSuaChua?: number;
  ghiChu?: string;
  thoiGian?: string;
}
