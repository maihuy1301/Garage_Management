package com.garage.service;

import com.garage.dto.CheckInRequest;
import com.garage.dto.ReceptionResponse;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import com.garage.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReceptionService {

    private final PhieuTiepNhanRepository phieuTiepNhanRepository;
    private final DatLichRepository datLichRepository;
    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final XeRepository xeRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public ReceptionService(PhieuTiepNhanRepository phieuTiepNhanRepository,
                            DatLichRepository datLichRepository,
                            NhanVienRepository nhanVienRepository,
                            NguoiDungRepository nguoiDungRepository,
                            XeRepository xeRepository,
                            BranchAuthorizationService branchAuthorizationService) {
        this.phieuTiepNhanRepository = phieuTiepNhanRepository;
        this.datLichRepository = datLichRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.xeRepository = xeRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Check-in tiếp nhận xe từ lịch hẹn:
     * - Xác thực quyền tiếp nhận (RECEPTIONIST, BRANCH_MANAGER thuộc chi nhánh của lịch hẹn, hoặc SYSTEM_ADMIN)
     * - Kiểm tra trạng thái lịch hẹn (không bị hủy, chưa được tiếp nhận trước đó)
     * - Idempotency check: Đảm bảo không tạo duplicate phiếu tiếp nhận
     * - Tạo PhieuTiepNhan và cập nhật DatLich.trangThai -> "DA_TIEP_NHAN" trong một transaction
     */
    @Transactional
    public ReceptionResponse checkIn(Integer appointmentId, CheckInRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        NguoiDung user = getAuthenticatedUser(auth);

        // 1. Tìm lịch hẹn
        DatLich datLich = datLichRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        // 2. Branch Authorization: Kiểm tra nhân viên có quyền tại chi nhánh này không
        if (!branchAuthorizationService.isAllowedBranch(datLich.getChiNhanh().getMaChiNhanh())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền tiếp nhận lịch hẹn thuộc chi nhánh khác");
        }

        // 3. Kiểm tra trạng thái lịch hẹn
        String currentStatus = datLich.getTrangThai();
        if ("HUY".equalsIgnoreCase(currentStatus)) {
            throw new BadRequestException("Không thể tiếp nhận lịch hẹn đã bị hủy");
        }
        if ("DA_TIEP_NHAN".equalsIgnoreCase(currentStatus)
                || "DANG_XU_LY".equalsIgnoreCase(currentStatus)
                || "HOAN_TAT".equalsIgnoreCase(currentStatus)) {
            throw new DuplicateResourceException("Lịch hẹn này đã được tiếp nhận trước đó (trạng thái hiện tại: " + currentStatus + ")");
        }

        // 4. Idempotency check: Kiểm tra xem đã có phiếu tiếp nhận cho lịch hẹn này chưa
        if (phieuTiepNhanRepository.existsByDatLichMaDatLich(appointmentId)) {
            throw new DuplicateResourceException("Phiếu tiếp nhận đã tồn tại cho lịch hẹn ID: " + appointmentId);
        }

        // 5. Xác định nhân viên tiếp nhận
        NhanVien receptionist = resolveReceptionist(user, datLich.getChiNhanh());

        // 6. Tạo phiếu tiếp nhận
        PhieuTiepNhan slip = new PhieuTiepNhan();
        slip.setDatLich(datLich);
        slip.setXe(datLich.getXe());
        slip.setChiNhanh(datLich.getChiNhanh());
        slip.setNhanVienTiepNhan(receptionist);
        slip.setTrangThai("DA_TIEP_NHAN");
        slip.setThoiGianTiepNhan(LocalDateTime.now());

        if (request != null) {
            slip.setSoKm(request.getSoKm());
            slip.setTinhTrangNgoaiThat(request.getTinhTrangNgoaiThat());
            slip.setYeuCauKhachHang(request.getYeuCauKhachHang());

            // Cập nhật số km hiện tại của xe nếu có
            if (request.getSoKm() != null && request.getSoKm() > 0) {
                Xe xe = datLich.getXe();
                if (xe != null && (xe.getSoKmHienTai() == null || request.getSoKm() > xe.getSoKmHienTai())) {
                    xe.setSoKmHienTai(request.getSoKm());
                    xeRepository.save(xe);
                }
            }
        }

        // 7. Cập nhật trạng thái lịch hẹn -> DA_TIEP_NHAN
        datLich.setTrangThai("DA_TIEP_NHAN");
        datLichRepository.save(datLich);

        PhieuTiepNhan saved = phieuTiepNhanRepository.save(slip);
        return mapToReceptionResponse(saved);
    }

    /**
     * Lấy danh sách phiếu tiếp nhận:
     * - SYSTEM_ADMIN: Toàn bộ
     * - BRANCH_MANAGER / RECEPTIONIST: Chỉ chi nhánh của mình
     */
    @Transactional(readOnly = true)
    public List<ReceptionResponse> getReceptionSlips() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isSystemAdmin(auth)) {
            return phieuTiepNhanRepository.findAll().stream()
                    .map(this::mapToReceptionResponse)
                    .collect(Collectors.toList());
        }

        Integer branchId = branchAuthorizationService.resolveUserBranchId(auth)
                .orElseThrow(() -> new AccessDeniedException("Forbidden: Nhân viên chưa được phân công chi nhánh"));

        return phieuTiepNhanRepository.findByChiNhanhMaChiNhanh(branchId).stream()
                .map(this::mapToReceptionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết phiếu tiếp nhận theo ID:
     * - SYSTEM_ADMIN: Bất kỳ
     * - BRANCH_MANAGER / RECEPTIONIST: Phải thuộc chi nhánh mình
     */
    @Transactional(readOnly = true)
    public ReceptionResponse getReceptionSlipById(Integer id) {
        PhieuTiepNhan slip = phieuTiepNhanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu tiếp nhận với ID: " + id));

        if (!branchAuthorizationService.isAllowedBranch(slip.getChiNhanh().getMaChiNhanh())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập phiếu tiếp nhận của chi nhánh khác");
        }

        return mapToReceptionResponse(slip);
    }

    // --- Private Helpers ---

    private NhanVien resolveReceptionist(NguoiDung user, ChiNhanh branch) {
        // Nếu user đăng nhập có bản ghi NhanVien
        return nhanVienRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                .orElseGet(() -> {
                    // Nếu là SYSTEM_ADMIN không có NhanVien riêng -> gán nhân viên đầu tiên tìm thấy của chi nhánh
                    List<NhanVien> staffList = nhanVienRepository.findByChiNhanhMaChiNhanh(branch.getMaChiNhanh());
                    if (!staffList.isEmpty()) {
                        return staffList.get(0);
                    }
                    throw new BadRequestException("Không tìm thấy nhân viên nào thuộc chi nhánh " + branch.getTenChiNhanh() + " để tiếp nhận");
                });
    }

    private NguoiDung getAuthenticatedUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Yêu cầu xác thực tài khoản");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getNguoiDung();
        }
        String username = auth.getName();
        return nguoiDungRepository.findByTenDangNhapOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + username));
    }

    private boolean isSystemAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private ReceptionResponse mapToReceptionResponse(PhieuTiepNhan slip) {
        DatLich dl = slip.getDatLich();
        Xe xe = slip.getXe();
        ChiNhanh cn = slip.getChiNhanh();
        NhanVien nv = slip.getNhanVienTiepNhan();

        Integer maKhachHang = null;
        String maKhachHangCode = null;
        String tenKhachHang = null;
        String soDienThoaiKhachHang = null;

        if (xe != null && xe.getKhachHang() != null) {
            KhachHang kh = xe.getKhachHang();
            maKhachHang = kh.getMaKhachHang();
            maKhachHangCode = kh.getMaKhachHangCode();
            if (kh.getNguoiDung() != null) {
                tenKhachHang = kh.getNguoiDung().getHoTen();
                soDienThoaiKhachHang = kh.getNguoiDung().getSoDienThoai();
            }
        }

        return new ReceptionResponse(
                slip.getMaTiepNhan(),
                dl != null ? dl.getMaDatLich() : null,
                xe != null ? xe.getMaXe() : null,
                xe != null ? xe.getBienSo() : null,
                xe != null ? xe.getHangXe() : null,
                xe != null ? xe.getModel() : null,
                maKhachHang,
                maKhachHangCode,
                tenKhachHang,
                soDienThoaiKhachHang,
                cn != null ? cn.getMaChiNhanh() : null,
                cn != null ? cn.getMaChiNhanhCode() : null,
                cn != null ? cn.getTenChiNhanh() : null,
                nv != null ? nv.getMaNhanVien() : null,
                nv != null ? nv.getMaNhanVienCode() : null,
                nv != null && nv.getNguoiDung() != null ? nv.getNguoiDung().getHoTen() : null,
                slip.getThoiGianTiepNhan(),
                slip.getSoKm(),
                slip.getTinhTrangNgoaiThat(),
                slip.getYeuCauKhachHang(),
                slip.getTrangThai()
        );
    }
}
