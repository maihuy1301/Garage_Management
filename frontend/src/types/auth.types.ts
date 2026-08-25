import { RoleType } from './role.types';

export interface LoginRequest {
  tenDangNhap: string;
  matKhau: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  maNguoiDung: number;
  tenDangNhap: string;
  hoTen: string;
}

export interface CurrentUser {
  maNguoiDung: number;
  tenDangNhap: string;
  hoTen: string;
  email?: string;
  roles: RoleType[];
}

export interface AuthState {
  user: CurrentUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}
