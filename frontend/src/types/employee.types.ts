export interface EmployeeResponse {
  maNhanVien: number;
  maNhanVienCode: string;
  chucVu?: string | null;
  ngayVaoLam?: string | null;
  trangThai: boolean;

  // User details
  maNguoiDung?: number | null;
  tenDangNhap?: string | null;
  hoTen?: string | null;
  email?: string | null;
  soDienThoai?: string | null;
  roles?: string[];

  // Branch details
  maChiNhanh?: number | null;
  maChiNhanhCode?: string | null;
  tenChiNhanh?: string | null;
}

export interface CreateEmployeeRequest {
  maNhanVienCode: string;
  maNguoiDung: number;
  maChiNhanh: number;
  chucVu?: string;
  ngayVaoLam?: string;
}

export interface UpdateEmployeeRequest {
  chucVu?: string;
  ngayVaoLam?: string;
  maChiNhanh?: number;
}

export interface UpdateEmployeeStatusRequest {
  trangThai: boolean;
}
