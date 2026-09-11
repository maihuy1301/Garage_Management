import React, { useState, useEffect } from 'react';
import { BrandResponse, CreateModelRequest } from '@/types/brand-model.types';
import { Button } from '@/components/common/Button';

interface ModelFormModalProps {
  isOpen: boolean;
  brandsList: BrandResponse[];
  selectedBrandId?: number;
  onClose: () => void;
  onSubmit: (brandId: number, data: CreateModelRequest) => Promise<void>;
}

export const ModelFormModal: React.FC<ModelFormModalProps> = ({
  isOpen,
  brandsList,
  selectedBrandId,
  onClose,
  onSubmit,
}) => {
  const [maHangXe, setMaHangXe] = useState<number | ''>('');
  const [tenModel, setTenModel] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (isOpen) {
      setMaHangXe(selectedBrandId || (brandsList.length > 0 ? brandsList[0].maHangXe : ''));
      setTenModel('');
      setErrorMessage(null);
      setFieldErrors({});
    }
  }, [isOpen, selectedBrandId, brandsList]);

  if (!isOpen) return null;

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!maHangXe) {
      errors.maHangXe = 'Vui lòng chọn hãng xe';
    }

    if (!tenModel.trim()) {
      errors.tenModel = 'Vui lòng nhập tên model xe';
    } else if (tenModel.trim().length > 100) {
      errors.tenModel = 'Tên model xe không vượt quá 100 ký tự';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setIsSubmitting(true);
      setErrorMessage(null);
      await onSubmit(Number(maHangXe), { tenModel: tenModel.trim() });
      onClose();
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'message' in err) {
        setErrorMessage((err as { message: string }).message);
      } else {
        setErrorMessage('Đã xảy ra lỗi khi tạo model xe.');
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
              directions_car
            </span>
            <span>Thêm Model Xe Mới</span>
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

            {/* Hãng xe select */}
            <div className="form-group" style={{ marginBottom: '16px' }}>
              <label className="form-label" htmlFor="model-brand-select">
                Hãng xe <span style={{ color: 'var(--color-danger)' }}>*</span>
              </label>
              <select
                id="model-brand-select"
                className={`form-input ${fieldErrors.maHangXe ? 'border-error' : ''}`}
                value={maHangXe}
                onChange={(e) => setMaHangXe(e.target.value ? Number(e.target.value) : '')}
                disabled={isSubmitting}
              >
                <option value="">-- Chọn hãng xe --</option>
                {brandsList.map((brand) => (
                  <option key={brand.maHangXe} value={brand.maHangXe}>
                    {brand.tenHangXe}
                  </option>
                ))}
              </select>
              {fieldErrors.maHangXe && (
                <span style={{ color: 'var(--color-danger)', fontSize: '0.8rem', marginTop: '2px' }}>
                  {fieldErrors.maHangXe}
                </span>
              )}
            </div>

            {/* Tên Model xe */}
            <div className="form-group">
              <label className="form-label" htmlFor="model-name">
                Tên Model xe <span style={{ color: 'var(--color-danger)' }}>*</span>
              </label>
              <input
                id="model-name"
                type="text"
                className={`form-input ${fieldErrors.tenModel ? 'border-error' : ''}`}
                placeholder="VD: Camry, Vios, Civic, CR-V, Ranger, VF8..."
                value={tenModel}
                onChange={(e) => setTenModel(e.target.value)}
                maxLength={100}
                disabled={isSubmitting}
                autoFocus
              />
              {fieldErrors.tenModel && (
                <span style={{ color: 'var(--color-danger)', fontSize: '0.8rem', marginTop: '2px' }}>
                  {fieldErrors.tenModel}
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
              <span>Tạo Model Xe</span>
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
