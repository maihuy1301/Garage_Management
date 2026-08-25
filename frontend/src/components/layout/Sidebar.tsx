import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { RoleLabels, RoleType } from '@/types/role.types';

interface SidebarProps {
  isOpen?: boolean;
  onClose?: () => void;
}

interface NavItemConfig {
  label: string;
  path: string;
  icon: string;
  roles: RoleType[];
}

const ALL_NAV_ITEMS: NavItemConfig[] = [
  // Common
  {
    label: 'Dashboard',
    path: '/app/dashboard',
    icon: 'dashboard',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FRONT_DESK', 'ROLE_TECHNICIAN', 'ROLE_CUSTOMER'],
  },
  // Admin specific
  {
    label: 'Chi nhánh',
    path: '/app/branches',
    icon: 'store_mall_directory',
    roles: ['ROLE_ADMIN'],
  },
  {
    label: 'Tài khoản & Phân quyền',
    path: '/app/users',
    icon: 'manage_accounts',
    roles: ['ROLE_ADMIN'],
  },
  {
    label: 'Danh mục dịch vụ',
    path: '/app/services',
    icon: 'build',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER'],
  },
  // Staff / Branch operations
  {
    label: 'Nhân viên',
    path: '/app/employees',
    icon: 'badge',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER'],
  },
  {
    label: 'Lịch hẹn',
    path: '/app/appointments',
    icon: 'calendar_month',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FRONT_DESK', 'ROLE_CUSTOMER'],
  },
  {
    label: 'Tiếp nhận xe',
    path: '/app/reception',
    icon: 'car_repair',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FRONT_DESK'],
  },
  {
    label: 'Lệnh sửa chữa',
    path: '/app/repair-orders',
    icon: 'assignment',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FRONT_DESK', 'ROLE_TECHNICIAN', 'ROLE_CUSTOMER'],
  },
  {
    label: 'Tiến độ thực hiện',
    path: '/app/execution',
    icon: 'construction',
    roles: ['ROLE_ADMIN', 'ROLE_TECHNICIAN'],
  },
  {
    label: 'Khách hàng',
    path: '/app/customers',
    icon: 'groups',
    roles: ['ROLE_ADMIN'],
  },
  {
    label: 'Phương tiện / Xe',
    path: '/app/vehicles',
    icon: 'directions_car',
    roles: ['ROLE_ADMIN', 'ROLE_CUSTOMER'],
  },
  {
    label: 'Kho & Phụ tùng',
    path: '/app/inventory',
    icon: 'inventory_2',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER'],
  },
  {
    label: 'Báo giá dịch vụ',
    path: '/app/quotations',
    icon: 'request_quote',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER'],
  },
  {
    label: 'Hóa đơn & Thu ngân',
    path: '/app/invoices',
    icon: 'receipt_long',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FRONT_DESK', 'ROLE_CUSTOMER'],
  },
  {
    label: 'Thông báo',
    path: '/app/notifications',
    icon: 'notifications',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FRONT_DESK', 'ROLE_TECHNICIAN', 'ROLE_CUSTOMER'],
  },
  {
    label: 'Chat trao đổi',
    path: '/app/chat',
    icon: 'chat',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FRONT_DESK', 'ROLE_TECHNICIAN', 'ROLE_CUSTOMER'],
  },
  {
    label: 'Báo cáo & Thống kê',
    path: '/app/reports',
    icon: 'assessment',
    roles: ['ROLE_ADMIN', 'ROLE_MANAGER'],
  },
  {
    label: 'Cài đặt hệ thống',
    path: '/app/settings',
    icon: 'settings',
    roles: ['ROLE_ADMIN'],
  },
];

export const Sidebar: React.FC<SidebarProps> = ({ isOpen = false, onClose }) => {
  const { user } = useAuth();
  const userRoles = (user?.roles || []) as RoleType[];

  const filteredNavItems = ALL_NAV_ITEMS.filter((item) =>
    item.roles.some((role) => userRoles.includes(role))
  );

  const primaryRole = userRoles[0] as RoleType | undefined;
  const roleLabel = primaryRole ? (RoleLabels[primaryRole] || primaryRole) : 'Người dùng';
  const initial = user?.hoTen?.charAt(0)?.toUpperCase() || user?.tenDangNhap?.charAt(0)?.toUpperCase() || 'U';

  return (
    <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
      {/* Brand Header */}
      <div className="sidebar-header">
        <div className="sidebar-logo">AC</div>
        <div>
          <div className="sidebar-brand-title">AutoCare Admin</div>
          <div className="sidebar-brand-subtitle">Multi-Branch Console</div>
        </div>
      </div>

      {/* Navigation List */}
      <nav className="sidebar-nav">
        <div className="sidebar-section-label">Menu chức năng</div>
        {filteredNavItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            onClick={onClose}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <span className="material-symbols-outlined">{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      {/* User Footer Card */}
      {user && (
        <div className="sidebar-user-footer">
          <div className="sidebar-avatar">{initial}</div>
          <div className="sidebar-user-details">
            <div className="sidebar-user-name" title={user.hoTen || user.tenDangNhap}>
              {user.hoTen || user.tenDangNhap}
            </div>
            <div className="sidebar-user-role">{roleLabel}</div>
          </div>
        </div>
      )}
    </aside>
  );
};
