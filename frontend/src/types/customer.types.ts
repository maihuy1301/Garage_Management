export interface CustomerResponse {
  maKhachHang: number;
  maKhachHangCode: string;
  diaChi?: string | null;
  ngaySinh?: string | null;

  // User fields
  maNguoiDung?: number | null;
  tenDangNhap?: string | null;
  hoTen?: string | null;
  email?: string | null;
  soDienThoai?: string | null;
  anhDaiDien?: string | null;
  trangThai?: boolean | null;
  ngayTao?: string | null;
  roles?: string[];
}

export interface CreateCustomerRequest {
  maKhachHangCode: string;
  maNguoiDung: number;
  diaChi?: string;
  ngaySinh?: string;
}

export interface UpdateCustomerRequest {
  hoTen?: string;
  email?: string;
  soDienThoai?: string;
  diaChi?: string;
  ngaySinh?: string;
  anhDaiDien?: string;
}

export interface UpdateCustomerStatusRequest {
  trangThai: boolean;
}
