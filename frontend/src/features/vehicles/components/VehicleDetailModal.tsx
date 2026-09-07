import React from 'react';
import { VehicleResponse } from '@/types/vehicle.types';

interface VehicleDetailModalProps {
  vehicle: VehicleResponse | null;
  isOpen: boolean;
  onClose: () => void;
  onEdit?: (vehicle: VehicleResponse) => void;
}

export const VehicleDetailModal: React.FC<VehicleDetailModalProps> = ({
  vehicle,
  isOpen,
  onClose,
  onEdit,
}) => {
  if (!isOpen || !vehicle) return null;

  const isActive = vehicle.trangThai !== false;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="modal-header">
          <div className="modal-title">
            <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>
              directions_car
            </span>
            <span>Chi Tiết Phương Tiện: {vehicle.bienSo}</span>
          </div>
          <button className="modal-close-btn" onClick={onClose} aria-label="Đóng">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        {/* Modal Body */}
        <div className="modal-body">
          {/* Header Summary Banner */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '16px',
              padding: '16px',
              backgroundColor: 'var(--color-surface-container-low)',
              borderRadius: 'var(--radius-lg)',
              marginBottom: '20px',
              border: '1px solid var(--color-outline-variant)',
            }}
          >
            <div
              style={{
                width: '52px',
                height: '52px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--color-primary)',
                color: '#ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '30px' }}>
                directions_car
              </span>
            </div>

            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
                <span
                  style={{
                    fontFamily: 'monospace',
                    fontSize: '1.25rem',
                    fontWeight: 700,
                    color: 'var(--color-primary)',
                    backgroundColor: '#ffffff',
                    padding: '2px 10px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--color-outline-variant)',
                  }}
                >
                  {vehicle.bienSo}
                </span>
                <span className={`status-badge ${isActive ? 'success' : 'danger'}`}>
                  {isActive ? 'Đang hoạt động' : 'Tạm ngưng hoạt động'}
                </span>
              </div>
              <p style={{ fontSize: '0.875rem', color: 'var(--color-on-surface-variant)', marginTop: '4px' }}>
                {vehicle.hangXe || 'Hãng xe'} {vehicle.model || 'Dòng xe'} {vehicle.namSanXuat ? `(Đời ${vehicle.namSanXuat})` : ''}
              </p>
            </div>
          </div>

          {/* Section: Thông số kỹ thuật */}
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
            Thông số kỹ thuật phương tiện
          </h4>

          <div className="detail-grid" style={{ marginBottom: '20px' }}>
            <div className="detail-item">
              <div className="detail-label">Hãng sản xuất</div>
              <div className="detail-value">{vehicle.hangXe || 'Chưa cập nhật'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Dòng xe (Model)</div>
              <div className="detail-value">{vehicle.model || 'Chưa cập nhật'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Năm sản xuất</div>
              <div className="detail-value">{vehicle.namSanXuat ? `${vehicle.namSanXuat}` : 'Chưa cập nhật'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Màu sơn xe</div>
              <div className="detail-value">{vehicle.mauXe || 'Chưa cập nhật'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Số KM đã chạy (ODO)</div>
              <div className="detail-value" style={{ fontWeight: 700, color: 'var(--color-secondary-container)' }}>
                {vehicle.soKmHienTai != null ? `${vehicle.soKmHienTai.toLocaleString('vi-VN')} km` : '0 km'}
              </div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Số khung / Số VIN</div>
              <div className="detail-value" style={{ fontFamily: 'monospace' }}>
                {vehicle.soVIN || 'Chưa cập nhật'}
              </div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Ngày đăng ký vào hệ thống</div>
              <div className="detail-value">
                {vehicle.ngayTao
                  ? new Date(vehicle.ngayTao).toLocaleString('vi-VN', {
                      year: 'numeric',
                      month: '2-digit',
                      day: '2-digit',
                      hour: '2-digit',
                      minute: '2-digit',
                    })
                  : '—'}
              </div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Mã phương tiện (ID)</div>
              <div className="detail-value">#{vehicle.maXe}</div>
            </div>
          </div>

          {/* Section: Chủ sở hữu phương tiện */}
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
            Thông tin chủ sở hữu
          </h4>

          <div className="detail-grid">
            <div className="detail-item">
              <div className="detail-label">Chủ xe (Khách hàng)</div>
              <div className="detail-value" style={{ fontWeight: 600 }}>
                {vehicle.tenChuXe || 'Chưa liên kết chủ xe'}
              </div>
            </div>

            <div className="detail-item">
              <div className="detail-label">ID Khách hàng</div>
              <div className="detail-value" style={{ color: 'var(--color-secondary-container)', fontWeight: 600 }}>
                {vehicle.maKhachHang ? `#${vehicle.maKhachHang}` : '—'}
              </div>
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
                onEdit(vehicle);
              }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                edit
              </span>
              <span>Chỉnh sửa thông số</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
