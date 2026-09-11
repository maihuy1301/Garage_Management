import React, { useState, useEffect } from 'react';
import { CreateBrandRequest } from '@/types/brand-model.types';
import { Button } from '@/components/common/Button';

interface BrandFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CreateBrandRequest) => Promise<void>;
}

export const BrandFormModal: React.FC<BrandFormModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
}) => {
  const [tenHangXe, setTenHangXe] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [fieldError, setFieldError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setTenHangXe('');
      setErrorMessage(null);
      setFieldError(null);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const validate = (): boolean => {
    if (!tenHangXe.trim()) {
      setFieldError('Vui lòng nhập tên hãng xe');
      return false;
    }
    if (tenHangXe.trim().length > 50) {
      setFieldError('Tên hãng xe không vượt quá 50 ký tự');
      return false;
    }
    setFieldError(null);
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setIsSubmitting(true);
      setErrorMessage(null);
      await onSubmit({ tenHangXe: tenHangXe.trim() });
      onClose();
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'message' in err) {
        setErrorMessage((err as { message: string }).message);
      } else {
        setErrorMessage('Đã xảy ra lỗi khi tạo hãng xe.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="modal-header">
          <div className="modal-title">
            <span className="material-symbols-outlined" style={{ color: 'var(--color-primary)' }}>
              category
            </span>
            <span>Thêm Hãng Xe Mới</span>
          </div>
          <button className="modal-close-btn" onClick={onClose} aria-label="Đóng" disabled={isSubmitting}>
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        {/* Modal Body & Form */}
        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            {errorMessage && (
              <div className="alert alert-danger" style={{ marginBottom: '16px' }}>
                <span className="material-symbols-outlined">error</span>
                <span>{errorMessage}</span>
              </div>
            )}

            <div className="form-group">
              <label className="form-label" htmlFor="brand-name">
                Tên hãng xe <span style={{ color: 'var(--color-danger)' }}>*</span>
              </label>
              <input
                id="brand-name"
                type="text"
                className={`form-input ${fieldError ? 'border-error' : ''}`}
                placeholder="VD: Toyota, Honda, Hyundai, VinFast, Mercedes-Benz..."
                value={tenHangXe}
                onChange={(e) => setTenHangXe(e.target.value)}
                maxLength={50}
                disabled={isSubmitting}
                autoFocus
              />
              {fieldError && (
                <span style={{ color: 'var(--color-danger)', fontSize: '0.8rem', marginTop: '2px' }}>
                  {fieldError}
                </span>
              )}
            </div>
          </div>

          {/* Modal Footer */}
          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={onClose} disabled={isSubmitting}>
              Hủy
            </button>
            <Button variant="primary" type="submit" isLoading={isSubmitting}>
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                add_circle
              </span>
              <span>Tạo Hãng Xe</span>
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
