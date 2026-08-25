import React, { useState, useEffect } from 'react';
import {
  VehicleResponse,
  CreateVehicleRequest,
  UpdateVehicleRequest,
} from '@/types/vehicle.types';
import { CustomerResponse } from '@/types/customer.types';
import { customerService } from '@/features/customers/services/customer.service';
import { normalizeApiError } from '@/lib/api/error-handler';

interface VehicleFormModalProps {
  isOpen: boolean;
  isAdmin: boolean;
  initialData: VehicleResponse | null;
  onClose: () => void;
  onSubmit: (data: CreateVehicleRequest | UpdateVehicleRequest) => Promise<void>;
}

export const VehicleFormModal: React.FC<VehicleFormModalProps> = ({
  isOpen,
  isAdmin,
  initialData,
  onClose,
  onSubmit,
}) => {
  const isEditMode = Boolean(initialData);

  // Form State
  const [maKhachHang, setMaKhachHang] = useState<number | ''>('');
  const [bienSo, setBienSo] = useState<string>('');
  const [hangXe, setHangXe] = useState<string>('');
  const [model, setModel] = useState<string>('');
  const [namSanXuat, setNamSanXuat] = useState<number | ''>('');
  const [mauXe, setMauXe] = useState<string>('');
  const [soVIN, setSoVIN] = useState<string>('');
  const [soKmHienTai, setSoKmHienTai] = useState<number | ''>('');

  // UI State
  const [customers, setCustomers] = useState<CustomerResponse[]>([]);
  const [isLoadingCustomers, setIsLoadingCustomers] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  // Popular car brands for quick suggestion
  const commonBrands = [
    'Toyota',
    'Honda',
    'Hyundai',
    'Mazda',
    'Kia',
    'Ford',
    'Mitsubishi',
    'VinFast',
    'Mercedes-Benz',
    'BMW',
    'Audi',
    'Lexus',
    'Nissan',
    'Suzuki',
    'Porsche',
  ];

  // Fetch customers if Admin creates a vehicle
  useEffect(() => {
    if (!isOpen) return;

    if (isAdmin && !isEditMode) {
      const loadCustomers = async () => {
        try {
          setIsLoadingCustomers(true);
          const data = await customerService.getAll();
          setCustomers(data.filter((c) => c.trangThai !== false));
        } catch (err) {
          console.error('Failed to load customer list for vehicle creation:', err);
        } finally {
          setIsLoadingCustomers(false);
        }
      };
      loadCustomers();
    }
  }, [isOpen, isAdmin, isEditMode]);

  // Sync initialData when modal opens
  useEffect(() => {
    if (isOpen) {
      setErrorMessage(null);
      setValidationErrors({});

      if (initialData) {
        setBienSo(initialData.bienSo || '');
        setHangXe(initialData.hangXe || '');
        setModel(initialData.model || '');
        setNamSanXuat(initialData.namSanXuat != null ? initialData.namSanXuat : '');
        setMauXe(initialData.mauXe || '');
        setSoVIN(initialData.soVIN || '');
        setSoKmHienTai(initialData.soKmHienTai != null ? initialData.soKmHienTai : '');
        setMaKhachHang(initialData.maKhachHang || '');
      } else {
        setBienSo('');
        setHangXe('');
        setModel('');
        setNamSanXuat(new Date().getFullYear());
        setMauXe('');
        setSoVIN('');
        setSoKmHienTai(0);
        setMaKhachHang('');
      }
    }
  }, [isOpen, initialData]);

  if (!isOpen) return null;

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!isEditMode) {
      if (!bienSo.trim()) {
        errors.bienSo = 'Vui lòng nhập biển số xe';
      } else if (bienSo.trim().length > 20) {
        errors.bienSo = 'Biển số không vượt quá 20 ký tự';
      }

      if (isAdmin && !maKhachHang) {
        errors.maKhachHang = 'Quản trị viên phải chọn khách hàng sở hữu xe';
      }
    }

    if (namSanXuat !== '') {
      const currentYear = new Date().getFullYear();
      if (namSanXuat < 1950 || namSanXuat > currentYear + 1) {
        errors.namSanXuat = `Năm sản xuất không hợp lệ (1950 - ${currentYear + 1})`;
      }
    }

    if (soKmHienTai !== '' && Number(soKmHienTai) < 0) {
      errors.soKmHienTai = 'Số KM hiện tại không thể là số âm';
    }

    if (hangXe.length > 50) {
      errors.hangXe = 'Tên hãng xe không được vượt quá 50 ký tự';
    }

    if (model.length > 100) {
      errors.model = 'Tên model xe không được vượt quá 100 ký tự';
    }

    if (mauXe.length > 50) {
      errors.mauXe = 'Màu xe không được vượt quá 50 ký tự';
    }

    if (soVIN.length > 50) {
      errors.soVIN = 'Số VIN không được vượt quá 50 ký tự';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setIsSubmitting(true);
      setErrorMessage(null);

      if (isEditMode) {
        const payload: UpdateVehicleRequest = {
          hangXe: hangXe.trim() || undefined,
          model: model.trim() || undefined,
          namSanXuat: namSanXuat !== '' ? Number(namSanXuat) : undefined,
          mauXe: mauXe.trim() || undefined,
          soVIN: soVIN.trim() || undefined,
          soKmHienTai: soKmHienTai !== '' ? Number(soKmHienTai) : 0,
        };
        await onSubmit(payload);
      } else {
        const payload: CreateVehicleRequest = {
          maKhachHang: isAdmin && maKhachHang !== '' ? Number(maKhachHang) : undefined,
          bienSo: bienSo.trim().toUpperCase(),
          hangXe: hangXe.trim() || undefined,
          model: model.trim() || undefined,
          namSanXuat: namSanXuat !== '' ? Number(namSanXuat) : undefined,
          mauXe: mauXe.trim() || undefined,
          soVIN: soVIN.trim() || undefined,
          soKmHienTai: soKmHienTai !== '' ? Number(soKmHienTai) : 0,
        };
        await onSubmit(payload);
      }
      onClose();
    } catch (err: unknown) {
      setErrorMessage(normalizeApiError(err).message);
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
              {isEditMode ? 'edit_note' : 'add_circle'}
            </span>
            <span>{isEditMode ? `Cập Nhật Phương Tiện: ${initialData?.bienSo}` : 'Đăng Ký Phương Tiện Mới'}</span>
          </div>
          <button className="modal-close-btn" onClick={onClose} aria-label="Đóng" disabled={isSubmitting}>
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        {/* Modal Body & Form */}
        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            {/* Error Banner */}
            {errorMessage && (
              <div
                style={{
                  padding: '12px 16px',
                  backgroundColor: 'var(--color-error-container)',
                  color: 'var(--color-on-error-container)',
                  borderRadius: 'var(--radius-md)',
                  marginBottom: '16px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  fontSize: '0.875rem',
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                  error
                </span>
                <span>{errorMessage}</span>
              </div>
            )}

            {/* Customer select for ADMIN in Create mode */}
            {isAdmin && !isEditMode && (
              <div className="form-group" style={{ marginBottom: '16px' }}>
                <label className="form-label" style={{ fontWeight: 600 }}>
                  Chủ sở hữu xe (Khách hàng) <span style={{ color: 'var(--color-danger-red)' }}>*</span>
                </label>
                {isLoadingCustomers ? (
                  <div style={{ fontSize: '0.85rem', color: 'var(--color-outline)' }}>Đang tải danh sách khách hàng...</div>
                ) : (
                  <select
                    className={`form-input ${validationErrors.maKhachHang ? 'error' : ''}`}
                    value={maKhachHang}
                    onChange={(e) => setMaKhachHang(e.target.value ? Number(e.target.value) : '')}
                    disabled={isSubmitting}
                  >
                    <option value="">-- Chọn khách hàng chủ xe --</option>
                    {customers.map((c) => (
                      <option key={c.maKhachHang} value={c.maKhachHang}>
                        {c.maKhachHangCode} — {c.hoTen} ({c.soDienThoai || 'Chưa có SĐT'})
                      </option>
                    ))}
                  </select>
                )}
                {validationErrors.maKhachHang && (
                  <div style={{ color: 'var(--color-danger-red)', fontSize: '0.75rem', marginTop: '4px' }}>
                    {validationErrors.maKhachHang}
                  </div>
                )}
              </div>
            )}

            {/* Notification banner for Customer */}
            {!isAdmin && !isEditMode && (
              <div
                style={{
                  padding: '10px 14px',
                  backgroundColor: 'var(--color-surface-container)',
                  color: 'var(--color-primary)',
                  borderRadius: 'var(--radius-md)',
                  marginBottom: '16px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  fontSize: '0.825rem',
                }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                  info
                </span>
                <span>Phương tiện đăng ký mới sẽ tự động được liên kết với tài khoản khách hàng của bạn.</span>
              </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
              {/* Biển số xe */}
              <div className="form-group">
                <label className="form-label" style={{ fontWeight: 600 }}>
                  Biển số xe {!isEditMode && <span style={{ color: 'var(--color-danger-red)' }}>*</span>}
                </label>
                <input
                  type="text"
                  className={`form-input ${validationErrors.bienSo ? 'error' : ''}`}
                  placeholder="VD: 51A-12345"
                  value={bienSo}
                  onChange={(e) => setBienSo(e.target.value.toUpperCase())}
                  disabled={isEditMode || isSubmitting}
                  style={isEditMode ? { backgroundColor: 'var(--color-surface-container-low)', cursor: 'not-allowed' } : {}}
                />
                {validationErrors.bienSo && (
                  <div style={{ color: 'var(--color-danger-red)', fontSize: '0.75rem', marginTop: '4px' }}>
                    {validationErrors.bienSo}
                  </div>
                )}
              </div>

              {/* Hãng xe */}
              <div className="form-group">
                <label className="form-label" style={{ fontWeight: 600 }}>
                  Hãng xe
                </label>
                <input
                  type="text"
                  list="car-brands-list"
                  className={`form-input ${validationErrors.hangXe ? 'error' : ''}`}
                  placeholder="VD: Toyota, Mazda, Honda..."
                  value={hangXe}
                  onChange={(e) => setHangXe(e.target.value)}
                  disabled={isSubmitting}
                />
                <datalist id="car-brands-list">
                  {commonBrands.map((b) => (
                    <option key={b} value={b} />
                  ))}
                </datalist>
                {validationErrors.hangXe && (
                  <div style={{ color: 'var(--color-danger-red)', fontSize: '0.75rem', marginTop: '4px' }}>
                    {validationErrors.hangXe}
                  </div>
                )}
              </div>

              {/* Dòng xe (Model) */}
              <div className="form-group">
                <label className="form-label" style={{ fontWeight: 600 }}>
                  Dòng xe (Model)
                </label>
                <input
                  type="text"
                  className={`form-input ${validationErrors.model ? 'error' : ''}`}
                  placeholder="VD: Vios, CX-5, City..."
                  value={model}
                  onChange={(e) => setModel(e.target.value)}
                  disabled={isSubmitting}
                />
                {validationErrors.model && (
                  <div style={{ color: 'var(--color-danger-red)', fontSize: '0.75rem', marginTop: '4px' }}>
                    {validationErrors.model}
                  </div>
                )}
              </div>

              {/* Năm sản xuất */}
              <div className="form-group">
                <label className="form-label" style={{ fontWeight: 600 }}>
                  Năm sản xuất
                </label>
                <input
                  type="number"
                  className={`form-input ${validationErrors.namSanXuat ? 'error' : ''}`}
                  placeholder="VD: 2022"
                  value={namSanXuat}
                  onChange={(e) => setNamSanXuat(e.target.value ? Number(e.target.value) : '')}
                  disabled={isSubmitting}
                  min={1950}
                  max={new Date().getFullYear() + 1}
                />
                {validationErrors.namSanXuat && (
                  <div style={{ color: 'var(--color-danger-red)', fontSize: '0.75rem', marginTop: '4px' }}>
                    {validationErrors.namSanXuat}
                  </div>
                )}
              </div>

              {/* Màu sắc */}
              <div className="form-group">
                <label className="form-label" style={{ fontWeight: 600 }}>
                  Màu sơn xe
                </label>
                <input
                  type="text"
                  className={`form-input ${validationErrors.mauXe ? 'error' : ''}`}
                  placeholder="VD: Trắng, Đen, Đỏ, Bạc..."
                  value={mauXe}
                  onChange={(e) => setMauXe(e.target.value)}
                  disabled={isSubmitting}
                />
                {validationErrors.mauXe && (
                  <div style={{ color: 'var(--color-danger-red)', fontSize: '0.75rem', marginTop: '4px' }}>
                    {validationErrors.mauXe}
                  </div>
                )}
              </div>

              {/* Số KM hiện tại (ODO) */}
              <div className="form-group">
                <label className="form-label" style={{ fontWeight: 600 }}>
                  Số KM hiện tại (ODO)
                </label>
                <input
                  type="number"
                  className={`form-input ${validationErrors.soKmHienTai ? 'error' : ''}`}
                  placeholder="VD: 25000"
                  value={soKmHienTai}
                  onChange={(e) => setSoKmHienTai(e.target.value ? Number(e.target.value) : '')}
                  disabled={isSubmitting}
                  min={0}
                />
                {validationErrors.soKmHienTai && (
                  <div style={{ color: 'var(--color-danger-red)', fontSize: '0.75rem', marginTop: '4px' }}>
                    {validationErrors.soKmHienTai}
                  </div>
                )}
              </div>
            </div>

            {/* Số VIN */}
            <div className="form-group" style={{ marginTop: '16px' }}>
              <label className="form-label" style={{ fontWeight: 600 }}>
                Số khung / Số VIN (Vehicle Identification Number)
              </label>
              <input
                type="text"
                className={`form-input ${validationErrors.soVIN ? 'error' : ''}`}
                placeholder="VD: 1HGCR2F83HA000000"
                value={soVIN}
                onChange={(e) => setSoVIN(e.target.value.toUpperCase())}
                disabled={isSubmitting}
                style={{ fontFamily: 'monospace' }}
              />
              {validationErrors.soVIN && (
                <div style={{ color: 'var(--color-danger-red)', fontSize: '0.75rem', marginTop: '4px' }}>
                  {validationErrors.soVIN}
                </div>
              )}
            </div>
          </div>

          {/* Modal Footer */}
          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={onClose} disabled={isSubmitting}>
              Hủy bỏ
            </button>
            <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
              {isSubmitting ? (
                <>
                  <span className="spinner small" />
                  <span>Đang lưu...</span>
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                    save
                  </span>
                  <span>{isEditMode ? 'Lưu thay đổi' : 'Đăng ký xe'}</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
