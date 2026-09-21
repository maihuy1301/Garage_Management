import React from 'react';
import { BranchResponse } from '@/types/branch.types';

interface BranchDetailModalProps {
  branch: BranchResponse | null;
  isOpen: boolean;
  onClose: () => void;
  onEdit: (branch: BranchResponse) => void;
}

export const BranchDetailModal: React.FC<BranchDetailModalProps> = ({
  branch,
  isOpen,
  onClose,
  onEdit,
}) => {
  if (!isOpen || !branch) return null;

  const formattedDate = branch.ngayTao
    ? new Date(branch.ngayTao).toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    : '—';

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title">
            <span className="material-symbols-outlined" style={{ color: 'var(--primary)' }}>
              store_mall_directory
            </span>
            <span>Chi tiết chi nhánh #{branch.maChiNhanh}</span>
          </div>
          <button className="modal-close-btn" onClick={onClose} aria-label="Đóng">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        <div className="modal-body">
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border)', paddingBottom: '12px' }}>
              <div>
                <h3 style={{ margin: 0, fontSize: '1.2rem', color: 'var(--text-primary)' }}>
                  {branch.tenChiNhanh}
                </h3>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  Mã chi nhánh: #{branch.maChiNhanh}
                </span>
              </div>
              <div>
                {branch.trangThai ? (
                  <span className="badge badge-success">
                    <span className="badge-dot"></span>
                    Hoạt động
                  </span>
                ) : (
                  <span className="badge badge-danger">
                    <span className="badge-dot"></span>
                    Ngưng hoạt động
                  </span>
                )}
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
              <div className="detail-item">
                <span className="detail-label" style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
                  Địa chỉ
                </span>
                <span className="detail-value" style={{ fontWeight: 500, color: 'var(--text-primary)' }}>
                  {branch.diaChi || '—'}
                </span>
              </div>

              <div className="detail-item">
                <span className="detail-label" style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
                  Số điện thoại
                </span>
                <span className="detail-value" style={{ fontWeight: 500, color: 'var(--text-primary)' }}>
                  {branch.soDienThoai || '—'}
                </span>
              </div>

              <div className="detail-item">
                <span className="detail-label" style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
                  Email
                </span>
                <span className="detail-value" style={{ fontWeight: 500, color: 'var(--text-primary)' }}>
                  {branch.email || '—'}
                </span>
              </div>

              <div className="detail-item">
                <span className="detail-label" style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
                  Ngày khởi tạo
                </span>
                <span className="detail-value" style={{ fontWeight: 500, color: 'var(--text-primary)' }}>
                  {formattedDate}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Đóng
          </button>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => {
              onClose();
              onEdit(branch);
            }}
          >
            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>edit</span>
            <span>Chỉnh sửa</span>
          </button>
        </div>
      </div>
    </div>
  );
};
