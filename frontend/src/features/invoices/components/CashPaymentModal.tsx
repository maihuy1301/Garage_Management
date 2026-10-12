import React, { useState, useEffect, useMemo } from 'react';
import { Button } from '@/components/common/Button';
import { invoiceService } from '../services/invoice.service';
import { InvoiceResponse } from '@/types/invoice.types';

interface CashPaymentModalProps {
  isOpen: boolean;
  invoice: InvoiceResponse | null;
  onClose: () => void;
  onSuccess: () => void;
}

export const CashPaymentModal: React.FC<CashPaymentModalProps> = ({
  isOpen,
  invoice,
  onClose,
  onSuccess,
}) => {
  const [customerGivenStr, setCustomerGivenStr] = useState<string>('');
  const [notes, setNotes] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const requiredAmount = useMemo(() => {
    if (!invoice) return 0;
    return invoice.conLai > 0 ? invoice.conLai : invoice.thanhTien;
  }, [invoice]);

  useEffect(() => {
    if (isOpen && invoice) {
      setCustomerGivenStr(requiredAmount.toString());
      setNotes('');
      setError(null);
      setLoading(false);
    }
  }, [isOpen, invoice, requiredAmount]);

  // Tạo các mốc gợi ý tiền nhanh
  const quickMoneyOptions = useMemo(() => {
    const options: number[] = [requiredAmount];
    const denominations = [100000, 200000, 500000, 1000000, 2000000, 5000000, 10000000, 20000000];

    // Tìm các mệnh giá tròn lớn hơn requiredAmount
    for (const d of denominations) {
      if (d > requiredAmount && !options.includes(d)) {
        options.push(d);
        if (options.length >= 4) break;
      }
    }

    // Nếu số tiền lẻ, tính số làm tròn lên 500k hoặc 1Tr gần nhất
    const roundNext500k = Math.ceil(requiredAmount / 500000) * 500000;
    if (roundNext500k > requiredAmount && !options.includes(roundNext500k)) {
      options.push(roundNext500k);
    }

    return Array.from(new Set(options)).sort((a, b) => a - b).slice(0, 4);
  }, [requiredAmount]);

  if (!isOpen || !invoice) return null;

  const customerGiven = parseFloat(customerGivenStr.replace(/[^0-9]/g, '')) || 0;
  const changeAmount = customerGiven - requiredAmount;
  const isEnoughMoney = customerGiven >= requiredAmount;

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  const handleGivenInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const rawValue = e.target.value.replace(/[^0-9]/g, '');
    setCustomerGivenStr(rawValue);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isEnoughMoney) {
      setError('Tiền khách đưa chưa đủ để thanh toán hóa đơn này');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      await invoiceService.createPayment(invoice.maHoaDon, {
        soTien: requiredAmount,
        phuongThuc: 'TIEN_MAT',
        maGiaoDich: notes.trim() ? notes.trim() : undefined,
      });

      onSuccess();
    } catch (err: any) {
      console.error('Lỗi khi xác nhận thanh toán tiền mặt:', err);
      const msg =
        err.response?.data?.message ||
        err.message ||
        'Không thể thực hiện thanh toán tiền mặt. Vui lòng thử lại';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1100,
        padding: '20px',
      }}
      onClick={onClose}
    >
      <div
        style={{
          backgroundColor: '#fff',
          borderRadius: '12px',
          width: '100%',
          maxWidth: '560px',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
          overflow: 'hidden',
          animation: 'fadeIn 0.15s ease-out',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div
          style={{
            padding: '18px 24px',
            borderBottom: '1px solid #e5e7eb',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: '#f8fafc',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div
              style={{
                width: '38px',
                height: '38px',
                borderRadius: '8px',
                backgroundColor: 'rgba(16, 185, 129, 0.12)',
                color: '#059669',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '22px' }}>
                payments
              </span>
            </div>
            <div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0, color: '#0f172a' }}>
                Xác nhận thanh toán tiền mặt
              </h3>
              <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '2px' }}>
                Hóa đơn #{invoice.maHoaDon} · Lệnh sửa chữa: #{invoice.maPhieuSuaChua || '—'}
              </div>
            </div>
          </div>

          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              color: '#94a3b8',
              padding: '4px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
              close
            </span>
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} style={{ padding: '24px' }}>
          {error && (
            <div
              style={{
                padding: '10px 14px',
                backgroundColor: '#fef2f2',
                border: '1px solid #fecaca',
                borderRadius: '8px',
                color: '#dc2626',
                fontSize: '0.875rem',
                marginBottom: '16px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                error
              </span>
              <span>{error}</span>
            </div>
          )}

          {/* Thông tin hóa đơn tóm tắt */}
          <div
            style={{
              backgroundColor: '#f8fafc',
              border: '1px solid #e2e8f0',
              borderRadius: '8px',
              padding: '12px 16px',
              marginBottom: '18px',
              display: 'grid',
              gridTemplateColumns: '1fr 1fr',
              gap: '10px',
              fontSize: '0.85rem',
            }}
          >
            <div>
              <span style={{ color: '#64748b' }}>Khách hàng: </span>
              <strong style={{ color: '#1e293b' }}>{invoice.tenKhachHang || 'Khách vãng lai'}</strong>
            </div>
            <div>
              <span style={{ color: '#64748b' }}>SĐT: </span>
              <span style={{ color: '#1e293b' }}>{invoice.soDienThoaiKhachHang || '—'}</span>
            </div>
            <div>
              <span style={{ color: '#64748b' }}>Biển số xe: </span>
              <strong style={{ color: '#1e293b', fontFamily: 'monospace' }}>
                {invoice.bienSoXe || '—'}
              </strong>
            </div>
            <div>
              <span style={{ color: '#64748b' }}>Chi nhánh: </span>
              <span style={{ color: '#1e293b' }}>{invoice.tenChiNhanh || '—'}</span>
            </div>
          </div>

          {/* Số tiền cần thanh toán */}
          <div
            style={{
              padding: '16px',
              backgroundColor: 'rgba(37, 99, 235, 0.05)',
              border: '1px solid rgba(37, 99, 235, 0.2)',
              borderRadius: '8px',
              marginBottom: '20px',
              textAlign: 'center',
            }}
          >
            <div style={{ fontSize: '0.825rem', color: '#475569', fontWeight: 600, textTransform: 'uppercase' }}>
              Số tiền cần thanh toán
            </div>
            <div
              style={{
                fontSize: '1.75rem',
                fontWeight: 800,
                color: '#2563eb',
                marginTop: '4px',
              }}
            >
              {formatCurrency(requiredAmount)}
            </div>
          </div>

          {/* Khối tiện ích thu ngân: Tiền khách đưa & Tiền thừa */}
          <div style={{ marginBottom: '16px' }}>
            <label
              style={{
                display: 'block',
                fontSize: '0.875rem',
                fontWeight: 600,
                color: '#1e293b',
                marginBottom: '6px',
              }}
            >
              Tiền khách đưa (VNĐ) <span style={{ color: '#ef4444' }}>*</span>
            </label>
            <input
              type="text"
              className="form-control"
              value={customerGiven ? Number(customerGiven).toLocaleString('vi-VN') : ''}
              onChange={handleGivenInputChange}
              placeholder="Nhập số tiền khách đưa..."
              style={{
                fontSize: '1.1rem',
                fontWeight: 600,
                padding: '10px 14px',
                textAlign: 'right',
              }}
              autoFocus
            />

            {/* Quick amount buttons */}
            <div style={{ display: 'flex', gap: '8px', marginTop: '8px', flexWrap: 'wrap' }}>
              {quickMoneyOptions.map((opt, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => setCustomerGivenStr(opt.toString())}
                  style={{
                    padding: '4px 10px',
                    fontSize: '0.8rem',
                    fontWeight: 500,
                    borderRadius: '6px',
                    border: '1px solid #cbd5e1',
                    backgroundColor: customerGiven === opt ? 'rgba(37, 99, 235, 0.1)' : '#fff',
                    color: customerGiven === opt ? '#2563eb' : '#475569',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease',
                  }}
                >
                  {opt === requiredAmount ? 'Đủ tiền' : formatCurrency(opt)}
                </button>
              ))}
            </div>
          </div>

          {/* Khối hiển thị tiền thừa / Cảnh báo thiếu tiền */}
          <div
            style={{
              padding: '12px 16px',
              borderRadius: '8px',
              marginBottom: '20px',
              backgroundColor: isEnoughMoney ? 'rgba(16, 185, 129, 0.08)' : '#fef2f2',
              border: `1px solid ${isEnoughMoney ? 'rgba(16, 185, 129, 0.3)' : '#fecaca'}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span
                className="material-symbols-outlined"
                style={{
                  fontSize: '20px',
                  color: isEnoughMoney ? '#059669' : '#dc2626',
                }}
              >
                {isEnoughMoney ? 'change_circle' : 'warning'}
              </span>
              <span
                style={{
                  fontSize: '0.9rem',
                  fontWeight: 600,
                  color: isEnoughMoney ? '#065f46' : '#991b1b',
                }}
              >
                {isEnoughMoney ? 'Tiền thừa thối lại khách:' : 'Tiền khách đưa chưa đủ:'}
              </span>
            </div>
            <div
              style={{
                fontSize: '1.15rem',
                fontWeight: 700,
                color: isEnoughMoney ? '#059669' : '#dc2626',
              }}
            >
              {isEnoughMoney ? formatCurrency(changeAmount) : `Thiếu ${formatCurrency(Math.abs(changeAmount))}`}
            </div>
          </div>

          {/* Ghi chú thu ngân */}
          <div style={{ marginBottom: '24px' }}>
            <label
              style={{
                display: 'block',
                fontSize: '0.85rem',
                fontWeight: 500,
                color: '#475569',
                marginBottom: '6px',
              }}
            >
              Ghi chú / Tham chiếu (Tùy chọn)
            </label>
            <input
              type="text"
              className="form-control"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="VD: Thu tiền mặt tại quầy tiếp nhận..."
              maxLength={100}
            />
          </div>

          {/* Footer Actions */}
          <div
            style={{
              display: 'flex',
              justifyContent: 'flex-end',
              gap: '12px',
              paddingTop: '16px',
              borderTop: '1px solid #f1f5f9',
            }}
          >
            <Button variant="secondary" onClick={onClose} disabled={loading} type="button">
              Hủy bỏ
            </Button>
            <Button
              variant="primary"
              type="submit"
              disabled={loading || !isEnoughMoney}
              style={{
                backgroundColor: '#059669',
                borderColor: '#059669',
                color: '#fff',
                fontWeight: 600,
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
              }}
            >
              {loading ? (
                <>
                  <span
                    className="spinner"
                    style={{
                      width: '16px',
                      height: '16px',
                      border: '2px solid rgba(255,255,255,0.3)',
                      borderTopColor: '#fff',
                      borderRadius: '50%',
                      animation: 'spin 0.8s linear infinite',
                    }}
                  />
                  <span>Đang xử lý...</span>
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                    check_circle
                  </span>
                  <span>Xác nhận đã nhận tiền mặt</span>
                </>
              )}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
