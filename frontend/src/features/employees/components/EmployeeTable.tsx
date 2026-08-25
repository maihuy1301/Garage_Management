import React, { useState, useMemo } from 'react';
import { EmployeeResponse } from '@/types/employee.types';
import { RoleLabels, RoleType } from '@/types/role.types';
import { EmptyState } from '@/components/common/EmptyState';

interface EmployeeTableProps {
  employees: EmployeeResponse[];
  onView: (employee: EmployeeResponse) => void;
  onEdit: (employee: EmployeeResponse) => void;
  onToggleStatus: (employee: EmployeeResponse) => void;
}

export const EmployeeTable: React.FC<EmployeeTableProps> = ({
  employees,
  onView,
  onEdit,
  onToggleStatus,
}) => {
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedBranch, setSelectedBranch] = useState<string>('ALL');
  const [selectedRole, setSelectedRole] = useState<string>('ALL');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');

  // Extract unique branches from employees
  const availableBranches = useMemo(() => {
    const branches = new Map<string, string>();
    employees.forEach((emp) => {
      if (emp.maChiNhanhCode && emp.tenChiNhanh) {
        branches.set(emp.maChiNhanhCode, emp.tenChiNhanh);
      }
    });
    return Array.from(branches.entries());
  }, [employees]);

  // Filtered employees list
  const filteredEmployees = useMemo(() => {
    return employees.filter((emp) => {
      // Search term filter
      const term = searchTerm.toLowerCase().trim();
      const matchSearch =
        !term ||
        emp.maNhanVienCode?.toLowerCase().includes(term) ||
        emp.hoTen?.toLowerCase().includes(term) ||
        emp.tenDangNhap?.toLowerCase().includes(term) ||
        emp.email?.toLowerCase().includes(term) ||
        emp.chucVu?.toLowerCase().includes(term) ||
        emp.soDienThoai?.includes(term);

      // Branch filter
      const matchBranch =
        selectedBranch === 'ALL' || emp.maChiNhanhCode === selectedBranch;

      // Role filter
      const matchRole =
        selectedRole === 'ALL' || (emp.roles && emp.roles.includes(selectedRole));

      // Status filter
      const matchStatus =
        selectedStatus === 'ALL' ||
        (selectedStatus === 'ACTIVE' && emp.trangThai) ||
        (selectedStatus === 'INACTIVE' && !emp.trangThai);

      return matchSearch && matchBranch && matchRole && matchStatus;
    });
  }, [employees, searchTerm, selectedBranch, selectedRole, selectedStatus]);

  return (
    <div>
      {/* Search & Filter Bar */}
      <div className="filter-card">
        <div className="filter-search-box">
          <span className="material-symbols-outlined">search</span>
          <input
            type="text"
            placeholder="Tìm theo mã NV, họ tên, email, chức vụ..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-controls">
          {/* Branch Filter */}
          {availableBranches.length > 1 && (
            <select
              className="filter-select"
              value={selectedBranch}
              onChange={(e) => setSelectedBranch(e.target.value)}
            >
              <option value="ALL">Tất cả chi nhánh</option>
              {availableBranches.map(([code, name]) => (
                <option key={code} value={code}>
                  {name}
                </option>
              ))}
            </select>
          )}

          {/* Role Filter */}
          <select
            className="filter-select"
            value={selectedRole}
            onChange={(e) => setSelectedRole(e.target.value)}
          >
            <option value="ALL">Tất cả vai trò</option>
            <option value="ROLE_ADMIN">Quản trị viên</option>
            <option value="ROLE_MANAGER">Quản lý chi nhánh</option>
            <option value="ROLE_FRONT_DESK">Lễ tân tiếp nhận</option>
            <option value="ROLE_TECHNICIAN">Kỹ thuật viên</option>
          </select>

          {/* Status Filter */}
          <select
            className="filter-select"
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="ACTIVE">Đang làm việc</option>
            <option value="INACTIVE">Đã nghỉ việc</option>
          </select>
        </div>
      </div>

      {/* Table Card */}
      <div className="data-table-card">
        {filteredEmployees.length === 0 ? (
          <div style={{ padding: '32px 0' }}>
            <EmptyState
              title="Không tìm thấy nhân viên"
              description={
                employees.length === 0
                  ? 'Hiện chưa có nhân viên nào trong danh sách.'
                  : 'Không có nhân viên nào khớp với điều kiện tìm kiếm hoặc bộ lọc.'
              }
            />
          </div>
        ) : (
          <div className="data-table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Nhân viên</th>
                  <th>Tài khoản / Email</th>
                  <th>Vai trò</th>
                  <th>Chi nhánh</th>
                  <th>Chức vụ</th>
                  <th>Trạng thái</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredEmployees.map((emp) => {
                  const initials = emp.hoTen
                    ? emp.hoTen
                        .split(' ')
                        .filter(Boolean)
                        .map((w) => w[0])
                        .slice(-2)
                        .join('')
                        .toUpperCase()
                    : 'NV';

                  const primaryRole = emp.roles?.[0] as RoleType | undefined;
                  const roleBadgeText = primaryRole
                    ? RoleLabels[primaryRole] || primaryRole
                    : 'Nhân viên';

                  return (
                    <tr key={emp.maNhanVien}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                          <div className="data-table-avatar">
                            {initials}
                          </div>
                          <div>
                            <div style={{ fontWeight: 600, color: 'var(--color-on-surface)' }}>
                              {emp.hoTen || 'Chưa cập nhật'}
                            </div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--color-outline)', marginTop: '2px' }}>
                              Mã: <span style={{ fontWeight: 600, color: 'var(--color-primary)' }}>{emp.maNhanVienCode}</span>
                            </div>
                          </div>
                        </div>
                      </td>

                      <td>
                        <div style={{ fontWeight: 500 }}>{emp.tenDangNhap || '—'}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--color-outline)', marginTop: '2px' }}>
                          {emp.email || '—'}
                        </div>
                      </td>

                      <td>
                        <span className="status-badge primary">
                          {roleBadgeText}
                        </span>
                      </td>

                      <td>
                        <div style={{ fontSize: '0.85rem' }}>
                          {emp.tenChiNhanh || 'Toàn hệ thống'}
                        </div>
                      </td>

                      <td>
                        <div style={{ fontSize: '0.85rem', color: 'var(--color-on-surface-variant)' }}>
                          {emp.chucVu || '—'}
                        </div>
                      </td>

                      <td>
                        <span className={`status-badge ${emp.trangThai ? 'success' : 'danger'}`}>
                          {emp.trangThai ? 'Đang làm việc' : 'Đã nghỉ việc'}
                        </span>
                      </td>

                      <td>
                        <div className="table-actions">
                          <button
                            type="button"
                            className="table-action-btn"
                            title="Xem chi tiết"
                            onClick={() => onView(emp)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              visibility
                            </span>
                          </button>

                          <button
                            type="button"
                            className="table-action-btn edit"
                            title="Chỉnh sửa"
                            onClick={() => onEdit(emp)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              edit
                            </span>
                          </button>

                          <button
                            type="button"
                            className={`table-action-btn ${emp.trangThai ? 'danger' : 'success'}`}
                            title={emp.trangThai ? 'Khóa nhân viên' : 'Kích hoạt lại'}
                            onClick={() => onToggleStatus(emp)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              {emp.trangThai ? 'block' : 'check_circle'}
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
