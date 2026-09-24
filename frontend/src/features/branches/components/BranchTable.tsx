import React, { useState, useMemo } from 'react';
import { BranchResponse } from '@/types/branch.types';
import { EmptyState } from '@/components/common/EmptyState';
import { ActionDropdown } from '@/components/common/ActionDropdown';

interface BranchTableProps {
  branches: BranchResponse[];
  onView: (branch: BranchResponse) => void;
  onEdit: (branch: BranchResponse) => void;
  onToggleStatus: (branch: BranchResponse) => void;
  onDelete: (branch: BranchResponse) => void;
}

export const BranchTable: React.FC<BranchTableProps> = ({
  branches,
  onView,
  onEdit,
  onToggleStatus,
  onDelete,
}) => {
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');

  const filteredBranches = useMemo(() => {
    return branches.filter((b) => {
      const term = searchTerm.toLowerCase().trim();
      const matchSearch =
        !term ||
        b.tenChiNhanh?.toLowerCase().includes(term) ||
        b.diaChi?.toLowerCase().includes(term) ||
        b.email?.toLowerCase().includes(term) ||
        b.soDienThoai?.includes(term) ||
        String(b.maChiNhanh).includes(term);

      const matchStatus =
        selectedStatus === 'ALL' ||
        (selectedStatus === 'ACTIVE' && b.trangThai) ||
        (selectedStatus === 'INACTIVE' && !b.trangThai);

      return matchSearch && matchStatus;
    });
  }, [branches, searchTerm, selectedStatus]);

  return (
    <div>
      {/* Search & Filter Bar */}
      <div className="filter-card">
        <div className="filter-search-box">
          <span className="material-symbols-outlined">search</span>
          <input
            type="text"
            placeholder="Tìm theo tên chi nhánh, địa chỉ, số điện thoại, email..."
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
            <option value="INACTIVE">Ngưng hoạt động</option>
          </select>
        </div>
      </div>

      {/* Data Table Card */}
      <div className="data-table-card">
        {filteredBranches.length === 0 ? (
          <div style={{ padding: '32px 0' }}>
            <EmptyState
              title="Không tìm thấy chi nhánh nào"
              description={
                searchTerm || selectedStatus !== 'ALL'
                  ? 'Thử thay đổi từ khóa hoặc điều kiện lọc tìm kiếm.'
                  : 'Hệ thống chưa có dữ liệu chi nhánh nào.'
              }
            />
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th style={{ width: '80px' }}>Mã CN</th>
                  <th>Tên chi nhánh</th>
                  <th>Địa chỉ</th>
                  <th>Liên hệ</th>
                  <th>Trạng thái</th>
                  <th>Ngày tạo</th>
                  <th style={{ width: '160px', textAlign: 'center' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredBranches.map((branch) => {
                  const formattedDate = branch.ngayTao
                    ? new Date(branch.ngayTao).toLocaleDateString('vi-VN', {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric',
                      })
                    : '—';

                  return (
                    <tr key={branch.maChiNhanh}>
                      <td>
                        <span className="badge badge-neutral" style={{ fontWeight: 600 }}>
                          #{branch.maChiNhanh}
                        </span>
                      </td>
                      <td>
                        <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                          {branch.tenChiNhanh}
                        </div>
                      </td>
                      <td>
                        <div
                          style={{
                            maxWidth: '260px',
                            whiteSpace: 'nowrap',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            color: 'var(--text-secondary)',
                          }}
                          title={branch.diaChi}
                        >
                          {branch.diaChi || '—'}
                        </div>
                      </td>
                      <td>
                        <div style={{ fontSize: '0.85rem' }}>
                          {branch.soDienThoai && (
                            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                              <span className="material-symbols-outlined" style={{ fontSize: '15px', color: 'var(--primary)' }}>
                                call
                              </span>
                              <span>{branch.soDienThoai}</span>
                            </div>
                          )}
                          {branch.email && (
                            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--text-muted)' }}>
                              <span className="material-symbols-outlined" style={{ fontSize: '15px' }}>
                                mail
                              </span>
                              <span>{branch.email}</span>
                            </div>
                          )}
                          {!branch.soDienThoai && !branch.email && <span>—</span>}
                        </div>
                      </td>
                      <td>
                        {branch.trangThai ? (
                          <span className="badge badge-success">
                            <span className="badge-dot"></span>
                            Hoạt động
                          </span>
                        ) : (
                          <span className="badge badge-danger">
                            <span className="badge-dot"></span>
                            Ngưng hoạt động
                          </span>
                        )}
                      </td>
                      <td>
                        <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                          {formattedDate}
                        </span>
                      </td>
                      <td style={{ textAlign: 'center' }}>
                        <ActionDropdown
                          items={[
                            {
                              label: 'Xem chi tiết',
                              icon: 'visibility',
                              onClick: () => onView(branch),
                            },
                            {
                              label: 'Chỉnh sửa thông tin',
                              icon: 'edit',
                              variant: 'primary',
                              onClick: () => onEdit(branch),
                            },
                            {
                              label: branch.trangThai ? 'Ngưng hoạt động' : 'Kích hoạt lại',
                              icon: branch.trangThai ? 'pause_circle' : 'check_circle',
                              variant: branch.trangThai ? 'warning' : 'success',
                              onClick: () => onToggleStatus(branch),
                            },
                            {
                              label: 'Xóa chi nhánh',
                              icon: 'delete',
                              variant: 'danger',
                              onClick: () => onDelete(branch),
                            },
                          ]}
                        />
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
