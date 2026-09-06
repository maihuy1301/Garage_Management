package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicianExecutionService {

    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final PhanCongRepository phanCongRepository;
    private final PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository;
    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    public TechnicianExecutionService(PhieuSuaChuaRepository phieuSuaChuaRepository,
                                      PhanCongRepository phanCongRepository,
                                      PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository,
                                      NhanVienRepository nhanVienRepository,
                                      NguoiDungRepository nguoiDungRepository,
                                      NguoiDungVaiTroRepository nguoiDungVaiTroRepository) {
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.phanCongRepository = phanCongRepository;
        this.phieuSuaChuaDichVuRepository = phieuSuaChuaDichVuRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
    }

    /**
     * Kỹ thuật viên xem danh sách các phiếu sửa chữa được phân công cho mình
     */
    @Transactional(readOnly = true)
    public List<RepairOrderResponse> getMyRepairOrders() {
        NhanVien tech = getCurrentTechnician();
        return phanCongRepository.findByNhanVienDuocPhanCongMaNhanVien(tech.getMaNhanVien())
                .stream()
                .map(PhanCong::getPhieuSuaChua)
                .distinct()
                .map(this::mapToRepairOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Kỹ thuật viên xem chi tiết phiếu sửa chữa được phân công cho mình
     */
    @Transactional(readOnly = true)
    public RepairOrderResponse getRepairOrderDetail(Integer repairOrderId) {
        NhanVien tech = getCurrentTechnician();
        PhieuSuaChua order = validateTechnicianAssignment(tech, repairOrderId);
        return mapToRepairOrderResponse(order);
    }

    /**
     * Kỹ thuật viên xem danh sách các hạng mục dịch vụ thuộc phiếu sửa chữa của mình
     */
    @Transactional(readOnly = true)
    public List<RepairItemResponse> getRepairOrderItems(Integer repairOrderId) {
        NhanVien tech = getCurrentTechnician();
        validateTechnicianAssignment(tech, repairOrderId);
        return phieuSuaChuaDichVuRepository.findByPhieuSuaChuaMaPhieuSuaChua(repairOrderId)
                .stream()
                .map(this::mapToRepairItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Kỹ thuật viên cập nhật tiến độ thực hiện sửa chữa:
     * - Kiểm tra phân công hợp lệ
     * - Bảo vệ phiếu sửa chữa đã hoàn tất/hủy (400)
     * - Cập nhật trực tiếp: TrangThai, ThoiGianBatDau, ThoiGianHoanTat, GhiChu của PhieuSuaChua
     * - Tự động ghi nhận thoiGianBatDau / thoiGianHoanTat khi tương ứng nếu chưa có
     * - Trả về thông tin PhieuSuaChua hiện tại
     */
    @Transactional
    public RepairOrderResponse updateProgress(Integer repairOrderId, UpdateProgressRequest request) {
        NhanVien tech = getCurrentTechnician();
        PhieuSuaChua order = validateTechnicianAssignment(tech, repairOrderId);
        validateOrderNotCompletedOrCancelled(order, "cập nhật tiến độ");

        if (request.getTrangThai() != null && !request.getTrangThai().isBlank()) {
            String newStatus = request.getTrangThai();
            if ("DANG_SUA".equalsIgnoreCase(newStatus)) {
                if (order.getThoiGianBatDau() == null) {
                    order.setThoiGianBatDau(request.getThoiGianBatDau() != null ? request.getThoiGianBatDau() : LocalDateTime.now());
                }
                order.setTrangThai("DANG_SUA");
            } else if ("HOAN_TAT".equalsIgnoreCase(newStatus)) {
                if (order.getThoiGianBatDau() == null) {
                    order.setThoiGianBatDau(request.getThoiGianBatDau() != null ? request.getThoiGianBatDau() : LocalDateTime.now());
                }
                order.setThoiGianHoanTat(request.getThoiGianHoanTat() != null ? request.getThoiGianHoanTat() : LocalDateTime.now());
                order.setTrangThai("HOAN_TAT");
            } else if ("TAM_DUNG".equalsIgnoreCase(newStatus) || "CHO_KH_DUYET".equalsIgnoreCase(newStatus) || "DA_PHAN_CONG".equalsIgnoreCase(newStatus)) {
                order.setTrangThai(newStatus);
            } else {
                throw new BadRequestException("Trạng thái tiến độ không hợp lệ: " + newStatus);
            }
        }

        if (request.getThoiGianBatDau() != null) {
            order.setThoiGianBatDau(request.getThoiGianBatDau());
        }
        if (request.getThoiGianHoanTat() != null) {
            order.setThoiGianHoanTat(request.getThoiGianHoanTat());
        }
        if (request.getGhiChu() != null) {
            order.setGhiChu(request.getGhiChu());
        }

        PhieuSuaChua saved = phieuSuaChuaRepository.save(order);
        return mapToRepairOrderResponse(saved);
    }

    /**
     * Kỹ thuật viên cập nhật trạng thái hạng mục dịch vụ trong phiếu sửa chữa
     */
    @Transactional
    public RepairItemResponse updateItemStatus(Integer repairOrderId, Integer itemId, UpdateServiceItemStatusRequest request) {
        NhanVien tech = getCurrentTechnician();
        PhieuSuaChua order = validateTechnicianAssignment(tech, repairOrderId);
        validateOrderNotCompletedOrCancelled(order, "cập nhật trạng thái dịch vụ");

        PhieuSuaChuaDichVu item = phieuSuaChuaDichVuRepository
                .findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(itemId, repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hạng mục dịch vụ ID " + itemId + " trong phiếu sửa chữa này"));

        item.setTrangThai(request.getTrangThai());
        PhieuSuaChuaDichVu updated = phieuSuaChuaDichVuRepository.save(item);
        return mapToRepairItemResponse(updated);
    }

    // --- Private Helpers ---

    private NhanVien getCurrentTechnician() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Unauthorized: Bạn chưa đăng nhập");
        }

        NguoiDung user = nguoiDungRepository.findByTenDangNhapOrEmail(auth.getName(), auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản: " + auth.getName()));

        if (Boolean.FALSE.equals(user.getTrangThai())) {
            throw new BadRequestException("Tài khoản của bạn đã bị khóa");
        }

        NhanVien employee = nhanVienRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                .orElseThrow(() -> new AccessDeniedException("Forbidden: Tài khoản này không được liên kết với nhân viên garage"));

        if (Boolean.FALSE.equals(employee.getTrangThai())) {
            throw new BadRequestException("Hồ sơ nhân viên hiện đang ngưng hoạt động");
        }

        boolean isTechnician = nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                .stream()
                .anyMatch(nvt -> {
                    String r = nvt.getVaiTro().getTenVaiTro();
                    return "TECHNICIAN".equalsIgnoreCase(r) || "ROLE_TECHNICIAN".equalsIgnoreCase(r);
                });

        if (!isTechnician) {
            throw new AccessDeniedException("Forbidden: Chỉ Kỹ thuật viên (TECHNICIAN) mới có quyền truy cập module này");
        }

        return employee;
    }

    private PhieuSuaChua validateTechnicianAssignment(NhanVien tech, Integer repairOrderId) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + repairOrderId));

        // Kiểm tra chi nhánh
        Integer techBranchId = (tech.getChiNhanh() != null) ? tech.getChiNhanh().getMaChiNhanh() : null;
        Integer orderBranchId = (order.getChiNhanh() != null) ? order.getChiNhanh().getMaChiNhanh() : null;

        if (techBranchId == null || !techBranchId.equals(orderBranchId)) {
            throw new AccessDeniedException("Forbidden: Phiếu sửa chữa thuộc chi nhánh khác");
        }

        // Kiểm tra phân công (Technician T1 không được xem/sửa phiếu của T2)
        boolean isAssigned = phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(
                repairOrderId, tech.getMaNhanVien()
        );

        if (!isAssigned) {
            throw new AccessDeniedException("Forbidden: Bạn không được phân công phụ trách phiếu sửa chữa này");
        }

        return order;
    }

    private void validateOrderNotCompletedOrCancelled(PhieuSuaChua order, String action) {
        String status = order.getTrangThai();
        if ("HOAN_TAT".equalsIgnoreCase(status) || "HUY".equalsIgnoreCase(status)) {
            throw new BadRequestException("Không thể " + action + " khi phiếu sửa chữa đã ở trạng thái: " + status);
        }
    }

    private RepairOrderResponse mapToRepairOrderResponse(PhieuSuaChua order) {
        PhieuTiepNhan ptn = order.getPhieuTiepNhan();
        ChiNhanh cn = order.getChiNhanh();

        Integer maTiepNhan = null;
        Integer maDatLich = null;
        Integer maXe = null;
        String bienSoXe = null;
        String hangXe = null;
        String modelXe = null;

        Integer maKhachHang = null;
        String tenKhachHang = null;
        String soDienThoaiKhachHang = null;

        if (ptn != null) {
            maTiepNhan = ptn.getMaTiepNhan();
            if (ptn.getDatLich() != null) {
                maDatLich = ptn.getDatLich().getMaDatLich();
            }
            Xe xe = ptn.getXe();
            if (xe != null) {
                maXe = xe.getMaXe();
                bienSoXe = xe.getBienSo();
                hangXe = xe.getHangXe();
                modelXe = xe.getModel();
                if (xe.getKhachHang() != null) {
                    KhachHang kh = xe.getKhachHang();
                    maKhachHang = kh.getMaKhachHang();
                    if (kh.getNguoiDung() != null) {
                        tenKhachHang = kh.getNguoiDung().getHoTen();
                        soDienThoaiKhachHang = kh.getNguoiDung().getSoDienThoai();
                    }
                }
            }
        }

        return new RepairOrderResponse(
                order.getMaPhieuSuaChua(),
                maTiepNhan,
                maDatLich,
                maXe,
                bienSoXe,
                hangXe,
                modelXe,
                maKhachHang,
                tenKhachHang,
                soDienThoaiKhachHang,
                cn != null ? cn.getMaChiNhanh() : null,
                cn != null ? cn.getTenChiNhanh() : null,
                order.getThoiGianBatDau(),
                order.getThoiGianHoanTat(),
                order.getTrangThai(),
                order.getGhiChu()
        );
    }

    private RepairItemResponse mapToRepairItemResponse(PhieuSuaChuaDichVu item) {
        DichVu dv = item.getDichVu();
        Integer maLoaiDichVu = null;
        String tenLoaiDichVu = null;
        if (dv != null && dv.getLoaiDichVu() != null) {
            maLoaiDichVu = dv.getLoaiDichVu().getMaLoaiDichVu();
            tenLoaiDichVu = dv.getLoaiDichVu().getTenLoai();
        }

        BigDecimal thanhTien = item.getDonGia() != null ? item.getDonGia() : BigDecimal.ZERO;

        return new RepairItemResponse(
                item.getMaChiTiet(),
                item.getPhieuSuaChua() != null ? item.getPhieuSuaChua().getMaPhieuSuaChua() : null,
                dv != null ? dv.getMaDichVu() : null,
                dv != null ? dv.getTenDichVu() : null,
                maLoaiDichVu,
                tenLoaiDichVu,
                item.getDonGia(),
                thanhTien,
                item.getTrangThai()
        );
    }
}
