import React from 'react';
import { CustomerResponse } from '@/types/customer.types';
import { VehicleResponse } from '@/types/vehicle.types';

interface CustomerDetailModalProps {
  customer: CustomerResponse | null;
  vehicles: VehicleResponse[];
  isOpen: boolean;
  onClose: () => void;
  onEdit?: (customer: CustomerResponse) => void;
}

export const CustomerDetailModal: React.FC<CustomerDetailModalProps> = ({
  customer,
  vehicles,
  isOpen,
  onClose,
  onEdit,
}) => {
  if (!isOpen || !customer) return null;

  // Filter vehicles belonging to this customer
  const customerVehicles = vehicles.filter(
    (v) => v.maKhachHang === customer.maKhachHang
  );

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container large" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="modal-header">
          <div className="modal-title">
            <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>
              person
            </span>
            <span>Hồ Sơ Khách Hàng: {customer.hoTen || `#${customer.maKhachHang}`}</span>
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
              className="data-table-avatar orange"
              style={{ width: '54px', height: '54px', fontSize: '1.25rem' }}
            >
              {customer.hoTen?.charAt(0)?.toUpperCase() || 'KH'}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--color-on-surface)' }}>
                  {customer.hoTen || 'Khách hàng'}
                </h3>
                <span className={`status-badge ${customer.trangThai !== false ? 'success' : 'danger'}`}>
                  {customer.trangThai !== false ? 'Đang hoạt động' : 'Tài khoản đã khóa'}
                </span>
              </div>
              <p style={{ fontSize: '0.875rem', color: 'var(--color-on-surface-variant)', marginTop: '2px' }}>
                ID KH: <strong>#{customer.maKhachHang}</strong> • Tài khoản: {customer.tenDangNhap || 'Chưa liên kết'}
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
            Thông tin liên hệ & cá nhân
          </h4>

          <div className="detail-grid">
            <div className="detail-item">
              <div className="detail-label">Số điện thoại</div>
              <div className="detail-value">{customer.soDienThoai || 'Chưa cập nhật'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Email</div>
              <div className="detail-value">{customer.email || 'Chưa cập nhật'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Địa chỉ</div>
              <div className="detail-value">{customer.diaChi || 'Chưa cập nhật'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Ngày sinh</div>
              <div className="detail-value">
                {customer.ngaySinh
                  ? new Date(customer.ngaySinh).toLocaleDateString('vi-VN')
                  : 'Chưa cập nhật'}
              </div>
            </div>
          </div>

          {/* Customer Vehicles List */}
          <div style={{ marginTop: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
              <h4
                style={{
                  fontSize: '0.85rem',
                  fontWeight: 700,
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em',
                  color: 'var(--color-primary)',
                }}
              >
                Phương tiện sở hữu ({customerVehicles.length})
              </h4>
            </div>

            {customerVehicles.length === 0 ? (
              <div
                style={{
                  padding: '20px',
                  textAlign: 'center',
                  backgroundColor: 'var(--color-surface-gray)',
                  borderRadius: 'var(--radius-md)',
                  color: 'var(--color-outline)',
                  fontSize: '0.875rem',
                }}
              >
                Chưa có phương tiện nào được liên kết với khách hàng này.
              </div>
            ) : (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '12px' }}>
                {customerVehicles.map((v) => (
                  <div
                    key={v.maXe}
                    style={{
                      padding: '14px',
                      backgroundColor: 'var(--color-surface-gray)',
                      borderRadius: 'var(--radius-md)',
                      border: '1px solid var(--color-border)',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '12px',
                    }}
                  >
                    <div
                      style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: 'var(--radius-md)',
                        backgroundColor: 'var(--color-surface-container)',
                        color: 'var(--color-primary)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      <span className="material-symbols-outlined">directions_car</span>
                    </div>
                    <div>
                      <div style={{ fontWeight: 700, fontSize: '0.95rem', color: 'var(--color-primary)' }}>
                        {v.bienSo}
                      </div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--color-on-surface-variant)' }}>
                        {v.hangXe} {v.model} ({v.namSanXuat || 'N/A'})
                      </div>
                    </div>
                  </div>
                ))}
              </div>
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
                onEdit(customer);
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
