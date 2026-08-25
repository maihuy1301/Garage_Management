export interface BranchResponse {
  maChiNhanh: number;
  maChiNhanhCode: string;
  tenChiNhanh: string;
  diaChi?: string;
  soDienThoai?: string;
  email?: string;
  trangThai?: boolean;
  ngayTao?: string;
}

export type BranchItem = BranchResponse;
