import React, { useState, useMemo } from 'react';
import { UserResponse } from '@/types/user.types';
import { RoleLabels, RoleType } from '@/types/role.types';
import { EmptyState } from '@/components/common/EmptyState';

interface UserTableProps {
  users: UserResponse[];
  onView: (user: UserResponse) => void;
  onEdit: (user: UserResponse) => void;
  onToggleStatus: (user: UserResponse) => void;
}

export const UserTable: React.FC<UserTableProps> = ({
  users,
  onView,
  onEdit,
  onToggleStatus,
}) => {
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedRole, setSelectedRole] = useState<string>('ALL');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');

  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const term = searchTerm.toLowerCase().trim();
      const matchSearch =
        !term ||
        u.tenDangNhap?.toLowerCase().includes(term) ||
        u.hoTen?.toLowerCase().includes(term) ||
        u.email?.toLowerCase().includes(term) ||
        u.soDienThoai?.includes(term);

      const matchRole =
        selectedRole === 'ALL' || (u.roles && u.roles.includes(selectedRole));

      const matchStatus =
        selectedStatus === 'ALL' ||
        (selectedStatus === 'ACTIVE' && u.trangThai) ||
        (selectedStatus === 'INACTIVE' && !u.trangThai);

      return matchSearch && matchRole && matchStatus;
    });
  }, [users, searchTerm, selectedRole, selectedStatus]);

  return (
    <div>
      {/* Search & Filter Bar */}
      <div className="filter-card">
        <div className="filter-search-box">
          <span className="material-symbols-outlined">search</span>
          <input
            type="text"
            placeholder="Tìm theo tên đăng nhập, họ tên, email, SĐT..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-controls">
          {/* Role Filter */}
          <select
            className="filter-select"
            value={selectedRole}
            onChange={(e) => setSelectedRole(e.target.value)}
          >
            <option value="ALL">Tất cả vai trò</option>
            <option value="ROLE_ADMIN">Quản trị viên (Admin)</option>
            <option value="ROLE_MANAGER">Quản lý chi nhánh</option>
            <option value="ROLE_FRONT_DESK">Lễ tân tiếp nhận</option>
            <option value="ROLE_TECHNICIAN">Kỹ thuật viên</option>
            <option value="ROLE_CUSTOMER">Khách hàng</option>
          </select>

          {/* Status Filter */}
          <select
            className="filter-select"
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="ACTIVE">Đang hoạt động</option>
            <option value="INACTIVE">Đã khóa</option>
          </select>
        </div>
      </div>

      {/* Data Table Card */}
      <div className="data-table-card">
        {filteredUsers.length === 0 ? (
          <div style={{ padding: '32px 0' }}>
            <EmptyState
              title="Không tìm thấy tài khoản"
              description={
                users.length === 0
                  ? 'Hiện chưa có tài khoản người dùng nào trong hệ thống.'
                  : 'Không có tài khoản nào khớp với điều kiện tìm kiếm hoặc bộ lọc.'
              }
            />
          </div>
        ) : (
          <div className="data-table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Người dùng</th>
                  <th>Tên đăng nhập</th>
                  <th>Email</th>
                  <th>Số điện thoại</th>
                  <th>Vai trò</th>
                  <th>Trạng thái</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((u) => {
                  const initials = u.hoTen
                    ? u.hoTen
                        .split(' ')
                        .filter(Boolean)
                        .map((w) => w[0])
                        .slice(-2)
                        .join('')
                        .toUpperCase()
                    : 'U';

                  return (
                    <tr key={u.maNguoiDung}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                          <div className="data-table-avatar slate">
                            {initials}
                          </div>
                          <div>
                            <div style={{ fontWeight: 600, color: 'var(--color-on-surface)' }}>
                              {u.hoTen}
                            </div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--color-outline)', marginTop: '2px' }}>
                              ID: #{u.maNguoiDung}
                            </div>
                          </div>
                        </div>
                      </td>

                      <td>
                        <div style={{ fontWeight: 600, color: 'var(--color-primary)' }}>
                          {u.tenDangNhap}
                        </div>
                      </td>

                      <td>
                        <div style={{ fontSize: '0.85rem' }}>{u.email || '—'}</div>
                      </td>

                      <td>
                        <div style={{ fontSize: '0.85rem', color: 'var(--color-on-surface-variant)' }}>
                          {u.soDienThoai || '—'}
                        </div>
                      </td>

                      <td>
                        <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                          {u.roles?.map((role) => (
                            <span key={role} className="status-badge primary">
                              {RoleLabels[role as RoleType] || role}
                            </span>
                          ))}
                        </div>
                      </td>

                      <td>
                        <span className={`status-badge ${u.trangThai ? 'success' : 'danger'}`}>
                          {u.trangThai ? 'Hoạt động' : 'Đã khóa'}
                        </span>
                      </td>

                      <td>
                        <div className="table-actions">
                          <button
                            type="button"
                            className="table-action-btn"
                            title="Xem chi tiết tài khoản"
                            onClick={() => onView(u)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              visibility
                            </span>
                          </button>

                          <button
                            type="button"
                            className="table-action-btn edit"
                            title="Chỉnh sửa tài khoản & phân quyền"
                            onClick={() => onEdit(u)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              edit
                            </span>
                          </button>

                          <button
                            type="button"
                            className={`table-action-btn ${u.trangThai ? 'danger' : 'success'}`}
                            title={u.trangThai ? 'Khóa tài khoản' : 'Kích hoạt lại'}
                            onClick={() => onToggleStatus(u)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              {u.trangThai ? 'lock' : 'lock_open'}
                            </span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
