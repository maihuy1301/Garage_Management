package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.PhieuSuaChuaRepository;
import com.garage.repository.PhieuTiepNhanRepository;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RepairOrderService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "CHO_XU_LY", "DA_PHAN_CONG", "DANG_SUA", "CHO_KH_DUYET", "TAM_DUNG", "HOAN_TAT", "HUY"
    );

    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final PhieuTiepNhanRepository phieuTiepNhanRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public RepairOrderService(PhieuSuaChuaRepository phieuSuaChuaRepository,
                              PhieuTiepNhanRepository phieuTiepNhanRepository,
                              BranchAuthorizationService branchAuthorizationService) {
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.phieuTiepNhanRepository = phieuTiepNhanRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Tạo phiếu sửa chữa từ phiếu tiếp nhận:
     * - Kiểm tra phiếu tiếp nhận tồn tại & hợp lệ
     * - Idempotency: Không cho tạo trùng phiếu sửa chữa chính cho cùng phiếu tiếp nhận (nếu không phải sub-repair order)
     * - Hỗ trợ maPhieuCha cho sub-repair order
     * - Branch authorization: Kiểm tra quyền truy cập chi nhánh
     * - Tự động liên kết ChiNhanh từ PhieuTiepNhan
     */
    @Transactional
    public RepairOrderResponse createRepairOrder(CreateRepairOrderRequest request) {
        // 1. Tìm phiếu tiếp nhận
        PhieuTiepNhan reception = phieuTiepNhanRepository.findById(request.getMaTiepNhan())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu tiếp nhận với ID: " + request.getMaTiepNhan()));

        // 2. Branch Authorization
        if (!branchAuthorizationService.isAllowedBranch(reception.getChiNhanh().getMaChiNhanh())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền tạo phiếu sửa chữa thuộc chi nhánh khác");
        }

        // 3. Kiểm tra trạng thái phiếu tiếp nhận
        if ("HUY".equalsIgnoreCase(reception.getTrangThai())) {
            throw new BadRequestException("Không thể tạo phiếu sửa chữa cho phiếu tiếp nhận đã bị hủy");
        }

        // 4. Validate MaPhieuCha nếu có
        PhieuSuaChua parentOrder = null;
        if (request.getMaPhieuCha() != null) {
            parentOrder = phieuSuaChuaRepository.findById(request.getMaPhieuCha())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa cha ID: " + request.getMaPhieuCha()));
        } else {
            // Idempotency check: Chỉ chặn khi tạo phiếu sửa chữa chính gốc
            if (phieuSuaChuaRepository.existsByPhieuTiepNhanMaTiepNhan(request.getMaTiepNhan())) {
                throw new DuplicateResourceException("Phiếu sửa chữa đã tồn tại cho phiếu tiếp nhận ID: " + request.getMaTiepNhan());
            }
        }

        // 5. Tạo PhieuSuaChua
        PhieuSuaChua order = new PhieuSuaChua();
        order.setPhieuTiepNhan(reception);
        order.setChiNhanh(reception.getChiNhanh());
        order.setPhieuCha(parentOrder);
        order.setThoiGianBatDau(request.getThoiGianBatDau() != null ? request.getThoiGianBatDau() : LocalDateTime.now());
        order.setTrangThai("CHO_XU_LY");
        order.setGhiChu(request.getGhiChu());

        PhieuSuaChua saved = phieuSuaChuaRepository.save(order);
        return mapToRepairOrderResponse(saved);
    }

    /**
     * Lấy danh sách phiếu sửa chữa:
     * - SYSTEM_ADMIN: Toàn bộ hệ thống
     * - BRANCH_MANAGER / RECEPTIONIST: Chi nhánh của mình
     */
    @Transactional(readOnly = true)
    public List<RepairOrderResponse> getRepairOrders() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isSystemAdmin(auth)) {
            return phieuSuaChuaRepository.findAll().stream()
                    .map(this::mapToRepairOrderResponse)
                    .collect(Collectors.toList());
        }

        Integer branchId = branchAuthorizationService.resolveUserBranchId(auth)
                .orElseThrow(() -> new AccessDeniedException("Forbidden: Nhân viên chưa được phân công chi nhánh"));

        return phieuSuaChuaRepository.findByChiNhanhMaChiNhanh(branchId).stream()
                .map(this::mapToRepairOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết phiếu sửa chữa theo ID:
     * - SYSTEM_ADMIN: Bất kỳ
     * - BRANCH_MANAGER / RECEPTIONIST: Phải thuộc chi nhánh mình
     */
    @Transactional(readOnly = true)
    public RepairOrderResponse getRepairOrderById(Integer id) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + id));

        if (!branchAuthorizationService.isAllowedBranch(order.getChiNhanh().getMaChiNhanh())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập phiếu sửa chữa của chi nhánh khác");
        }

        return mapToRepairOrderResponse(order);
    }

    /**
     * Cập nhật thông tin phiếu sửa chữa:
     * - Không cho phép đổi quan hệ chi nhánh, xe, khách hàng, phiếu tiếp nhận
     * - Không cho sửa nếu đã HOAN_TAT hoặc HUY
     */
    @Transactional
    public RepairOrderResponse updateRepairOrder(Integer id, UpdateRepairOrderRequest request) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + id));

        if (!branchAuthorizationService.isAllowedBranch(order.getChiNhanh().getMaChiNhanh())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền cập nhật phiếu sửa chữa của chi nhánh khác");
        }

        String currentStatus = order.getTrangThai();
        if ("HOAN_TAT".equalsIgnoreCase(currentStatus) || "HUY".equalsIgnoreCase(currentStatus)) {
            throw new BadRequestException("Không thể chỉnh sửa phiếu sửa chữa đã ở trạng thái: " + currentStatus);
        }

        if (request.getGhiChu() != null) {
            order.setGhiChu(request.getGhiChu());
        }
        if (request.getThoiGianBatDau() != null) {
            order.setThoiGianBatDau(request.getThoiGianBatDau());
        }
        if (request.getThoiGianHoanTat() != null) {
            order.setThoiGianHoanTat(request.getThoiGianHoanTat());
        }

        PhieuSuaChua updated = phieuSuaChuaRepository.save(order);
        return mapToRepairOrderResponse(updated);
    }

    /**
     * Cập nhật trạng thái phiếu sửa chữa:
     * - Kiểm tra tính hợp lệ của status
     * - Chặn chuyển trạng thái khi đã kết thúc (HOAN_TAT, HUY)
     */
    @Transactional
    public RepairOrderResponse updateStatus(Integer id, UpdateRepairOrderStatusRequest request) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + id));

        if (!branchAuthorizationService.isAllowedBranch(order.getChiNhanh().getMaChiNhanh())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền thay đổi trạng thái phiếu sửa chữa của chi nhánh khác");
        }

        String newStatus = request.getTrangThai().toUpperCase().trim();
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new BadRequestException("Trạng thái không hợp lệ: " + newStatus + ". Các trạng thái hợp lệ: " + VALID_STATUSES);
        }

        String currentStatus = order.getTrangThai();
        if ("HOAN_TAT".equalsIgnoreCase(currentStatus) || "HUY".equalsIgnoreCase(currentStatus)) {
            throw new BadRequestException("Phiếu sửa chữa đã ở trạng thái kết thúc (" + currentStatus + "), không thể thay đổi");
        }

        order.setTrangThai(newStatus);
        if ("HOAN_TAT".equalsIgnoreCase(newStatus) && order.getThoiGianHoanTat() == null) {
            order.setThoiGianHoanTat(LocalDateTime.now());
        }

        PhieuSuaChua updated = phieuSuaChuaRepository.save(order);
        return mapToRepairOrderResponse(updated);
    }

    // --- Private Helpers ---

    private boolean isSystemAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private RepairOrderResponse mapToRepairOrderResponse(PhieuSuaChua order) {
        PhieuTiepNhan ptn = order.getPhieuTiepNhan();
        ChiNhanh cn = order.getChiNhanh();

        Integer maTiepNhan = null;
        Integer maDatLich = null;
        Integer maXe = null;
        String bienSoXe = null;
        Integer maHangXe = null;
        String tenHangXe = null;
        Integer maModel = null;
        String tenModel = null;

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
                if (xe.getModelXe() != null) {
                    maModel = xe.getModelXe().getMaModel();
                    tenModel = xe.getModelXe().getTenModel();
                    if (xe.getModelXe().getHangXe() != null) {
                        maHangXe = xe.getModelXe().getHangXe().getMaHangXe();
                        tenHangXe = xe.getModelXe().getHangXe().getTenHangXe();
                    }
                }

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

        Integer maPhieuCha = (order.getPhieuCha() != null) ? order.getPhieuCha().getMaPhieuSuaChua() : null;

        return new RepairOrderResponse(
                order.getMaPhieuSuaChua(),
                maPhieuCha,
                maTiepNhan,
                maDatLich,
                maXe,
                bienSoXe,
                maHangXe,
                tenHangXe,
                maModel,
                tenModel,
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
}
