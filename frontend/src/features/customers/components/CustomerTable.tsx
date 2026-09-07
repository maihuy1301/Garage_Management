import React, { useState, useMemo } from 'react';
import { CustomerResponse } from '@/types/customer.types';
import { EmptyState } from '@/components/common/EmptyState';

interface CustomerTableProps {
  customers: CustomerResponse[];
  onView: (customer: CustomerResponse) => void;
  onEdit: (customer: CustomerResponse) => void;
  onToggleStatus: (customer: CustomerResponse) => void;
}

export const CustomerTable: React.FC<CustomerTableProps> = ({
  customers,
  onView,
  onEdit,
  onToggleStatus,
}) => {
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');

  const filteredCustomers = useMemo(() => {
    return customers.filter((cust) => {
      const term = searchTerm.toLowerCase().trim();
      const matchSearch =
        !term ||
        cust.hoTen?.toLowerCase().includes(term) ||
        cust.tenDangNhap?.toLowerCase().includes(term) ||
        cust.email?.toLowerCase().includes(term) ||
        cust.soDienThoai?.includes(term) ||
        cust.diaChi?.toLowerCase().includes(term) ||
        String(cust.maKhachHang).includes(term);

      const isCustActive = cust.trangThai !== false;
      const matchStatus =
        selectedStatus === 'ALL' ||
        (selectedStatus === 'ACTIVE' && isCustActive) ||
        (selectedStatus === 'INACTIVE' && !isCustActive);

      return matchSearch && matchStatus;
    });
  }, [customers, searchTerm, selectedStatus]);

  return (
    <div>
      {/* Search & Filter Bar */}
      <div className="filter-card">
        <div className="filter-search-box">
          <span className="material-symbols-outlined">search</span>
          <input
            type="text"
            placeholder="Tìm theo mã KH, họ tên, số điện thoại, email, địa chỉ..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-controls">
          <select
            className="filter-select"
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="ACTIVE">Đang hoạt động</option>
            <option value="INACTIVE">Tài khoản khóa</option>
          </select>
        </div>
      </div>

      {/* Table Card */}
      <div className="data-table-card">
        {filteredCustomers.length === 0 ? (
          <div style={{ padding: '32px 0' }}>
            <EmptyState
              title="Không tìm thấy khách hàng"
              description={
                customers.length === 0
                  ? 'Hiện chưa có hồ sơ khách hàng nào trong hệ thống.'
                  : 'Không có khách hàng nào khớp với điều kiện tìm kiếm hoặc bộ lọc.'
              }
            />
          </div>
        ) : (
          <div className="data-table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Khách hàng</th>
                  <th>Tài khoản / Email</th>
                  <th>Số điện thoại</th>
                  <th>Địa chỉ</th>
                  <th>Ngày sinh</th>
                  <th>Trạng thái</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredCustomers.map((cust) => {
                  const initials = cust.hoTen
                    ? cust.hoTen
                        .split(' ')
                        .filter(Boolean)
                        .map((w) => w[0])
                        .slice(-2)
                        .join('')
                        .toUpperCase()
                    : 'KH';

                  const isActive = cust.trangThai !== false;

                  return (
                    <tr key={cust.maKhachHang}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                          <div className="data-table-avatar orange">
                            {initials}
                          </div>
                          <div>
                            <div style={{ fontWeight: 600, color: 'var(--color-on-surface)' }}>
                              {cust.hoTen || 'Khách hàng'}
                            </div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--color-outline)', marginTop: '2px' }}>
                              ID: <span style={{ fontWeight: 600, color: 'var(--color-primary)' }}>#{cust.maKhachHang}</span>
                            </div>
                          </div>
                        </div>
                      </td>

                      <td>
                        <div style={{ fontWeight: 500 }}>{cust.tenDangNhap || '—'}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--color-outline)', marginTop: '2px' }}>
                          {cust.email || '—'}
                        </div>
                      </td>

                      <td>
                        <div style={{ fontWeight: 600, color: 'var(--color-primary)' }}>
                          {cust.soDienThoai || '—'}
                        </div>
                      </td>

                      <td>
                        <div style={{ fontSize: '0.85rem', maxWidth: '220px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {cust.diaChi || '—'}
                        </div>
                      </td>

                      <td>
                        <div style={{ fontSize: '0.85rem', color: 'var(--color-on-surface-variant)' }}>
                          {cust.ngaySinh
                            ? new Date(cust.ngaySinh).toLocaleDateString('vi-VN')
                            : '—'}
                        </div>
                      </td>

                      <td>
                        <span className={`status-badge ${isActive ? 'success' : 'danger'}`}>
                          {isActive ? 'Hoạt động' : 'Đã khóa'}
                        </span>
                      </td>

                      <td>
                        <div className="table-actions">
                          <button
                            type="button"
                            className="table-action-btn"
                            title="Xem chi tiết & Xe"
                            onClick={() => onView(cust)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              visibility
                            </span>
                          </button>

                          <button
                            type="button"
                            className="table-action-btn edit"
                            title="Chỉnh sửa thông tin"
                            onClick={() => onEdit(cust)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              edit
                            </span>
                          </button>

                          <button
                            type="button"
                            className={`table-action-btn ${isActive ? 'danger' : 'success'}`}
                            title={isActive ? 'Khóa tài khoản' : 'Kích hoạt lại'}
                            onClick={() => onToggleStatus(cust)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              {isActive ? 'lock' : 'lock_open'}
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
