import React from 'react';
import { UserResponse } from '@/types/user.types';
import { RoleLabels, RoleType } from '@/types/role.types';

interface UserDetailModalProps {
  user: UserResponse | null;
  isOpen: boolean;
  onClose: () => void;
  onEdit?: (user: UserResponse) => void;
}

export const UserDetailModal: React.FC<UserDetailModalProps> = ({
  user,
  isOpen,
  onClose,
  onEdit,
}) => {
  if (!isOpen || !user) return null;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container large" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="modal-header">
          <div className="modal-title">
            <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>
              manage_accounts
            </span>
            <span>Chi Tiết Tài Khoản: {user.tenDangNhap}</span>
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
              className="data-table-avatar slate"
              style={{ width: '54px', height: '54px', fontSize: '1.25rem' }}
            >
              {user.hoTen?.charAt(0)?.toUpperCase() || 'U'}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--color-on-surface)' }}>
                  {user.hoTen}
                </h3>
                <span className={`status-badge ${user.trangThai ? 'success' : 'danger'}`}>
                  {user.trangThai ? 'Đang hoạt động' : 'Tài khoản đã khóa'}
                </span>
              </div>
              <p style={{ fontSize: '0.875rem', color: 'var(--color-on-surface-variant)', marginTop: '2px' }}>
                Tên đăng nhập: <strong>{user.tenDangNhap}</strong> • ID: #{user.maNguoiDung}
              </p>
            </div>
          </div>

          {/* Details Grid */}
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
            Thông tin định danh & liên hệ
          </h4>

          <div className="detail-grid">
            <div className="detail-item">
              <div className="detail-label">Tên đăng nhập</div>
              <div className="detail-value">{user.tenDangNhap}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Họ và tên</div>
              <div className="detail-value">{user.hoTen}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Email</div>
              <div className="detail-value">{user.email || 'Chưa cập nhật'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Số điện thoại</div>
              <div className="detail-value">{user.soDienThoai || 'Chưa cập nhật'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Ngày đăng ký</div>
              <div className="detail-value">
                {user.ngayTao
                  ? new Date(user.ngayTao).toLocaleString('vi-VN')
                  : 'Chưa cập nhật'}
              </div>
            </div>
          </div>

          {/* Assigned Roles */}
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
            Vai trò phân quyền hệ thống
          </h4>

          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            {user.roles && user.roles.length > 0 ? (
              user.roles.map((role) => (
                <span
                  key={role}
                  className="status-badge primary"
                  style={{ padding: '6px 14px', fontSize: '0.85rem' }}
                >
                  <span className="material-symbols-outlined" style={{ fontSize: '16px', marginRight: '4px' }}>
                    verified
                  </span>
                  {RoleLabels[role as RoleType] || role} ({role})
                </span>
              ))
            ) : (
              <span style={{ color: 'var(--color-outline)', fontSize: '0.875rem' }}>
                Chưa gán vai trò nào
              </span>
            )}
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
                onEdit(user);
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
