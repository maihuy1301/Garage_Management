import React, { useState, useRef, useEffect } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { RoleLabels, RoleType } from '@/types/role.types';

interface HeaderProps {
  title?: string;
  onToggleSidebar?: () => void;
}

export const Header: React.FC<HeaderProps> = ({ title = 'AutoCare Multi-Branch', onToggleSidebar }) => {
  const { user, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const userRoles = (user?.roles || []) as RoleType[];
  const primaryRole = userRoles[0];
  const roleLabel = primaryRole ? (RoleLabels[primaryRole] || primaryRole) : 'Người dùng';
  const initial = user?.hoTen?.charAt(0)?.toUpperCase() || user?.tenDangNhap?.charAt(0)?.toUpperCase() || 'U';

  // Close dropdown on outside click
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    setDropdownOpen(false);
    await logout();
  };

  return (
    <header className="header">
      {/* Left Area: Mobile toggle, Title & Search */}
      <div className="header-left">
        {onToggleSidebar && (
          <button
            onClick={onToggleSidebar}
            className="header-icon-btn"
            style={{ display: 'flex' }}
            aria-label="Mở Menu Sidebar"
          >
            <span className="material-symbols-outlined">menu</span>
          </button>
        )}
        <h2 className="header-page-title">{title}</h2>

        <div className="header-search">
          <span className="material-symbols-outlined">search</span>
          <input
            type="text"
            placeholder="Tìm kiếm phiếu, xe, khách hàng..."
            aria-label="Tìm kiếm trong hệ thống"
          />
        </div>
      </div>

      {/* Right Area: Notifications, Help, User Dropdown */}
      <div className="header-right">
        <button
          className="header-icon-btn"
          title="Thông báo hệ thống"
          aria-label="Thông báo hệ thống"
          onClick={() => alert('Chức năng Thông báo thời gian thực đã sẵn sàng qua WebSocket.')}
        >
          <span className="material-symbols-outlined">notifications</span>
          <span className="header-badge-dot"></span>
        </button>

        <button
          className="header-icon-btn"
          title="Trợ giúp & Hướng dẫn"
          aria-label="Trợ giúp"
          onClick={() => alert('AutoCare Multi-Branch - Phiên bản 1.0.0. Tài liệu hỗ trợ: docs/PROJECT_CONTEXT.md')}
        >
          <span className="material-symbols-outlined">help_outline</span>
        </button>

        <div className="header-divider"></div>

        {/* User Menu Trigger */}
        <div className="user-menu-container" ref={dropdownRef}>
          <div
            className="user-menu-trigger"
            onClick={() => setDropdownOpen(!dropdownOpen)}
            role="button"
            tabIndex={0}
            aria-haspopup="true"
            aria-expanded={dropdownOpen}
            aria-label="Menu người dùng"
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                setDropdownOpen(!dropdownOpen);
              }
            }}
          >
            <div className="user-menu-avatar">{initial}</div>
            <div className="user-menu-info">
              <div className="user-menu-name">{user?.hoTen || user?.tenDangNhap || 'Tài khoản'}</div>
              <div className="user-menu-role">{roleLabel}</div>
            </div>
            <span className="material-symbols-outlined" style={{ fontSize: '18px', color: 'var(--color-outline)' }}>
              {dropdownOpen ? 'expand_less' : 'expand_more'}
            </span>
          </div>

          {/* User Dropdown Menu */}
          {dropdownOpen && (
            <div className="user-dropdown">
              <div className="dropdown-header">
                <div className="dropdown-user-name">{user?.hoTen || user?.tenDangNhap}</div>
                <div className="dropdown-user-email">{user?.email || `@${user?.tenDangNhap}`}</div>
                <span className="dropdown-role-badge">{roleLabel}</span>
              </div>

              <div style={{ padding: '4px 0' }}>
                <button
                  type="button"
                  className="dropdown-item"
                  onClick={() => {
                    setDropdownOpen(false);
                    alert(`Thông tin tài khoản:\n- ID: ${user?.maNguoiDung}\n- Tên đăng nhập: ${user?.tenDangNhap}\n- Họ tên: ${user?.hoTen}\n- Email: ${user?.email}\n- Vai trò: ${user?.roles?.join(', ')}`);
                  }}
                >
                  <span className="material-symbols-outlined">person</span>
                  <span>Hồ sơ người dùng</span>
                </button>

                <div className="dropdown-divider"></div>

                <button
                  type="button"
                  className="dropdown-item danger"
                  onClick={handleLogout}
                >
                  <span className="material-symbols-outlined" style={{ color: 'var(--color-danger)' }}>logout</span>
                  <span style={{ color: 'var(--color-danger)', fontWeight: 600 }}>Đăng xuất</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
