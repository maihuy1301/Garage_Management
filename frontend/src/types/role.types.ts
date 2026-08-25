export type RoleType =
  | 'ROLE_ADMIN'
  | 'ROLE_MANAGER'
  | 'ROLE_FRONT_DESK'
  | 'ROLE_TECHNICIAN'
  | 'ROLE_CUSTOMER';

export const UserRole = {
  ROLE_ADMIN: 'ROLE_ADMIN' as RoleType,
  ROLE_MANAGER: 'ROLE_MANAGER' as RoleType,
  ROLE_FRONT_DESK: 'ROLE_FRONT_DESK' as RoleType,
  ROLE_TECHNICIAN: 'ROLE_TECHNICIAN' as RoleType,
  ROLE_CUSTOMER: 'ROLE_CUSTOMER' as RoleType,
} as const;

export const RoleLabels: Record<RoleType, string> = {
  ROLE_ADMIN: 'Quản trị viên toàn hệ thống',
  ROLE_MANAGER: 'Quản lý chi nhánh',
  ROLE_FRONT_DESK: 'Lễ tân tiếp nhận',
  ROLE_TECHNICIAN: 'Kỹ thuật viên',
  ROLE_CUSTOMER: 'Khách hàng',
};

export interface RoleResponse {
  maVaiTro: number;
  tenVaiTro: RoleType | string;
  moTa?: string;
}
