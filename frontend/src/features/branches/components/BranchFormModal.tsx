import React, { useState, useEffect } from 'react';
import {
  BranchResponse,
  CreateBranchRequest,
  UpdateBranchRequest,
} from '@/types/branch.types';
import { Button } from '@/components/common/Button';

interface BranchFormModalProps {
  branch: BranchResponse | null;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (
    payload: CreateBranchRequest | UpdateBranchRequest,
    isEdit: boolean
  ) => Promise<void>;
}

export const BranchFormModal: React.FC<BranchFormModalProps> = ({
  branch,
  isOpen,
  onClose,
  onSubmit,
}) => {
  const isEdit = !!branch;

  const [tenChiNhanh, setTenChiNhanh] = useState<string>('');
  const [diaChi, setDiaChi] = useState<string>('');
  const [soDienThoai, setSoDienThoai] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [trangThai, setTrangThai] = useState<boolean>(true);

  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [serverError, setServerError] = useState<string | null>(null);

  useEffect(() => {
    if (branch) {
      setTenChiNhanh(branch.tenChiNhanh || '');
      setDiaChi(branch.diaChi || '');
      setSoDienThoai(branch.soDienThoai || '');
      setEmail(branch.email || '');
      setTrangThai(branch.trangThai ?? true);
    } else {
      setTenChiNhanh('');
      setDiaChi('');
      setSoDienThoai('');
      setEmail('');
      setTrangThai(true);
    }
    setFormErrors({});
    setServerError(null);
  }, [branch, isOpen]);

  if (!isOpen) return null;

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!tenChiNhanh.trim()) {
      errors.tenChiNhanh = 'Tên chi nhánh không được để trống';
    } else if (tenChiNhanh.length > 150) {
      errors.tenChiNhanh = 'Tên chi nhánh tối đa 150 ký tự';
    }

    if (!diaChi.trim()) {
      errors.diaChi = 'Địa chỉ không được để trống';
    } else if (diaChi.length > 255) {
      errors.diaChi = 'Địa chỉ tối đa 255 ký tự';
    }

    if (soDienThoai && soDienThoai.length > 20) {
      errors.soDienThoai = 'Số điện thoại tối đa 20 ký tự';
    }

    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      errors.email = 'Email không hợp lệ';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setIsSubmitting(true);
      setServerError(null);

      const payload: CreateBranchRequest | UpdateBranchRequest = {
        tenChiNhanh: tenChiNhanh.trim(),
        diaChi: diaChi.trim(),
        soDienThoai: soDienThoai.trim() || undefined,
        email: email.trim() || undefined,
        trangThai,
      };

      await onSubmit(payload, isEdit);
      onClose();
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'message' in err) {
        setServerError((err as { message: string }).message);
      } else {
        setServerError('Không thể lưu thông tin chi nhánh. Vui lòng thử lại.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title">
            <span className="material-symbols-outlined" style={{ color: 'var(--primary)' }}>
              {isEdit ? 'edit_location_alt' : 'add_business'}
            </span>
            <span>{isEdit ? `Chỉnh sửa chi nhánh #${branch?.maChiNhanh}` : 'Thêm chi nhánh mới'}</span>
          </div>
          <button className="modal-close-btn" onClick={onClose} aria-label="Đóng">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            {serverError && (
              <div className="alert alert-danger" style={{ marginBottom: '16px' }}>
                <span className="material-symbols-outlined">error</span>
                <span>{serverError}</span>
              </div>
            )}

            <div className="form-group" style={{ marginBottom: '16px' }}>
              <label className="form-label" htmlFor="tenChiNhanh">
                Tên chi nhánh <span style={{ color: 'var(--danger)' }}>*</span>
              </label>
              <input
                id="tenChiNhanh"
                type="text"
                className={`form-input ${formErrors.tenChiNhanh ? 'input-error' : ''}`}
                placeholder="VD: Garage Central Chi Nhánh Quận 1"
                value={tenChiNhanh}
                onChange={(e) => setTenChiNhanh(e.target.value)}
                disabled={isSubmitting}
              />
              {formErrors.tenChiNhanh && (
                <span className="form-error" style={{ color: 'var(--danger)', fontSize: '0.8rem' }}>
                  {formErrors.tenChiNhanh}
                </span>
              )}
            </div>

            <div className="form-group" style={{ marginBottom: '16px' }}>
              <label className="form-label" htmlFor="diaChi">
                Địa chỉ <span style={{ color: 'var(--danger)' }}>*</span>
              </label>
              <input
                id="diaChi"
                type="text"
                className={`form-input ${formErrors.diaChi ? 'input-error' : ''}`}
                placeholder="VD: 123 Nguyễn Văn Cừ, Phường 4, Quận 5, TP.HCM"
                value={diaChi}
                onChange={(e) => setDiaChi(e.target.value)}
                disabled={isSubmitting}
              />
              {formErrors.diaChi && (
                <span className="form-error" style={{ color: 'var(--danger)', fontSize: '0.8rem' }}>
                  {formErrors.diaChi}
                </span>
              )}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '16px' }}>
              <div className="form-group">
                <label className="form-label" htmlFor="soDienThoai">
                  Số điện thoại
                </label>
                <input
                  id="soDienThoai"
                  type="text"
                  className={`form-input ${formErrors.soDienThoai ? 'input-error' : ''}`}
                  placeholder="VD: 02838123456"
                  value={soDienThoai}
                  onChange={(e) => setSoDienThoai(e.target.value)}
                  disabled={isSubmitting}
                />
                {formErrors.soDienThoai && (
                  <span className="form-error" style={{ color: 'var(--danger)', fontSize: '0.8rem' }}>
                    {formErrors.soDienThoai}
                  </span>
                )}
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="email">
                  Email
                </label>
                <input
                  id="email"
                  type="email"
                  className={`form-input ${formErrors.email ? 'input-error' : ''}`}
                  placeholder="VD: branch1@garage.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={isSubmitting}
                />
                {formErrors.email && (
                  <span className="form-error" style={{ color: 'var(--danger)', fontSize: '0.8rem' }}>
                    {formErrors.email}
                  </span>
                )}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={trangThai}
                  onChange={(e) => setTrangThai(e.target.checked)}
                  disabled={isSubmitting}
                  style={{ width: '16px', height: '16px' }}
                />
                <span>Kích hoạt chi nhánh ngay sau khi lưu</span>
              </label>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={onClose} disabled={isSubmitting}>
              Hủy
            </button>
            <Button variant="primary" type="submit" isLoading={isSubmitting}>
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                {isEdit ? 'save' : 'add'}
              </span>
              <span>{isEdit ? 'Lưu thay đổi' : 'Tạo chi nhánh'}</span>
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
