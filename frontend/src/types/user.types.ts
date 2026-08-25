export interface UserResponse {
  maNguoiDung: number;
  tenDangNhap: string;
  hoTen: string;
  email?: string | null;
  soDienThoai?: string | null;
  anhDaiDien?: string | null;
  trangThai: boolean;
  ngayTao?: string | null;
  roles: string[];
}

export interface CreateUserRequest {
  tenDangNhap: string;
  matKhau: string;
  hoTen: string;
  email?: string;
  soDienThoai?: string;
  anhDaiDien?: string;
  roles?: string[];
}

export interface UpdateUserRequest {
  hoTen: string;
  email?: string;
  soDienThoai?: string;
  anhDaiDien?: string;
  roles?: string[];
}

export interface UpdateUserStatusRequest {
  trangThai: boolean;
}
