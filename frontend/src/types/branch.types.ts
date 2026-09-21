export interface BranchResponse {
  maChiNhanh: number;
  tenChiNhanh: string;
  diaChi?: string;
  soDienThoai?: string;
  email?: string;
  trangThai?: boolean;
  ngayTao?: string;
}

export interface CreateBranchRequest {
  tenChiNhanh: string;
  diaChi: string;
  soDienThoai?: string;
  email?: string;
  trangThai?: boolean;
}

export interface UpdateBranchRequest {
  tenChiNhanh: string;
  diaChi: string;
  soDienThoai?: string;
  email?: string;
  trangThai?: boolean;
}

export interface UpdateBranchStatusRequest {
  trangThai: boolean;
}

export type BranchItem = BranchResponse;
