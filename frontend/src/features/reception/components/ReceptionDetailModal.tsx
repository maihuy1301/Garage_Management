import React from 'react';
import { ReceptionResponse, ReceptionStatusLabels, ReceptionStatusTone } from '@/types/reception.types';
import { Button } from '@/components/common/Button';

interface ReceptionDetailModalProps {
  isOpen: boolean;
  reception: ReceptionResponse | null;
  hasRepairOrder?: boolean;
  onClose: () => void;
  onCreateRepairOrder?: (reception: ReceptionResponse) => void;
}

export const ReceptionDetailModal: React.FC<ReceptionDetailModalProps> = ({
  isOpen,
  reception,
  hasRepairOrder = false,
  onClose,
  onCreateRepairOrder,
}) => {
  if (!isOpen || !reception) return null;

  const tone = ReceptionStatusTone[reception.trangThai] || 'primary';
  const statusLabel = ReceptionStatusLabels[reception.trangThai] || reception.trangThai;

  const formatDateTime = (dateStr?: string) => {
    if (!dateStr) return '—';
    try {
      const d = new Date(dateStr);
      return d.toLocaleString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className="modal-container"
        style={{ maxWidth: '680px' }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="modal-header">
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h3 className="modal-title" style={{ margin: 0 }}>Biên Bản Tiếp Nhận Xe #{reception.maTiepNhan}</h3>
              <span className={`status-badge ${tone}`}>
                {statusLabel}
              </span>
            </div>
            <p style={{ margin: '4px 0 0 0', fontSize: '0.85rem', color: 'var(--color-outline)' }}>
              Tiếp nhận lúc: {formatDateTime(reception.thoiGianTiepNhan)}
            </p>
          </div>
          <button className="modal-close-btn" onClick={onClose} type="button" aria-label="Đóng">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        {/* Body */}
        <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Thông tin 2 cột: Khách hàng & Phương tiện */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
            {/* Box Khách hàng */}
            <div
              style={{
                backgroundColor: 'var(--color-surface-container-low)',
                borderRadius: 'var(--radius-lg)',
                padding: '16px',
                border: '1px solid var(--color-border)',
              }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  color: 'var(--color-primary)',
                  fontWeight: 600,
                  fontSize: '0.85rem',
                  marginBottom: '10px',
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                  person
                </span>
                Thông Tin Khách Hàng
              </div>
              <div style={{ fontWeight: 600, fontSize: '1rem', color: 'var(--color-on-surface)' }}>
                {reception.tenKhachHang}
              </div>
              <div
                style={{
                  fontSize: '0.85rem',
                  color: 'var(--color-outline)',
                  marginTop: '4px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '15px' }}>
                  call
                </span>
                {reception.soDienThoaiKhachHang || 'Chưa có SĐT'}
              </div>
            </div>

            {/* Box Phương tiện */}
            <div
              style={{
                backgroundColor: 'var(--color-surface-container-low)',
                borderRadius: 'var(--radius-lg)',
                padding: '16px',
                border: '1px solid var(--color-border)',
              }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  color: 'var(--color-primary)',
                  fontWeight: 600,
                  fontSize: '0.85rem',
                  marginBottom: '10px',
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                  directions_car
                </span>
                Thông Tin Phương Tiện
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span
                  style={{
                    fontFamily: 'monospace',
                    fontWeight: 700,
                    fontSize: '0.95rem',
                    backgroundColor: '#fef3c7',
                    color: '#92400e',
                    padding: '2px 8px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid #fde68a',
                  }}
                >
                  {reception.bienSoXe}
                </span>
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--color-on-surface-variant)', marginTop: '4px' }}>
                {reception.hangXe || ''} {reception.modelXe || ''}
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--color-outline)', marginTop: '4px' }}>
                Số Km (ODO):{' '}
                <strong style={{ color: 'var(--color-on-surface)' }}>
                  {reception.soKm != null ? `${reception.soKm.toLocaleString('vi-VN')} km` : 'Chưa ghi nhận'}
                </strong>
              </div>
            </div>
          </div>

          {/* Chi nhánh & Nhân viên tiếp nhận */}
          <div
            style={{
              backgroundColor: '#ffffff',
              borderRadius: 'var(--radius-lg)',
              padding: '14px 16px',
              border: '1px solid var(--color-border)',
              display: 'grid',
              gridTemplateColumns: 'repeat(3, 1fr)',
              gap: '12px',
            }}
          >
            <div>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-outline)', textTransform: 'uppercase', fontWeight: 700 }}>
                Chi nhánh
              </span>
              <div style={{ fontWeight: 600, fontSize: '0.875rem', marginTop: '2px', color: 'var(--color-on-surface)' }}>
                {reception.tenChiNhanh || `Chi nhánh #${reception.maChiNhanh}`}
              </div>
            </div>

            <div>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-outline)', textTransform: 'uppercase', fontWeight: 700 }}>
                Nhân viên tiếp nhận
              </span>
              <div style={{ fontWeight: 600, fontSize: '0.875rem', marginTop: '2px', color: 'var(--color-on-surface)' }}>
                {reception.tenNhanVienTiepNhan || `NV #${reception.maNhanVienTiepNhan}`}
              </div>
            </div>

            <div>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-outline)', textTransform: 'uppercase', fontWeight: 700 }}>
                Lịch hẹn liên kết
              </span>
              <div style={{ fontWeight: 700, fontSize: '0.875rem', marginTop: '2px', color: 'var(--color-primary)' }}>
                {reception.maDatLich ? `#${reception.maDatLich}` : 'Tiếp nhận trực tiếp'}
              </div>
            </div>
          </div>

          {/* Tình trạng ngoại thất */}
          <div>
            <label className="form-label" style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '6px' }}>
              <span className="material-symbols-outlined" style={{ fontSize: '18px', color: 'var(--color-primary)' }}>
                assignment_turned_in
              </span>
              Tình trạng ngoại thất & Phụ kiện lúc nhận xe
            </label>
            <div
              style={{
                backgroundColor: 'var(--color-surface-gray)',
                borderRadius: 'var(--radius-md)',
                padding: '12px 16px',
                border: '1px solid var(--color-border)',
                fontSize: '0.875rem',
                color: reception.tinhTrangNgoaiThat ? 'var(--color-on-surface)' : 'var(--color-outline)',
                lineHeight: 1.5,
              }}
            >
              {reception.tinhTrangNgoaiThat || 'Không có ghi nhận đặc biệt về ngoại thất.'}
            </div>
          </div>

          {/* Yêu cầu của khách hàng */}
          <div>
            <label className="form-label" style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '6px' }}>
              <span className="material-symbols-outlined" style={{ fontSize: '18px', color: 'var(--color-primary)' }}>
                format_quote
              </span>
              Yêu cầu của khách hàng
            </label>
            <div
              style={{
                backgroundColor: 'var(--color-surface-gray)',
                borderRadius: 'var(--radius-md)',
                padding: '12px 16px',
                border: '1px solid var(--color-border)',
                fontSize: '0.875rem',
                color: reception.yeuCauKhachHang ? 'var(--color-on-surface)' : 'var(--color-outline)',
                lineHeight: 1.5,
              }}
            >
              {reception.yeuCauKhachHang || 'Không có yêu cầu bổ sung.'}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="modal-footer" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            {!hasRepairOrder && onCreateRepairOrder && (
              <Button
                type="button"
                variant="primary"
                onClick={() => {
                  onClose();
                  onCreateRepairOrder(reception);
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '18px', marginRight: '6px' }}>
                  build
                </span>
                Tạo Lệnh Sửa Chữa
              </Button>
            )}
            {hasRepairOrder && (
              <span
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '6px',
                  fontSize: '0.85rem',
                  color: 'var(--color-success)',
                  fontWeight: 600,
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                  task_alt
                </span>
                Đã lập phiếu sửa chữa
              </span>
            )}
          </div>
          <Button type="button" variant="secondary" onClick={onClose}>
            Đóng
          </Button>
        </div>
      </div>
    </div>
  );
};
