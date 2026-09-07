import React from 'react';
import { EmployeeResponse } from '@/types/employee.types';
import { RoleLabels, RoleType } from '@/types/role.types';

interface EmployeeDetailModalProps {
  employee: EmployeeResponse | null;
  isOpen: boolean;
  onClose: () => void;
  onEdit?: (employee: EmployeeResponse) => void;
}

export const EmployeeDetailModal: React.FC<EmployeeDetailModalProps> = ({
  employee,
  isOpen,
  onClose,
  onEdit,
}) => {
  if (!isOpen || !employee) return null;

  const roleText = (employee.roles || [])
    .map((r) => RoleLabels[r as RoleType] || r)
    .join(', ') || 'Chưa phân quyền';

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container large" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="modal-header">
          <div className="modal-title">
            <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>
              badge
            </span>
            <span>Hồ Sơ Nhân Viên: {employee.hoTen || `#${employee.maNhanVien}`}</span>
          </div>
          <button className="modal-close-btn" onClick={onClose} aria-label="Đóng">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        {/* Modal Body */}
        <div className="modal-body">
          {/* Avatar & Header Summary */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '16px',
              padding: '16px',
              backgroundColor: 'var(--color-surface-container-low)',
              borderRadius: 'var(--radius-lg)',
              marginBottom: '24px',
              border: '1px solid var(--color-outline-variant)',
            }}
          >
            <div
              className="data-table-avatar"
              style={{ width: '54px', height: '54px', fontSize: '1.25rem' }}
            >
              {employee.hoTen?.charAt(0)?.toUpperCase() || 'NV'}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--color-on-surface)' }}>
                  {employee.hoTen || 'Chưa cập nhật họ tên'}
                </h3>
                <span className={`status-badge ${employee.trangThai ? 'success' : 'danger'}`}>
                  {employee.trangThai ? 'Đang làm việc' : 'Đã nghỉ việc'}
                </span>
              </div>
              <p style={{ fontSize: '0.875rem', color: 'var(--color-on-surface-variant)', marginTop: '2px' }}>
                {employee.chucVu || 'Nhân viên'} • {employee.tenChiNhanh || 'Toàn hệ thống'}
              </p>
            </div>
          </div>

          {/* Detailed Info Grid */}
          <h4
            style={{
              fontSize: '0.85rem',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
              color: 'var(--color-primary)',
              marginBottom: '12px',
            }}
          >
            Thông tin nhân sự
          </h4>

          <div className="detail-grid">
            <div className="detail-item">
              <div className="detail-label">ID Nhân viên</div>
              <div className="detail-value">#{employee.maNhanVien}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Chức vụ</div>
              <div className="detail-value">{employee.chucVu || 'Chưa xác định'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Chi nhánh</div>
              <div className="detail-value">
                {employee.tenChiNhanh || 'Toàn hệ thống'}
              </div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Ngày vào làm</div>
              <div className="detail-value">
                {employee.ngayVaoLam
                  ? new Date(employee.ngayVaoLam).toLocaleDateString('vi-VN')
                  : 'Chưa cập nhật'}
              </div>
            </div>
          </div>

          <h4
            style={{
              fontSize: '0.85rem',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
              color: 'var(--color-primary)',
              marginBottom: '12px',
              marginTop: '16px',
            }}
          >
            Thông tin tài khoản & liên hệ
          </h4>

          <div className="detail-grid">
            <div className="detail-item">
              <div className="detail-label">Tên đăng nhập</div>
              <div className="detail-value">{employee.tenDangNhap || 'Không có'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Vai trò hệ thống</div>
              <div className="detail-value">{roleText}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Email</div>
              <div className="detail-value">{employee.email || 'Chưa cập nhật'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Số điện thoại</div>
              <div className="detail-value">{employee.soDienThoai || 'Chưa cập nhật'}</div>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="modal-footer">
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Đóng
          </button>
          {onEdit && (
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => {
                onClose();
                onEdit(employee);
              }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                edit
              </span>
              <span>Chỉnh sửa</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
