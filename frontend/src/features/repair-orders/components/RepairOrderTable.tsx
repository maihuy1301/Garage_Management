import React, { useState } from 'react';
import {
  RepairOrderResponse,
  RepairOrderStatusLabels,
  RepairOrderStatusTone,
} from '@/types/repair-order.types';
import { Button } from '@/components/common/Button';
import { ActionDropdown } from '@/components/common/ActionDropdown';

interface RepairOrderTableProps {
  orders: RepairOrderResponse[];
  loading: boolean;
  canAssign?: boolean;
  onViewDetail: (order: RepairOrderResponse) => void;
  onAssign?: (order: RepairOrderResponse) => void;
}

export const RepairOrderTable: React.FC<RepairOrderTableProps> = ({
  orders,
  loading,
  canAssign = false,
  onViewDetail,
  onAssign,
}) => {
  const [currentPage, setCurrentPage] = useState<number>(1);
  const pageSize = 10;

  const totalPages = Math.ceil(orders.length / pageSize) || 1;
  const startIndex = (currentPage - 1) * pageSize;
  const displayedOrders = orders.slice(startIndex, startIndex + pageSize);

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

  if (loading) {
    return (
      <div className="data-table-card" style={{ padding: '48px', textAlign: 'center' }}>
        <div
          className="spinner"
          style={{
            width: '36px',
            height: '36px',
            border: '3px solid var(--color-outline-variant)',
            borderTopColor: 'var(--color-primary)',
            borderRadius: '50%',
            animation: 'spin 0.8s linear infinite',
            margin: '0 auto 16px auto',
          }}
        />
        <p style={{ color: 'var(--color-outline)', margin: 0, fontSize: '0.9rem' }}>
          Đang tải danh sách lệnh sửa chữa...
        </p>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="data-table-card" style={{ padding: '48px', textAlign: 'center' }}>
        <span
          className="material-symbols-outlined"
          style={{ fontSize: '48px', color: 'var(--color-outline)', marginBottom: '12px' }}
        >
          assignment
        </span>
        <h4 style={{ margin: '0 0 8px 0', color: 'var(--color-on-surface)' }}>
          Không có lệnh sửa chữa nào
        </h4>
        <p style={{ margin: 0, color: 'var(--color-outline)', fontSize: '0.875rem' }}>
          Hiện chưa có lệnh sửa chữa nào phù hợp với bộ lọc tìm kiếm.
        </p>
      </div>
    );
  }

  return (
    <div className="data-table-card">
      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th style={{ width: '90px', textAlign: 'center' }}>MÃ LỆNH</th>
              <th>PHƯƠNG TIỆN & BIỂN SỐ</th>
              <th>KHÁCH HÀNG</th>
              <th>CHI NHÁNH</th>
              <th>THỜI GIAN BẮT ĐẦU</th>
              <th style={{ width: '150px', textAlign: 'center' }}>TRẠNG THÁI</th>
              <th style={{ width: '140px', textAlign: 'right' }}>THAO TÁC</th>
            </tr>
          </thead>
          <tbody>
            {displayedOrders.map((order) => {
              const statusLabel = RepairOrderStatusLabels[order.trangThai] || order.trangThai;
              const statusTone = RepairOrderStatusTone[order.trangThai] || 'primary';

              return (
                <tr key={order.maPhieuSuaChua}>
                  {/* Mã Lệnh */}
                  <td style={{ textAlign: 'center' }}>
                    <span
                      style={{
                        fontFamily: 'monospace',
                        fontWeight: 700,
                        fontSize: '0.8rem',
                        backgroundColor: 'var(--color-surface-container)',
                        color: 'var(--color-primary)',
                        padding: '4px 8px',
                        borderRadius: 'var(--radius-sm)',
                        display: 'inline-block',
                      }}
                    >
                      #{order.maPhieuSuaChua}
                    </span>
                    {order.maTiepNhan && (
                      <div style={{ fontSize: '0.725rem', color: 'var(--color-outline)', marginTop: '2px' }}>
                        TN #{order.maTiepNhan}
                      </div>
                    )}
                  </td>

                  {/* Phương tiện & Biển số */}
                  <td>
                    <div>
                      <span
                        style={{
                          display: 'inline-block',
                          fontFamily: 'monospace',
                          fontWeight: 700,
                          fontSize: '0.8rem',
                          backgroundColor: '#fef3c7',
                          color: '#92400e',
                          padding: '2px 8px',
                          borderRadius: 'var(--radius-sm)',
                          border: '1px solid #fde68a',
                        }}
                      >
                        {order.bienSoXe}
                      </span>
                      <div style={{ fontSize: '0.8rem', color: 'var(--color-on-surface-variant)', marginTop: '2px' }}>
                        {order.hangXe || order.tenHangXe || ''} {order.modelXe || order.tenModel || ''}
                      </div>
                    </div>
                  </td>

                  {/* Khách hàng */}
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <div className="data-table-avatar">
                        {order.tenKhachHang ? order.tenKhachHang.charAt(0).toUpperCase() : 'KH'}
                      </div>
                      <div>
                        <div style={{ fontWeight: 600, color: 'var(--color-on-surface)' }}>
                          {order.tenKhachHang}
                        </div>
                        <div
                          style={{
                            fontSize: '0.775rem',
                            color: 'var(--color-outline)',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px',
                            marginTop: '2px',
                          }}
                        >
                          <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>
                            call
                          </span>
                          <span>{order.soDienThoaiKhachHang || '—'}</span>
                        </div>
                      </div>
                    </div>
                  </td>

                  {/* Chi nhánh */}
                  <td>
                    <div style={{ fontWeight: 500, fontSize: '0.85rem', color: 'var(--color-on-surface)' }}>
                      {order.tenChiNhanh}
                    </div>
                  </td>

                  {/* Thời gian bắt đầu */}
                  <td>
                    <div style={{ fontSize: '0.85rem', color: 'var(--color-on-surface)' }}>
                      {formatDateTime(order.thoiGianBatDau)}
                    </div>
                    {order.thoiGianHoanTat && (
                      <div style={{ fontSize: '0.75rem', color: 'var(--color-success)', marginTop: '2px' }}>
                        Xong: {formatDateTime(order.thoiGianHoanTat)}
                      </div>
                    )}
                  </td>

                  {/* Trạng thái */}
                  <td style={{ textAlign: 'center' }}>
                    <span className={`status-badge ${statusTone}`}>
                      {statusLabel}
                    </span>
                  </td>

                  {/* Thao tác */}
                  <td style={{ textAlign: 'center' }}>
                    <ActionDropdown
                      items={[
                        {
                          label: 'Xem chi tiết lệnh',
                          icon: 'visibility',
                          onClick: () => onViewDetail(order),
                        },
                        canAssign && onAssign
                          ? {
                              label: 'Phân công kỹ thuật viên',
                              icon: 'person_add',
                              variant: 'primary',
                              onClick: () => onAssign(order),
                            }
                          : false,
                      ]}
                    />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Pagination Footer */}
      {totalPages > 1 && (
        <div
          className="pagination-container"
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '12px 20px',
            borderTop: '1px solid var(--color-border)',
          }}
        >
          <div style={{ fontSize: '0.85rem', color: 'var(--color-outline)' }}>
            Hiển thị {startIndex + 1} - {Math.min(startIndex + pageSize, orders.length)} / {orders.length} lệnh
          </div>
          <div style={{ display: 'flex', gap: '6px' }}>
            <Button
              variant="secondary"
              disabled={currentPage === 1}
              onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
              style={{ padding: '6px 12px', minHeight: 'auto' }}
            >
              Trang trước
            </Button>
            <Button
              variant="secondary"
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
              style={{ padding: '6px 12px', minHeight: 'auto' }}
            >
              Trang sau
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};
