import React, { useState, useMemo } from 'react';
import { VehicleResponse } from '@/types/vehicle.types';
import { EmptyState } from '@/components/common/EmptyState';

interface VehicleTableProps {
  vehicles: VehicleResponse[];
  isAdmin: boolean;
  onView: (vehicle: VehicleResponse) => void;
  onEdit: (vehicle: VehicleResponse) => void;
  onDelete: (vehicle: VehicleResponse) => void;
}

export const VehicleTable: React.FC<VehicleTableProps> = ({
  vehicles,
  isAdmin,
  onView,
  onEdit,
  onDelete,
}) => {
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedBrand, setSelectedBrand] = useState<string>('ALL');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');

  // Extract unique brands for filtering
  const availableBrands = useMemo(() => {
    const brands = new Set<string>();
    vehicles.forEach((v) => {
      if (v.hangXe && v.hangXe.trim()) {
        brands.add(v.hangXe.trim());
      }
    });
    return Array.from(brands).sort();
  }, [vehicles]);

  const filteredVehicles = useMemo(() => {
    return vehicles.filter((v) => {
      const term = searchTerm.toLowerCase().trim();
      const matchSearch =
        !term ||
        v.bienSo?.toLowerCase().includes(term) ||
        v.hangXe?.toLowerCase().includes(term) ||
        v.model?.toLowerCase().includes(term) ||
        v.mauXe?.toLowerCase().includes(term) ||
        v.soVIN?.toLowerCase().includes(term) ||
        v.tenChuXe?.toLowerCase().includes(term) ||
        v.maKhachHangCode?.toLowerCase().includes(term);

      const matchBrand = selectedBrand === 'ALL' || v.hangXe === selectedBrand;

      const isVehicleActive = v.trangThai !== false;
      const matchStatus =
        selectedStatus === 'ALL' ||
        (selectedStatus === 'ACTIVE' && isVehicleActive) ||
        (selectedStatus === 'INACTIVE' && !isVehicleActive);

      return matchSearch && matchBrand && matchStatus;
    });
  }, [vehicles, searchTerm, selectedBrand, selectedStatus]);

  return (
    <div>
      {/* Search & Filter Bar */}
      <div className="filter-card">
        <div className="filter-search-box">
          <span className="material-symbols-outlined">search</span>
          <input
            type="text"
            placeholder={
              isAdmin
                ? 'Tìm theo biển số, hãng xe, model, chủ xe, mã KH, số VIN...'
                : 'Tìm kiếm phương tiện theo biển số, hãng xe, dòng xe, màu sắc...'
            }
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-controls">
          {/* Brand Filter */}
          <select
            className="filter-select"
            value={selectedBrand}
            onChange={(e) => setSelectedBrand(e.target.value)}
          >
            <option value="ALL">Tất cả hãng xe</option>
            {availableBrands.map((brand) => (
              <option key={brand} value={brand}>
                {brand}
              </option>
            ))}
          </select>

          {/* Status Filter */}
          <select
            className="filter-select"
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="ACTIVE">Đang hoạt động</option>
            <option value="INACTIVE">Tạm ngưng</option>
          </select>
        </div>
      </div>

      {/* Table Card */}
      <div className="data-table-card">
        {filteredVehicles.length === 0 ? (
          <div style={{ padding: '32px 0' }}>
            <EmptyState
              title="Không tìm thấy phương tiện"
              description={
                vehicles.length === 0
                  ? 'Hiện chưa có phương tiện nào được ghi nhận trong hệ thống.'
                  : 'Không tìm thấy xe nào khớp với điều kiện tìm kiếm hoặc bộ lọc.'
              }
            />
          </div>
        ) : (
          <div className="data-table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Biển số xe</th>
                  <th>Hãng & Dòng xe</th>
                  <th>Năm SX / Màu sắc</th>
                  <th>Số KM hiện tại</th>
                  {isAdmin && <th>Chủ sở hữu</th>}
                  <th>Số VIN</th>
                  <th>Trạng thái</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredVehicles.map((vehicle) => {
                  const isActive = vehicle.trangThai !== false;
                  return (
                    <tr key={vehicle.maXe}>
                      {/* Biển số xe */}
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                          <div
                            style={{
                              width: '38px',
                              height: '38px',
                              borderRadius: 'var(--radius-md)',
                              backgroundColor: 'var(--color-surface-container-high)',
                              color: 'var(--color-primary)',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              flexShrink: 0,
                            }}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
                              directions_car
                            </span>
                          </div>
                          <div>
                            <div
                              style={{
                                fontFamily: 'monospace',
                                fontWeight: 700,
                                fontSize: '0.95rem',
                                color: 'var(--color-primary)',
                                backgroundColor: 'var(--color-surface-container-low)',
                                padding: '2px 8px',
                                borderRadius: 'var(--radius-sm)',
                                display: 'inline-block',
                                border: '1px solid var(--color-outline-variant)',
                              }}
                            >
                              {vehicle.bienSo}
                            </div>
                            {vehicle.ngayTao && (
                              <div style={{ fontSize: '0.725rem', color: 'var(--color-outline)', marginTop: '2px' }}>
                                Đăng ký: {new Date(vehicle.ngayTao).toLocaleDateString('vi-VN')}
                              </div>
                            )}
                          </div>
                        </div>
                      </td>

                      {/* Hãng & Model */}
                      <td>
                        <div style={{ fontWeight: 600, color: 'var(--color-on-surface)' }}>
                          {vehicle.hangXe || '—'} {vehicle.model || ''}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--color-outline)', marginTop: '2px' }}>
                          Phân khúc: Du lịch / Cá nhân
                        </div>
                      </td>

                      {/* Năm SX & Màu */}
                      <td>
                        <div style={{ fontWeight: 500 }}>
                          {vehicle.namSanXuat ? `Đời ${vehicle.namSanXuat}` : '—'}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--color-outline)', marginTop: '2px' }}>
                          Màu: <strong>{vehicle.mauXe || 'Chưa rõ'}</strong>
                        </div>
                      </td>

                      {/* ODO / KM */}
                      <td>
                        <div style={{ fontWeight: 600, color: 'var(--color-secondary-container)' }}>
                          {vehicle.soKmHienTai != null
                            ? `${vehicle.soKmHienTai.toLocaleString('vi-VN')} km`
                            : '0 km'}
                        </div>
                      </td>

                      {/* Chủ sở hữu (Admin view) */}
                      {isAdmin && (
                        <td>
                          {vehicle.tenChuXe || vehicle.maKhachHangCode ? (
                            <div>
                              <div style={{ fontWeight: 600, color: 'var(--color-on-surface)' }}>
                                {vehicle.tenChuXe || 'Khách hàng'}
                              </div>
                              <div style={{ fontSize: '0.75rem', color: 'var(--color-secondary-container)', fontWeight: 600, marginTop: '2px' }}>
                                Mã KH: {vehicle.maKhachHangCode || `ID: ${vehicle.maKhachHang}`}
                              </div>
                            </div>
                          ) : (
                            <span style={{ color: 'var(--color-outline)', fontSize: '0.85rem' }}>Chưa gắn KH</span>
                          )}
                        </td>
                      )}

                      {/* Số VIN */}
                      <td>
                        <div
                          style={{
                            fontSize: '0.8rem',
                            fontFamily: 'monospace',
                            color: 'var(--color-on-surface-variant)',
                            maxWidth: '130px',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap',
                          }}
                          title={vehicle.soVIN || 'Chưa cập nhật'}
                        >
                          {vehicle.soVIN || '—'}
                        </div>
                      </td>

                      {/* Trạng thái */}
                      <td>
                        <span className={`status-badge ${isActive ? 'success' : 'danger'}`}>
                          {isActive ? 'Hoạt động' : 'Tạm ngưng'}
                        </span>
                      </td>

                      {/* Actions */}
                      <td>
                        <div className="table-actions">
                          <button
                            type="button"
                            className="table-action-btn"
                            title="Xem chi tiết phương tiện"
                            onClick={() => onView(vehicle)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              visibility
                            </span>
                          </button>

                          <button
                            type="button"
                            className="table-action-btn edit"
                            title="Chỉnh sửa thông số xe"
                            onClick={() => onEdit(vehicle)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              edit
                            </span>
                          </button>

                          <button
                            type="button"
                            className="table-action-btn danger"
                            title="Xóa phương tiện"
                            onClick={() => onDelete(vehicle)}
                          >
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>
                              delete
                            </span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
