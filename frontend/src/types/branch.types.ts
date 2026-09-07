export interface BranchResponse {
  maChiNhanh: number;
  tenChiNhanh: string;
  diaChi?: string;
  soDienThoai?: string;
  email?: string;
  trangThai?: boolean;
  ngayTao?: string;
}

export type BranchItem = BranchResponse;
