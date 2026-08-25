package com.garage.service;

import com.garage.dto.AppointmentResponse;
import com.garage.dto.CreateAppointmentRequest;
import com.garage.entity.ChiNhanh;
import com.garage.entity.DatLich;
import com.garage.entity.KhachHang;
import com.garage.entity.NguoiDung;
import com.garage.entity.Xe;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.ChiNhanhRepository;
import com.garage.repository.DatLichRepository;
import com.garage.repository.KhachHangRepository;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.XeRepository;
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
public class AppointmentService {

    private final DatLichRepository datLichRepository;
    private final KhachHangRepository khachHangRepository;
    private final XeRepository xeRepository;
    private final ChiNhanhRepository chiNhanhRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public AppointmentService(DatLichRepository datLichRepository,
                              KhachHangRepository khachHangRepository,
                              XeRepository xeRepository,
                              ChiNhanhRepository chiNhanhRepository,
                              NguoiDungRepository nguoiDungRepository,
                              BranchAuthorizationService branchAuthorizationService) {
        this.datLichRepository = datLichRepository;
        this.khachHangRepository = khachHangRepository;
        this.xeRepository = xeRepository;
        this.chiNhanhRepository = chiNhanhRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Lấy danh sách lịch hẹn dựa trên phân quyền:
     * - SYSTEM_ADMIN: Toàn bộ hệ thống
     * - BRANCH_MANAGER / RECEPTIONIST: Chỉ lịch hẹn thuộc chi nhánh của mình
     * - CUSTOMER: Chỉ lịch hẹn của chính mình
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isSystemAdmin(auth)) {
            return datLichRepository.findAll().stream()
                    .map(this::mapToAppointmentResponse)
                    .collect(Collectors.toList());
        }

        if (isBranchStaff(auth)) {
            Integer branchId = branchAuthorizationService.resolveUserBranchId(auth)
                    .orElseThrow(() -> new AccessDeniedException("Forbidden: Nhân viên chưa được phân công chi nhánh"));
            return datLichRepository.findByChiNhanhMaChiNhanh(branchId).stream()
                    .map(this::mapToAppointmentResponse)
                    .collect(Collectors.toList());
        }

        if (isCustomer(auth)) {
            KhachHang customer = getAuthenticatedCustomer(auth);
            return datLichRepository.findByKhachHangMaKhachHang(customer.getMaKhachHang()).stream()
                    .map(this::mapToAppointmentResponse)
                    .collect(Collectors.toList());
        }

        throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập danh sách lịch hẹn");
    }

    /**
     * Lấy chi tiết lịch hẹn theo ID kèm theo kiểm tra quyền truy cập:
     * - SYSTEM_ADMIN: Được xem bất kỳ
     * - BRANCH_MANAGER / RECEPTIONIST: Chỉ được xem nếu thuộc chi nhánh mình
     * - CUSTOMER: Chỉ được xem nếu là lịch hẹn của mình
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Integer id) {
        DatLich datLich = datLichRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + id));

        validateViewPermission(datLich);
        return mapToAppointmentResponse(datLich);
    }

    /**
     * Tạo lịch hẹn mới:
     * - CUSTOMER: Khách hàng được trích xuất từ JWT SecurityContext, chỉ được đặt lịch cho xe của mình
     * - SYSTEM_ADMIN: Có thể chỉ định maKhachHang
     * - Kiểm tra thời gian hẹn (không ở quá khứ)
     * - Kiểm tra xung đột lịch hẹn (trùng xe và thời điểm)
     */
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        KhachHang customer;
        if (isSystemAdmin(auth)) {
            if (request.getMaKhachHang() == null) {
                throw new BadRequestException("ADMIN phải cung cấp maKhachHang khi tạo lịch hẹn");
            }
            customer = khachHangRepository.findById(request.getMaKhachHang())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + request.getMaKhachHang()));
        } else if (isCustomer(auth)) {
            customer = getAuthenticatedCustomer(auth);
        } else {
            throw new AccessDeniedException("Forbidden: Chỉ khách hàng hoặc admin mới có thể tạo lịch hẹn");
        }

        // 1. Kiểm tra Xe & Quyền sở hữu xe
        Xe xe = xeRepository.findById(request.getMaXe())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + request.getMaXe()));

        if (!xe.getKhachHang().getMaKhachHang().equals(customer.getMaKhachHang())) {
            throw new AccessDeniedException("Forbidden: Bạn chỉ có thể tạo lịch hẹn cho xe thuộc sở hữu của mình");
        }

        // 2. Kiểm tra Chi nhánh
        ChiNhanh chiNhanh = chiNhanhRepository.findById(request.getMaChiNhanh())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + request.getMaChiNhanh()));

        if (Boolean.FALSE.equals(chiNhanh.getTrangThai())) {
            throw new BadRequestException("Chi nhánh hiện đang tạm ngưng hoạt động");
        }

        // 3. Kiểm tra Thời gian
        if (request.getThoiGianHen() == null || request.getThoiGianHen().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Thời gian hẹn phải ở tương lai");
        }

        // 4. Kiểm tra Xung đột lịch hẹn cho xe
        boolean hasConflict = datLichRepository.existsByXeMaXeAndThoiGianHenAndTrangThaiNotIn(
                xe.getMaXe(),
                request.getThoiGianHen(),
                List.of("HUY", "HOAN_TAT", "KHONG_DEN")
        );
        if (hasConflict) {
            throw new DuplicateResourceException("Xe '" + xe.getBienSo() + "' đã có lịch hẹn vào thời điểm: " + request.getThoiGianHen());
        }

        DatLich datLich = new DatLich();
        datLich.setKhachHang(customer);
        datLich.setXe(xe);
        datLich.setChiNhanh(chiNhanh);
        datLich.setThoiGianHen(request.getThoiGianHen());
        datLich.setTrangThai("CHO_XAC_NHAN");
        datLich.setGhiChu(request.getGhiChu());

        DatLich saved = datLichRepository.save(datLich);
        return mapToAppointmentResponse(saved);
    }

    /**
     * Hủy lịch hẹn:
     * - CUSTOMER: Chỉ hủy được lịch hẹn của mình
     * - BRANCH_MANAGER / RECEPTIONIST: Chỉ hủy được lịch hẹn chi nhánh mình
     * - SYSTEM_ADMIN: Toàn quyền
     * - Trạng thái hợp lệ để hủy: CHO_XAC_NHAN, DA_XAC_NHAN
     */
    @Transactional
    public AppointmentResponse cancelAppointment(Integer id) {
        DatLich datLich = datLichRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + id));

        validateCancelPermission(datLich);

        String currentStatus = datLich.getTrangThai();
        if ("HUY".equalsIgnoreCase(currentStatus)) {
            throw new BadRequestException("Lịch hẹn này đã được hủy trước đó");
        }
        if (!"CHO_XAC_NHAN".equalsIgnoreCase(currentStatus) && !"DA_XAC_NHAN".equalsIgnoreCase(currentStatus)) {
            throw new BadRequestException("Không thể hủy lịch hẹn đang ở trạng thái: " + currentStatus);
        }

        datLich.setTrangThai("HUY");
        DatLich updated = datLichRepository.save(datLich);
        return mapToAppointmentResponse(updated);
    }

    // --- Private Helpers ---

    private void validateViewPermission(DatLich datLich) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isSystemAdmin(auth)) {
            return;
        }

        if (isBranchStaff(auth)) {
            if (!branchAuthorizationService.isAllowedBranch(datLich.getChiNhanh().getMaChiNhanh())) {
                throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập lịch hẹn của chi nhánh khác");
            }
            return;
        }

        if (isCustomer(auth)) {
            KhachHang customer = getAuthenticatedCustomer(auth);
            if (!datLich.getKhachHang().getMaKhachHang().equals(customer.getMaKhachHang())) {
                throw new AccessDeniedException("Forbidden: Bạn không có quyền xem lịch hẹn của khách hàng khác");
            }
            return;
        }

        throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập lịch hẹn này");
    }

    private void validateCancelPermission(DatLich datLich) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isSystemAdmin(auth)) {
            return;
        }

        if (isBranchStaff(auth)) {
            if (!branchAuthorizationService.isAllowedBranch(datLich.getChiNhanh().getMaChiNhanh())) {
                throw new AccessDeniedException("Forbidden: Bạn không có quyền hủy lịch hẹn của chi nhánh khác");
            }
            return;
        }

        if (isCustomer(auth)) {
            KhachHang customer = getAuthenticatedCustomer(auth);
            if (!datLich.getKhachHang().getMaKhachHang().equals(customer.getMaKhachHang())) {
                throw new AccessDeniedException("Forbidden: Bạn không thể hủy lịch hẹn của người khác");
            }
            return;
        }

        throw new AccessDeniedException("Forbidden: Bạn không có quyền hủy lịch hẹn");
    }

    private KhachHang getAuthenticatedCustomer(Authentication auth) {
        NguoiDung user = getAuthenticatedUser(auth);
        return khachHangRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hồ sơ khách hàng cho tài khoản hiện tại."));
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản: " + username));
    }

    private boolean isSystemAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private boolean isBranchStaff(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> "ROLE_MANAGER".equals(role) || "ROLE_FRONT_DESK".equals(role));
    }

    private boolean isCustomer(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_CUSTOMER"::equals);
    }

    private AppointmentResponse mapToAppointmentResponse(DatLich dl) {
        KhachHang kh = dl.getKhachHang();
        Xe xe = dl.getXe();
        ChiNhanh cn = dl.getChiNhanh();

        String tenKhachHang = null;
        String soDienThoaiKhachHang = null;
        if (kh != null && kh.getNguoiDung() != null) {
            tenKhachHang = kh.getNguoiDung().getHoTen();
            soDienThoaiKhachHang = kh.getNguoiDung().getSoDienThoai();
        }

        return new AppointmentResponse(
                dl.getMaDatLich(),
                kh != null ? kh.getMaKhachHang() : null,
                kh != null ? kh.getMaKhachHangCode() : null,
                tenKhachHang,
                soDienThoaiKhachHang,
                xe != null ? xe.getMaXe() : null,
                xe != null ? xe.getBienSo() : null,
                xe != null ? xe.getHangXe() : null,
                xe != null ? xe.getModel() : null,
                cn != null ? cn.getMaChiNhanh() : null,
                cn != null ? cn.getMaChiNhanhCode() : null,
                cn != null ? cn.getTenChiNhanh() : null,
                dl.getThoiGianHen(),
                dl.getTrangThai(),
                dl.getGhiChu(),
                dl.getNgayDat()
        );
    }
}
