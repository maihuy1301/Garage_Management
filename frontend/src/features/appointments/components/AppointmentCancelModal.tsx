import React, { useState } from 'react';
import { AppointmentResponse } from '@/types/appointment.types';
import { appointmentService } from '../services/appointment.service';

interface AppointmentCancelModalProps {
  appointment: AppointmentResponse | null;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (updatedAppointment: AppointmentResponse) => void;
}

export const AppointmentCancelModal: React.FC<AppointmentCancelModalProps> = ({
  appointment,
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen || !appointment) return null;

  const handleConfirmCancel = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await appointmentService.cancelAppointment(appointment.maDatLich);
      onSuccess(res);
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Không thể hủy lịch hẹn. Vui lòng thử lại.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container small" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="modal-header">
          <div className="modal-title" style={{ color: 'var(--color-danger)' }}>
            <span className="material-symbols-outlined">warning</span>
            <span>Hủy Lịch Hẹn #{appointment.maDatLich}</span>
          </div>
          <button className="modal-close-btn" onClick={onClose} aria-label="Đóng">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        {/* Body */}
        <div className="modal-body" style={{ textAlign: 'center', padding: '24px 20px' }}>
          <div
            style={{
              width: '56px',
              height: '56px',
              borderRadius: 'var(--radius-full)',
              backgroundColor: 'var(--color-danger-bg)',
              color: 'var(--color-danger)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 16px',
            }}
          >
            <span className="material-symbols-outlined" style={{ fontSize: '32px' }}>
              cancel
            </span>
          </div>

          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--color-on-surface)', marginBottom: '8px' }}>
            Xác nhận hủy lịch hẹn?
          </h3>

          <p style={{ fontSize: '0.875rem', color: 'var(--color-on-surface-variant)', lineHeight: 1.5 }}>
            Bạn có chắc chắn muốn hủy lịch hẹn cho xe{' '}
            <strong style={{ color: 'var(--color-on-surface)' }}>{appointment.bienSoXe || 'này'}</strong> vào thời
            điểm{' '}
            <strong style={{ color: 'var(--color-primary)' }}>
              {new Date(appointment.thoiGianHen).toLocaleString('vi-VN')}
            </strong>{' '}
            không?
          </p>

          <p style={{ fontSize: '0.75rem', color: 'var(--color-danger)', marginTop: '12px' }}>
            * Trạng thái lịch hẹn sẽ được chuyển sang "Đã hủy" và không thể hoàn tác.
          </p>

          {error && (
            <div className="alert alert-danger" style={{ marginTop: '16px', textAlign: 'left' }}>
              <span className="material-symbols-outlined">error</span>
              <span>{error}</span>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-secondary"
            disabled={loading}
            onClick={onClose}
          >
            Đóng
          </button>
          <button
            type="button"
            className="btn btn-danger"
            disabled={loading}
            onClick={handleConfirmCancel}
          >
            {loading && (
              <span className="material-symbols-outlined animate-spin" style={{ fontSize: '18px' }}>
                progress_activity
              </span>
            )}
            <span>{loading ? 'Đang xử lý...' : 'Xác nhận hủy'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
