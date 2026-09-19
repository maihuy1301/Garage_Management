import React, { useState } from 'react';
import { AssignmentResponse } from '@/types/repair-order.types';
import { assignmentService } from '../services/assignment.service';
import { Button } from '@/components/common/Button';

interface RejectAssignmentModalProps {
  isOpen: boolean;
  assignment: AssignmentResponse | null;
  onClose: () => void;
  onSuccess: () => void;
}

export const RejectAssignmentModal: React.FC<RejectAssignmentModalProps> = ({
  isOpen,
  assignment,
  onClose,
  onSuccess,
}) => {
  const [ghiChu, setGhiChu] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen || !assignment) return null;

  const handleReject = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);
      await assignmentService.rejectAssignment(
        assignment.maPhieuSuaChua,
        assignment.maPhanCong,
        { ghiChu: ghiChu.trim() || undefined }
      );
      onSuccess();
      onClose();
    } catch (err: any) {
      console.error('Lỗi khi từ chối phân công:', err);
      const msg = err.response?.data?.message || err.message || 'Không thể từ chối phân công. Vui lòng thử lại.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className="modal-container small"
        style={{ maxWidth: '480px' }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <div className="modal-title" style={{ color: 'var(--color-danger)' }}>
            <span className="material-symbols-outlined" style={{ fontSize: '24px' }}>
              cancel
            </span>
            <span>Từ Chối Phân Công</span>
          </div>
          <button type="button" className="modal-close-btn" onClick={onClose}>
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        <form onSubmit={handleReject}>
          <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--color-on-surface)', lineHeight: '1.5' }}>
              Bạn có chắc chắn muốn từ chối phân công kỹ thuật viên{' '}
              <strong>{assignment.tenNhanVien || `#${assignment.maNhanVien}`}</strong> cho phiếu sửa chữa{' '}
              <strong>#{assignment.maPhieuSuaChua}</strong>?
            </p>

            {error && (
              <div className="alert alert-danger" style={{ margin: 0 }}>
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                  error
                </span>
                <span>{error}</span>
              </div>
            )}

            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label">
                Lý do từ chối (tùy chọn)
              </label>
              <textarea
                className="filter-select"
                style={{
                  width: '100%',
                  minHeight: '80px',
                  padding: '10px 12px',
                  fontFamily: 'inherit',
                  backgroundColor: '#ffffff',
                  resize: 'vertical',
                }}
                placeholder="Nhập lý do từ chối để nhân viên tạo phân công nắm được thông tin..."
                value={ghiChu}
                onChange={(e) => setGhiChu(e.target.value)}
                disabled={loading}
              />
            </div>
          </div>

          <div className="modal-footer">
            <Button variant="secondary" type="button" onClick={onClose} disabled={loading} style={{ padding: '8px 16px', minHeight: 'auto' }}>
              Đóng
            </Button>
            <Button
              variant="danger"
              type="submit"
              disabled={loading}
              style={{ padding: '8px 16px', minHeight: 'auto' }}
            >
              {loading ? 'Đang xử lý...' : 'Xác nhận từ chối'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
