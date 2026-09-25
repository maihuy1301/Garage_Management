package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final DatLichDichVuRepository datLichDichVuRepository;
    private final PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository;
    private final PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository;
    private final DichVuPhuTungRepository dichVuPhuTungRepository;
    private final KhachHangRepository khachHangRepository;
    private final XeRepository xeRepository;
    private final DichVuRepository dichVuRepository;
    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final ChiNhanhRepository chiNhanhRepository;
    private final BranchAuthorizationService branchAuthorizationService;
    private final CustomerProgressNotifier customerProgressNotifier;

    public RepairOrderService(PhieuSuaChuaRepository phieuSuaChuaRepository,
                              PhieuTiepNhanRepository phieuTiepNhanRepository,
                              DatLichDichVuRepository datLichDichVuRepository,
                              PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository,
                              PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository,
                              DichVuPhuTungRepository dichVuPhuTungRepository,
                              KhachHangRepository khachHangRepository,
                              XeRepository xeRepository,
                              DichVuRepository dichVuRepository,
                              NhanVienRepository nhanVienRepository,
                              NguoiDungRepository nguoiDungRepository,
                              ChiNhanhRepository chiNhanhRepository,
                              BranchAuthorizationService branchAuthorizationService,
                              CustomerProgressNotifier customerProgressNotifier) {
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.phieuTiepNhanRepository = phieuTiepNhanRepository;
        this.datLichDichVuRepository = datLichDichVuRepository;
        this.phieuSuaChuaDichVuRepository = phieuSuaChuaDichVuRepository;
        this.phieuSuaChuaPhuTungRepository = phieuSuaChuaPhuTungRepository;
        this.dichVuPhuTungRepository = dichVuPhuTungRepository;
        this.khachHangRepository = khachHangRepository;
        this.xeRepository = xeRepository;
        this.dichVuRepository = dichVuRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.chiNhanhRepository = chiNhanhRepository;
        this.branchAuthorizationService = branchAuthorizationService;
        this.customerProgressNotifier = customerProgressNotifier;
    }

    /**
     * Tạo phiếu sửa chữa từ phiếu tiếp nhận:
     * - Kiểm tra phiếu tiếp nhận tồn tại & hợp lệ
     * - Idempotency: Không cho tạo trùng phiếu sửa chữa chính cho cùng phiếu tiếp nhận (nếu không phải sub-repair order)
     * - Hỗ trợ maPhieuCha cho sub-repair order
     * - Branch authorization: Kiểm tra quyền truy cập chi nhánh
     * - Tự động liên kết ChiNhanh từ PhieuTiepNhan
     * - Tự động sao chép dịch vụ từ DatLich sang PhieuSuaChua_DichVu và phụ tùng sang PhieuSuaChua_PhuTung
     */
    @Transactional
    public RepairOrderResponse createRepairOrder(CreateRepairOrderRequest request) {
        // 1. Tìm phiếu tiếp nhận
        PhieuTiepNhan reception = phieuTiepNhanRepository.findForHandover(request.getMaTiepNhan())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu tiếp nhận với ID: " + request.getMaTiepNhan()));

        // 2. Branch Authorization
        if (!branchAuthorizationService.isAllowedBranch(reception.getChiNhanh().getMaChiNhanh())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền tạo phiếu sửa chữa thuộc chi nhánh khác");
        }

        // 3. Kiểm tra trạng thái phiếu tiếp nhận
        if ("DA_BAN_GIAO".equals(reception.getTrangThai()) || "HOAN_TAT".equals(reception.getTrangThai())) {
            throw new BadRequestException("Xe đã bàn giao, cần tạo lượt tiếp nhận mới để sửa chữa");
        }
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

        // 6. Tự động sao chép dịch vụ và phụ tùng từ lịch hẹn (nếu có)
        if (reception.getDatLich() != null) {
            Integer maDatLich = reception.getDatLich().getMaDatLich();
            List<DatLichDichVu> apptServices = datLichDichVuRepository.findByDatLichMaDatLich(maDatLich);
            for (DatLichDichVu apptSvc : apptServices) {
                DichVu dv = apptSvc.getDichVu();
                if (dv != null) {
                    if (!phieuSuaChuaDichVuRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndDichVuMaDichVu(saved.getMaPhieuSuaChua(), dv.getMaDichVu())) {
                        BigDecimal price = (dv.getDonGia() != null) ? dv.getDonGia() : BigDecimal.ZERO;

                        PhieuSuaChuaDichVu roService = new PhieuSuaChuaDichVu();
                        roService.setPhieuSuaChua(saved);
                        roService.setDichVu(dv);
                        roService.setSoLuong(1);
                        roService.setDonGia(price);
                        roService.setTrangThai("CHO_XU_LY");
                        PhieuSuaChuaDichVu savedRoService = phieuSuaChuaDichVuRepository.save(roService);

                        // Sao chép phụ tùng tiêu chuẩn theo DichVu_PhuTung nếu có
                        List<DichVuPhuTung> standardParts = dichVuPhuTungRepository.findByDichVuMaDichVu(dv.getMaDichVu());
                        for (DichVuPhuTung standardPart : standardParts) {
                            PhuTung pt = standardPart.getPhuTung();
                            if (pt != null && !phieuSuaChuaPhuTungRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndPhuTungMaPhuTungAndDichVuChiTietMaChiTiet(saved.getMaPhieuSuaChua(), pt.getMaPhuTung(), savedRoService.getMaChiTiet())) {
                                int partQty = (standardPart.getSoLuong() != null && standardPart.getSoLuong() >= 1) ? standardPart.getSoLuong() : 1;
                                BigDecimal partPrice = (pt.getGiaBan() != null) ? pt.getGiaBan() : BigDecimal.ZERO;

                                PhieuSuaChuaPhuTung roPart = new PhieuSuaChuaPhuTung();
                                roPart.setPhieuSuaChua(saved);
                                roPart.setPhuTung(pt);
                                roPart.setDichVuChiTiet(savedRoService);
                                roPart.setSoLuong(partQty);
                                roPart.setDonGia(partPrice);
                                phieuSuaChuaPhuTungRepository.save(roPart);
                            }
                        }
                    }
                }
            }
        }

        customerProgressNotifier.repairChanged(saved, null);
        return mapToRepairOrderResponse(saved);
    }

    /**
     * Chủ động tạo phiếu sửa chữa (không bắt đầu từ lịch hẹn) hoặc tạo phiếu phát sinh sửa chữa:
     * - Validate khách hàng, xe thuộc khách hàng
     * - Hỗ trợ maPhieuCha (OPTIONAL) cho trường hợp phát sinh sửa chữa
     * - Kế thừa tiếp nhận của phiếu cha hoặc tạo tiếp nhận trực tiếp
     * - Tạo các bản ghi PhieuSuaChua_DichVu theo danh sách dịch vụ chọn
     * - Tạo các bản ghi PhieuSuaChua_PhuTung định mức theo DichVu_PhuTung (KHÔNG TRỪ TỒN KHO)
     * - Tuân thủ Branch Authorization và Transactional
     */
    @Transactional
    public RepairOrderResponse createDirectRepairOrder(CreateDirectRepairOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        NguoiDung currentUser = getAuthenticatedUser(auth);

        // 1. Validate Khách hàng
        KhachHang customer = khachHangRepository.findById(request.getMaKhachHang())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + request.getMaKhachHang()));

        // 2. Validate Xe và quan hệ sở hữu của Xe với Khách hàng
        Xe vehicle = xeRepository.findById(request.getMaXe())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + request.getMaXe()));

        if (vehicle.getKhachHang() == null || !vehicle.getKhachHang().getMaKhachHang().equals(customer.getMaKhachHang())) {
            throw new BadRequestException("Xe biển số '" + vehicle.getBienSo() + "' không thuộc sở hữu của khách hàng đã chọn");
        }

        // 3. Xử lý MaPhieuCha (nếu có - trường hợp phiếu phát sinh)
        PhieuSuaChua parentOrder = null;
        ChiNhanh branch;
        PhieuTiepNhan reception;

        if (request.getMaPhieuCha() != null) {
            parentOrder = phieuSuaChuaRepository.findById(request.getMaPhieuCha())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa cha ID: " + request.getMaPhieuCha()));

            // Branch Authorization đối với phiếu cha
            if (!branchAuthorizationService.isAllowedBranch(parentOrder.getChiNhanh().getMaChiNhanh())) {
                throw new AccessDeniedException("Forbidden: Bạn không có quyền thao tác trên phiếu sửa chữa của chi nhánh khác");
            }

            // Kiểm tra trạng thái phiếu cha
            if ("HUY".equalsIgnoreCase(parentOrder.getTrangThai())) {
                throw new BadRequestException("Không thể tạo phiếu phát sinh từ phiếu sửa chữa đã bị hủy");
            }

            // Kiểm tra phiếu cha có thuộc đúng xe và đúng khách hàng không
            PhieuTiepNhan parentReception = parentOrder.getPhieuTiepNhan();
            if (parentReception == null || parentReception.getXe() == null || !parentReception.getXe().getMaXe().equals(vehicle.getMaXe())) {
                throw new BadRequestException("Phiếu sửa chữa cha không thuộc xe đã chọn");
            }
            if (parentReception.getXe().getKhachHang() == null || !parentReception.getXe().getKhachHang().getMaKhachHang().equals(customer.getMaKhachHang())) {
                throw new BadRequestException("Phiếu sửa chữa cha không thuộc khách hàng đã chọn");
            }

            // Tránh vòng lặp tham chiếu cha-con (kiểm tra chuỗi ancestor)
            PhieuSuaChua ancestor = parentOrder;
            Set<Integer> visited = new java.util.HashSet<>();
            while (ancestor != null) {
                if (!visited.add(ancestor.getMaPhieuSuaChua())) {
                    throw new BadRequestException("Phát hiện cấu trúc lặp vòng giữa các phiếu sửa chữa cha-con");
                }
                ancestor = ancestor.getPhieuCha();
            }

            branch = parentOrder.getChiNhanh();
            reception = parentOrder.getPhieuTiepNhan();
        } else {
            // Trường hợp độc lập (Walk-in trực tiếp)
            Integer userBranchId = branchAuthorizationService.resolveUserBranchId(auth)
                    .orElse(null);

            if (userBranchId != null) {
                branch = chiNhanhRepository.findById(userBranchId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh ID: " + userBranchId));
            } else {
                List<ChiNhanh> branchList = chiNhanhRepository.findAll();
                if (branchList.isEmpty()) {
                    throw new BadRequestException("Hệ thống chưa có chi nhánh nào được thiết lập");
                }
                branch = branchList.get(0);
            }

            if (!branchAuthorizationService.isAllowedBranch(branch.getMaChiNhanh())) {
                throw new AccessDeniedException("Forbidden: Bạn không có quyền tạo phiếu tại chi nhánh này");
            }

            NhanVien receptionist = resolveReceptionistStaff(currentUser, branch);

            // Tạo phiếu tiếp nhận trực tiếp (MaDatLich = NULL)
            PhieuTiepNhan directReception = new PhieuTiepNhan();
            directReception.setDatLich(null);
            directReception.setXe(vehicle);
            directReception.setChiNhanh(branch);
            directReception.setNhanVienTiepNhan(receptionist);
            directReception.setTrangThai("DA_TIEP_NHAN");
            directReception.setThoiGianTiepNhan(LocalDateTime.now());
            directReception.setSoKm(request.getSoKm());
            directReception.setTinhTrangNgoaiThat(request.getGhiChu());
            directReception.setYeuCauKhachHang(request.getYeuCauKhachHang());

            if (request.getSoKm() != null && request.getSoKm() > 0) {
                if (vehicle.getSoKmHienTai() == null || request.getSoKm() > vehicle.getSoKmHienTai()) {
                    vehicle.setSoKmHienTai(request.getSoKm());
                    xeRepository.save(vehicle);
                }
            }

            reception = phieuTiepNhanRepository.save(directReception);
        }

        // 4. Tạo PhieuSuaChua
        PhieuSuaChua order = new PhieuSuaChua();
        order.setPhieuTiepNhan(reception);
        order.setChiNhanh(branch);
        order.setPhieuCha(parentOrder);
        order.setThoiGianBatDau(request.getThoiGianBatDau() != null ? request.getThoiGianBatDau() : LocalDateTime.now());
        order.setTrangThai("CHO_XU_LY");
        order.setGhiChu(request.getGhiChu());

        PhieuSuaChua saved = phieuSuaChuaRepository.save(order);

        // 5. Tạo PhieuSuaChua_DichVu và PhieuSuaChua_PhuTung định mức (KHÔNG TRỪ TỒN KHO)
        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            List<Integer> distinctServiceIds = request.getServiceIds().stream().distinct().toList();
            for (Integer serviceId : distinctServiceIds) {
                DichVu dv = dichVuRepository.findById(serviceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + serviceId));

                if (Boolean.FALSE.equals(dv.getTrangThai())) {
                    throw new BadRequestException("Dịch vụ '" + dv.getTenDichVu() + "' hiện đang tạm ngưng hoạt động");
                }

                BigDecimal price = (dv.getDonGia() != null) ? dv.getDonGia() : BigDecimal.ZERO;

                PhieuSuaChuaDichVu roService = new PhieuSuaChuaDichVu();
                roService.setPhieuSuaChua(saved);
                roService.setDichVu(dv);
                roService.setSoLuong(1);
                roService.setDonGia(price);
                roService.setTrangThai("CHO_XU_LY");
                PhieuSuaChuaDichVu savedRoService = phieuSuaChuaDichVuRepository.save(roService);

                // Sao chép phụ tùng tiêu chuẩn theo DichVu_PhuTung (định mức)
                List<DichVuPhuTung> standardParts = dichVuPhuTungRepository.findByDichVuMaDichVu(dv.getMaDichVu());
                for (DichVuPhuTung standardPart : standardParts) {
                    PhuTung pt = standardPart.getPhuTung();
                    if (pt != null && !phieuSuaChuaPhuTungRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndPhuTungMaPhuTungAndDichVuChiTietMaChiTiet(saved.getMaPhieuSuaChua(), pt.getMaPhuTung(), savedRoService.getMaChiTiet())) {
                        int partQty = (standardPart.getSoLuong() != null && standardPart.getSoLuong() >= 1) ? standardPart.getSoLuong() : 1;
                        BigDecimal partPrice = (pt.getGiaBan() != null) ? pt.getGiaBan() : BigDecimal.ZERO;

                        PhieuSuaChuaPhuTung roPart = new PhieuSuaChuaPhuTung();
                        roPart.setPhieuSuaChua(saved);
                        roPart.setPhuTung(pt);
                        roPart.setDichVuChiTiet(savedRoService);
                        roPart.setSoLuong(partQty);
                        roPart.setDonGia(partPrice);
                        phieuSuaChuaPhuTungRepository.save(roPart);
                    }
                }
            }
        }

        customerProgressNotifier.repairChanged(saved, null);
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
        customerProgressNotifier.repairChanged(updated, currentStatus);
        return mapToRepairOrderResponse(updated);
    }

    // --- Private Helpers ---

    private NguoiDung getAuthenticatedUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Yêu cầu xác thực tài khoản");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof com.garage.security.CustomUserDetails) {
            return ((com.garage.security.CustomUserDetails) principal).getNguoiDung();
        }
        String username = auth.getName();
        return nguoiDungRepository.findByTenDangNhapOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + username));
    }

    private NhanVien resolveReceptionistStaff(NguoiDung user, ChiNhanh branch) {
        return nhanVienRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                .orElseGet(() -> {
                    List<NhanVien> staffList = nhanVienRepository.findByChiNhanhMaChiNhanh(branch.getMaChiNhanh());
                    if (!staffList.isEmpty()) {
                        return staffList.get(0);
                    }
                    throw new BadRequestException("Không tìm thấy nhân viên nào thuộc chi nhánh " + branch.getTenChiNhanh() + " để tiếp nhận");
                });
    }

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
