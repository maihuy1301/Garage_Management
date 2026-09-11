import React from 'react';
import { AppointmentResponse, AppointmentStatusLabels, AppointmentStatusTone } from '@/types/appointment.types';

interface AppointmentDetailModalProps {
  appointment: AppointmentResponse | null;
  isOpen: boolean;
  onClose: () => void;
  onOpenCancelModal?: (appointment: AppointmentResponse) => void;
  onConfirm?: (appointment: AppointmentResponse) => void;
  onReceive?: (appointment: AppointmentResponse) => void;
  canCancel?: boolean;
  canManageStatus?: boolean;
}

export const AppointmentDetailModal: React.FC<AppointmentDetailModalProps> = ({
  appointment,
  isOpen,
  onClose,
  onOpenCancelModal,
  onConfirm,
  onReceive,
  canCancel = false,
  canManageStatus = false,
}) => {
  if (!isOpen || !appointment) return null;

  const formatDateTime = (dateStr?: string) => {
    if (!dateStr) return '—';
    try {
      const d = new Date(dateStr);
      return d.toLocaleString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  const tone = AppointmentStatusTone[appointment.trangThai] || 'gray';
  const statusLabel = AppointmentStatusLabels[appointment.trangThai] || appointment.trangThai;
  const statusClass =
    tone === 'warning'
      ? 'warning'
      : tone === 'success'
      ? 'success'
      : tone === 'danger'
      ? 'danger'
      : tone === 'primary' || tone === 'info'
      ? 'primary'
      : '';

  const isCancellable =
    canCancel && (appointment.trangThai === 'CHO_XAC_NHAN' || appointment.trangThai === 'DA_XAC_NHAN');

  const isConfirmable =
    canManageStatus && appointment.trangThai === 'CHO_XAC_NHAN' && Boolean(onConfirm);

  const isReceivable =
    canManageStatus &&
    appointment.trangThai === 'DA_XAC_NHAN' &&
    Boolean(onReceive);

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="modal-header">
          <div className="modal-title">
            <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>
              calendar_month
            </span>
            <span>Chi Tiết Lịch Hẹn #{appointment.maDatLich}</span>
          </div>
          <button className="modal-close-btn" onClick={onClose} aria-label="Đóng">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        {/* Body */}
        <div className="modal-body">
          {/* Top Banner */}
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
              <span className="material-symbols-outlined" style={{ fontSize: '28px' }}>
                event
              </span>
            </div>

            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
                <span
                  style={{
                    fontFamily: 'monospace',
                    fontSize: '1.1rem',
                    fontWeight: 700,
                    color: 'var(--color-primary)',
                    backgroundColor: '#ffffff',
                    padding: '2px 8px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--color-outline-variant)',
                  }}
                >
                  #{appointment.maDatLich}
                </span>
                <span className={`status-badge ${statusClass}`}>{statusLabel}</span>
              </div>
              <p style={{ fontSize: '0.8rem', color: 'var(--color-on-surface-variant)', marginTop: '4px' }}>
                Ngày tạo yêu cầu: {formatDateTime(appointment.ngayDat)}
              </p>
            </div>
          </div>

          {/* Section: Thông tin lịch & địa điểm */}
          <h4
            style={{
              fontSize: '0.85rem',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
              color: 'var(--color-outline)',
              marginBottom: '10px',
            }}
          >
            Thời Gian & Chi Nhánh
          </h4>

          <div className="detail-grid">
            <div className="detail-item">
              <div className="detail-label">Thời Gian Hẹn</div>
              <div
                className="detail-value"
                style={{ color: 'var(--color-primary-container)', fontSize: '1rem', fontWeight: 700 }}
              >
                {formatDateTime(appointment.thoiGianHen)}
              </div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Chi Nhánh Tiếp Nhận</div>
              <div className="detail-value">
                {appointment.tenChiNhanh || `Chi nhánh #${appointment.maChiNhanh}`}
              </div>
            </div>
          </div>

          {/* Section: Khách hàng & Phương tiện */}
          <h4
            style={{
              fontSize: '0.85rem',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
              color: 'var(--color-outline)',
              marginBottom: '10px',
            }}
          >
            Khách Hàng & Phương Tiện
          </h4>

          <div className="detail-grid">
            <div className="detail-item">
              <div className="detail-label">Khách Hàng</div>
              <div className="detail-value">{appointment.tenKhachHang || '—'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Số Điện Thoại</div>
              <div className="detail-value">{appointment.soDienThoaiKhachHang || '—'}</div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Biển Số Xe</div>
              <div className="detail-value">
                <span
                  style={{
                    fontFamily: 'monospace',
                    fontWeight: 700,
                    backgroundColor: '#fef3c7',
                    color: '#92400e',
                    padding: '2px 8px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid #fde68a',
                  }}
                >
                  {appointment.bienSoXe || '—'}
                </span>
              </div>
            </div>

            <div className="detail-item">
              <div className="detail-label">Hãng & Dòng Xe</div>
              <div className="detail-value">
                {appointment.hangXe || '—'} {appointment.modelXe || ''}
              </div>
            </div>
          </div>

          {/* Section: Ghi chú */}
          <h4
            style={{
              fontSize: '0.85rem',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
              color: 'var(--color-outline)',
              marginBottom: '10px',
            }}
          >
            Yêu Cầu & Ghi Chú Dịch Vụ
          </h4>

          <div
            style={{
              padding: '12px 16px',
              backgroundColor: 'var(--color-surface-gray)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--color-border)',
              fontSize: '0.875rem',
              color: 'var(--color-on-surface)',
              lineHeight: 1.6,
              whiteSpace: 'pre-wrap',
            }}
          >
            {appointment.ghiChu || 'Không có ghi chú thêm từ khách hàng.'}
          </div>
        </div>

        {/* Footer */}
        <div className="modal-footer" style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '8px' }}>
          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            {/* Xác nhận */}
            {isConfirmable && onConfirm && (
              <button
                type="button"
                className="btn btn-primary"
                style={{ backgroundColor: 'var(--color-success)', borderColor: 'var(--color-success)' }}
                onClick={() => {
                  onClose();
                  onConfirm(appointment);
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                  check_circle
                </span>
                <span>Xác nhận lịch</span>
              </button>
            )}

            {/* Tiếp nhận */}
            {isReceivable && onReceive && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => {
                  onClose();
                  onReceive(appointment);
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                  car_repair
                </span>
                <span>Tiếp nhận xe</span>
              </button>
            )}

            {/* Hủy */}
            {isCancellable && onOpenCancelModal && (
              <button
                type="button"
                className="btn btn-danger"
                onClick={() => {
                  onClose();
                  onOpenCancelModal(appointment);
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                  cancel
                </span>
                <span>Hủy lịch</span>
              </button>
            )}
          </div>

          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
};
