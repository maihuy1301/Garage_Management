package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final HoaDonRepository hoaDonRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final DatLichRepository datLichRepository;
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository;
    private final PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository;
    private final TonKhoRepository tonKhoRepository;
    private final PhanCongRepository phanCongRepository;
    private final ChiNhanhRepository chiNhanhRepository;
    private final KhachHangRepository khachHangRepository;
    private final XeRepository xeRepository;
    private final NhanVienRepository nhanVienRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public ReportService(HoaDonRepository hoaDonRepository,
                         ThanhToanRepository thanhToanRepository,
                         DatLichRepository datLichRepository,
                         PhieuSuaChuaRepository phieuSuaChuaRepository,
                         PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository,
                         PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository,
                         TonKhoRepository tonKhoRepository,
                         PhanCongRepository phanCongRepository,
                         ChiNhanhRepository chiNhanhRepository,
                         KhachHangRepository khachHangRepository,
                         XeRepository xeRepository,
                         NhanVienRepository nhanVienRepository,
                         BranchAuthorizationService branchAuthorizationService) {
        this.hoaDonRepository = hoaDonRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.datLichRepository = datLichRepository;
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.phieuSuaChuaDichVuRepository = phieuSuaChuaDichVuRepository;
        this.phieuSuaChuaPhuTungRepository = phieuSuaChuaPhuTungRepository;
        this.tonKhoRepository = tonKhoRepository;
        this.phanCongRepository = phanCongRepository;
        this.chiNhanhRepository = chiNhanhRepository;
        this.khachHangRepository = khachHangRepository;
        this.xeRepository = xeRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * A. Dashboard tổng quan
     */
    public DashboardResponse getDashboardReport(String branchCode, LocalDate from, LocalDate to) {
        Optional<ChiNhanh> branchOpt = resolveBranchAndValidateDates(branchCode, from, to);

        List<HoaDon> invoices = filterInvoices(branchOpt, from, to);
        List<ThanhToan> payments = filterPayments(branchOpt, from, to);
        List<DatLich> appointments = filterAppointments(branchOpt, from, to);
        List<PhieuSuaChua> repairOrders = filterRepairOrders(branchOpt, from, to);

        BigDecimal totalRevenue = invoices.stream()
                .filter(h -> "DA_THANH_TOAN".equalsIgnoreCase(h.getTrangThai()))
                .map(h -> h.getThanhTien() != null ? h.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long inProgress = repairOrders.stream()
                .filter(r -> "DANG_SUA".equalsIgnoreCase(r.getTrangThai()))
                .count();

        long completed = repairOrders.stream()
                .filter(r -> "HOAN_TAT".equalsIgnoreCase(r.getTrangThai()))
                .count();

        long totalCustomers = khachHangRepository.count();
        long totalVehicles = xeRepository.count();

        return new DashboardResponse(
                totalRevenue,
                invoices.size(),
                payments.size(),
                appointments.size(),
                repairOrders.size(),
                inProgress,
                completed,
                totalCustomers,
                totalVehicles
        );
    }

    /**
     * B. Thống kê doanh thu
     */
    public RevenueReportResponse getRevenueReport(String branchCode, LocalDate from, LocalDate to) {
        Optional<ChiNhanh> branchOpt = resolveBranchAndValidateDates(branchCode, from, to);

        List<HoaDon> invoices = filterInvoices(branchOpt, from, to);
        List<ThanhToan> payments = filterPayments(branchOpt, from, to);

        BigDecimal totalRevenue = invoices.stream()
                .filter(h -> "DA_THANH_TOAN".equalsIgnoreCase(h.getTrangThai()))
                .map(h -> h.getThanhTien() != null ? h.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = payments.stream()
                .filter(t -> "THANH_CONG".equalsIgnoreCase(t.getTrangThai()))
                .map(t -> t.getSoTien() != null ? t.getSoTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnpaid = invoices.stream()
                .filter(h -> !"DA_THANH_TOAN".equalsIgnoreCase(h.getTrangThai()))
                .map(h -> h.getThanhTien() != null ? h.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by Date (period)
        Map<String, List<HoaDon>> byDate = invoices.stream()
                .filter(h -> h.getNgayLap() != null)
                .collect(Collectors.groupingBy(
                        h -> h.getNgayLap().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<RevenuePeriodResponse> periods = new ArrayList<>();
        for (Map.Entry<String, List<HoaDon>> entry : byDate.entrySet()) {
            BigDecimal rev = entry.getValue().stream()
                    .filter(h -> "DA_THANH_TOAN".equalsIgnoreCase(h.getTrangThai()))
                    .map(h -> h.getThanhTien() != null ? h.getThanhTien() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            periods.add(new RevenuePeriodResponse(entry.getKey(), rev, entry.getValue().size()));
        }

        return new RevenueReportResponse(totalRevenue, invoices.size(), totalPaid, totalUnpaid, periods);
    }

    /**
     * D. Thống kê lịch hẹn
     */
    public AppointmentReportResponse getAppointmentReport(String branchCode, LocalDate from, LocalDate to) {
        Optional<ChiNhanh> branchOpt = resolveBranchAndValidateDates(branchCode, from, to);

        List<DatLich> appointments = filterAppointments(branchOpt, from, to);

        long choXacNhan = appointments.stream().filter(a -> "CHO_XAC_NHAN".equalsIgnoreCase(a.getTrangThai())).count();
        long daXacNhan = appointments.stream().filter(a -> "DA_XAC_NHAN".equalsIgnoreCase(a.getTrangThai())).count();
        long daTiepNhan = appointments.stream().filter(a -> "DA_TIEP_NHAN".equalsIgnoreCase(a.getTrangThai())).count();
        long daHuy = appointments.stream().filter(a -> "DA_HUY".equalsIgnoreCase(a.getTrangThai())).count();

        return new AppointmentReportResponse(appointments.size(), choXacNhan, daXacNhan, daTiepNhan, daHuy);
    }

    /**
     * E. Thống kê phiếu sửa chữa
     */
    public RepairOrderReportResponse getRepairOrderReport(String branchCode, LocalDate from, LocalDate to) {
        Optional<ChiNhanh> branchOpt = resolveBranchAndValidateDates(branchCode, from, to);

        List<PhieuSuaChua> repairOrders = filterRepairOrders(branchOpt, from, to);

        long choXuLy = repairOrders.stream().filter(r -> "CHO_XU_LY".equalsIgnoreCase(r.getTrangThai())).count();
        long daPhanCong = repairOrders.stream().filter(r -> "DA_PHAN_CONG".equalsIgnoreCase(r.getTrangThai())).count();
        long dangSua = repairOrders.stream().filter(r -> "DANG_SUA".equalsIgnoreCase(r.getTrangThai())).count();
        long choKhachDuyet = repairOrders.stream().filter(r -> "CHO_KH_DUYET".equalsIgnoreCase(r.getTrangThai())).count();
        long tamDung = repairOrders.stream().filter(r -> "TAM_DUNG".equalsIgnoreCase(r.getTrangThai())).count();
        long hoanTat = repairOrders.stream().filter(r -> "HOAN_TAT".equalsIgnoreCase(r.getTrangThai())).count();
        long huy = repairOrders.stream().filter(r -> "HUY".equalsIgnoreCase(r.getTrangThai())).count();

        return new RepairOrderReportResponse(repairOrders.size(), choXuLy, daPhanCong, dangSua, choKhachDuyet, tamDung, hoanTat, huy);
    }

    /**
     * F. Thống kê dịch vụ sử dụng nhiều nhất
     */
    public List<ServiceReportResponse> getServiceReport(String branchCode, LocalDate from, LocalDate to, Integer limit) {
        Optional<ChiNhanh> branchOpt = resolveBranchAndValidateDates(branchCode, from, to);
        int maxResults = (limit != null && limit > 0) ? limit : 10;

        List<PhieuSuaChuaDichVu> items = phieuSuaChuaDichVuRepository.findAll();
        if (branchOpt.isPresent()) {
            Integer branchId = branchOpt.get().getMaChiNhanh();
            items = items.stream()
                    .filter(i -> i.getPhieuSuaChua() != null && i.getPhieuSuaChua().getChiNhanh() != null &&
                            branchId.equals(i.getPhieuSuaChua().getChiNhanh().getMaChiNhanh()))
                    .collect(Collectors.toList());
        }

        Map<Integer, List<PhieuSuaChuaDichVu>> byService = items.stream()
                .filter(i -> i.getDichVu() != null)
                .collect(Collectors.groupingBy(i -> i.getDichVu().getMaDichVu()));

        return byService.entrySet().stream()
                .map(e -> {
                    DichVu dv = e.getValue().get(0).getDichVu();
                    long count = e.getValue().stream().mapToLong(i -> i.getSoLuong() != null ? i.getSoLuong() : 1).sum();
                    BigDecimal rev = e.getValue().stream()
                            .map(i -> (i.getDonGia() != null ? i.getDonGia() : BigDecimal.ZERO)
                                    .multiply(BigDecimal.valueOf(i.getSoLuong() != null ? i.getSoLuong() : 1)))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ServiceReportResponse(dv.getMaDichVu(), dv.getTenDichVu(), count, rev);
                })
                .sorted((a, b) -> Long.compare(b.getSoLanSuDung(), a.getSoLanSuDung()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * G. Thống kê phụ tùng sử dụng nhiều nhất
     */
    public List<PartReportResponse> getPartReport(String branchCode, LocalDate from, LocalDate to, Integer limit) {
        Optional<ChiNhanh> branchOpt = resolveBranchAndValidateDates(branchCode, from, to);
        int maxResults = (limit != null && limit > 0) ? limit : 10;

        List<PhieuSuaChuaPhuTung> items = phieuSuaChuaPhuTungRepository.findAll();
        if (branchOpt.isPresent()) {
            Integer branchId = branchOpt.get().getMaChiNhanh();
            items = items.stream()
                    .filter(i -> i.getPhieuSuaChua() != null && i.getPhieuSuaChua().getChiNhanh() != null &&
                            branchId.equals(i.getPhieuSuaChua().getChiNhanh().getMaChiNhanh()))
                    .collect(Collectors.toList());
        }

        Map<Integer, List<PhieuSuaChuaPhuTung>> byPart = items.stream()
                .filter(i -> i.getPhuTung() != null)
                .collect(Collectors.groupingBy(i -> i.getPhuTung().getMaPhuTung()));

        return byPart.entrySet().stream()
                .map(e -> {
                    PhuTung pt = e.getValue().get(0).getPhuTung();
                    long count = e.getValue().stream().mapToLong(i -> i.getSoLuong() != null ? i.getSoLuong() : 1).sum();
                    BigDecimal rev = e.getValue().stream()
                            .map(i -> (i.getDonGia() != null ? i.getDonGia() : BigDecimal.ZERO)
                                    .multiply(BigDecimal.valueOf(i.getSoLuong() != null ? i.getSoLuong() : 1)))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new PartReportResponse(pt.getMaPhuTung(), pt.getMaPhuTungCode(), pt.getTenPhuTung(), count, rev);
                })
                .sorted((a, b) -> Long.compare(b.getSoLuongSuDung(), a.getSoLuongSuDung()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * G2. Thống kê tồn kho
     */
    public List<InventoryReportResponse> getInventoryReport(String branchCode) {
        Optional<ChiNhanh> branchOpt = resolveBranchAndValidateDates(branchCode, null, null);

        List<TonKho> list;
        if (branchOpt.isPresent()) {
            list = tonKhoRepository.findByIdMaChiNhanh(branchOpt.get().getMaChiNhanh());
        } else {
            list = tonKhoRepository.findAll();
        }

        return list.stream()
                .map(t -> {
                    PhuTung pt = t.getPhuTung();
                    BigDecimal donGia = (pt != null && pt.getGiaBan() != null) ? pt.getGiaBan() : BigDecimal.ZERO;
                    int slTon = t.getSoLuongTon() != null ? t.getSoLuongTon() : 0;
                    int slMin = t.getSoLuongToiThieu() != null ? t.getSoLuongToiThieu() : 0;
                    BigDecimal giaTri = donGia.multiply(BigDecimal.valueOf(slTon));
                    boolean sapHet = slTon <= slMin;

                    return new InventoryReportResponse(
                            pt != null ? pt.getMaPhuTung() : null,
                            pt != null ? pt.getMaPhuTungCode() : null,
                            pt != null ? pt.getTenPhuTung() : null,
                            donGia,
                            slTon,
                            slMin,
                            giaTri,
                            sapHet
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * H. Thống kê kỹ thuật viên
     */
    public List<TechnicianReportResponse> getTechnicianReport(String branchCode, LocalDate from, LocalDate to) {
        Optional<ChiNhanh> branchOpt = resolveBranchAndValidateDates(branchCode, from, to);

        List<NhanVien> technicians = nhanVienRepository.findAll().stream()
                .filter(nv -> nv.getChucVu() != null &&
                        (nv.getChucVu().toLowerCase().contains("kỹ thuật") || nv.getChucVu().toLowerCase().contains("technician")))
                .collect(Collectors.toList());

        if (branchOpt.isPresent()) {
            Integer branchId = branchOpt.get().getMaChiNhanh();
            technicians = technicians.stream()
                    .filter(nv -> nv.getChiNhanh() != null && branchId.equals(nv.getChiNhanh().getMaChiNhanh()))
                    .collect(Collectors.toList());
        }

        List<PhanCong> assignments = phanCongRepository.findAll();

        return technicians.stream()
                .map(tech -> {
                    List<PhanCong> myAssigns = assignments.stream()
                            .filter(a -> a.getNhanVien() != null && tech.getMaNhanVien().equals(a.getNhanVien().getMaNhanVien()))
                            .collect(Collectors.toList());

                    long totalAssign = myAssigns.size();
                    long completed = myAssigns.stream()
                            .filter(a -> a.getPhieuSuaChua() != null && "HOAN_TAT".equalsIgnoreCase(a.getPhieuSuaChua().getTrangThai()))
                            .count();
                    long inProgress = myAssigns.stream()
                            .filter(a -> a.getPhieuSuaChua() != null && "DANG_SUA".equalsIgnoreCase(a.getPhieuSuaChua().getTrangThai()))
                            .count();

                    String hoTen = tech.getNguoiDung() != null ? tech.getNguoiDung().getHoTen() : tech.getMaNhanVienCode();

                    return new TechnicianReportResponse(
                            tech.getMaNhanVien(),
                            tech.getMaNhanVienCode(),
                            hoTen,
                            totalAssign,
                            completed,
                            inProgress
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * I. Thống kê theo chi nhánh
     */
    public List<BranchReportResponse> getBranchReport(LocalDate from, LocalDate to) {
        Optional<ChiNhanh> branchOpt = resolveBranchAndValidateDates(null, from, to);

        List<ChiNhanh> branches;
        if (branchOpt.isPresent()) {
            branches = List.of(branchOpt.get());
        } else {
            branches = chiNhanhRepository.findAll();
        }

        List<HoaDon> allInvoices = hoaDonRepository.findAll();
        List<DatLich> allAppointments = datLichRepository.findAll();
        List<PhieuSuaChua> allRepairs = phieuSuaChuaRepository.findAll();

        return branches.stream()
                .map(cn -> {
                    Integer branchId = cn.getMaChiNhanh();

                    BigDecimal rev = allInvoices.stream()
                            .filter(h -> h.getChiNhanh() != null && branchId.equals(h.getChiNhanh().getMaChiNhanh()))
                            .filter(h -> "DA_THANH_TOAN".equalsIgnoreCase(h.getTrangThai()))
                            .map(h -> h.getThanhTien() != null ? h.getThanhTien() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long appts = allAppointments.stream()
                            .filter(a -> a.getChiNhanh() != null && branchId.equals(a.getChiNhanh().getMaChiNhanh()))
                            .count();

                    long repairs = allRepairs.stream()
                            .filter(r -> r.getChiNhanh() != null && branchId.equals(r.getChiNhanh().getMaChiNhanh()))
                            .count();

                    long completedRepairs = allRepairs.stream()
                            .filter(r -> r.getChiNhanh() != null && branchId.equals(r.getChiNhanh().getMaChiNhanh()))
                            .filter(r -> "HOAN_TAT".equalsIgnoreCase(r.getTrangThai()))
                            .count();

                    return new BranchReportResponse(
                            cn.getMaChiNhanh(),
                            cn.getMaChiNhanhCode(),
                            cn.getTenChiNhanh(),
                            rev,
                            appts,
                            repairs,
                            completedRepairs
                    );
                })
                .collect(Collectors.toList());
    }

    // --- Helpers & Security ---

    private Optional<ChiNhanh> resolveBranchAndValidateDates(String branchCode, LocalDate from, LocalDate to) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Forbidden: Yêu cầu đăng nhập");
        }

        // Chặn Customer truy cập báo cáo quản trị
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            throw new AccessDeniedException("Forbidden: Khách hàng không có quyền truy cập báo cáo quản trị");
        }

        // Validate Date Range
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("Thời gian bắt đầu (from) phải trước hoặc bằng thời gian kết thúc (to)");
        }

        boolean isSystemAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isSystemAdmin) {
            if (branchCode != null && !branchCode.isBlank()) {
                ChiNhanh cn = chiNhanhRepository.findByMaChiNhanhCode(branchCode.trim().toUpperCase())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với mã: " + branchCode));
                return Optional.of(cn);
            }
            return Optional.empty(); // Toàn hệ thống
        }

        // Staff / Branch Manager: Enforce assigned branch
        Optional<Integer> branchIdOpt = branchAuthorizationService.resolveUserBranchId(auth);
        if (branchIdOpt.isEmpty()) {
            throw new AccessDeniedException("Forbidden: Tài khoản nhân viên không thuộc chi nhánh nào");
        }

        ChiNhanh userBranch = chiNhanhRepository.findById(branchIdOpt.get())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin chi nhánh của tài khoản"));

        if (branchCode != null && !branchCode.isBlank() && !userBranch.getMaChiNhanhCode().equalsIgnoreCase(branchCode.trim())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền xem báo cáo của chi nhánh khác");
        }

        return Optional.of(userBranch);
    }

    private List<HoaDon> filterInvoices(Optional<ChiNhanh> branchOpt, LocalDate from, LocalDate to) {
        List<HoaDon> list = hoaDonRepository.findAll();
        if (branchOpt.isPresent()) {
            Integer bId = branchOpt.get().getMaChiNhanh();
            list = list.stream()
                    .filter(h -> h.getChiNhanh() != null && bId.equals(h.getChiNhanh().getMaChiNhanh()))
                    .collect(Collectors.toList());
        }
        if (from != null) {
            LocalDateTime start = from.atStartOfDay();
            list = list.stream().filter(h -> h.getNgayLap() != null && !h.getNgayLap().isBefore(start)).collect(Collectors.toList());
        }
        if (to != null) {
            LocalDateTime end = to.atTime(LocalTime.MAX);
            list = list.stream().filter(h -> h.getNgayLap() != null && !h.getNgayLap().isAfter(end)).collect(Collectors.toList());
        }
        return list;
    }

    private List<ThanhToan> filterPayments(Optional<ChiNhanh> branchOpt, LocalDate from, LocalDate to) {
        List<ThanhToan> list = thanhToanRepository.findAll();
        if (branchOpt.isPresent()) {
            Integer bId = branchOpt.get().getMaChiNhanh();
            list = list.stream()
                    .filter(t -> t.getHoaDon() != null && t.getHoaDon().getChiNhanh() != null &&
                            bId.equals(t.getHoaDon().getChiNhanh().getMaChiNhanh()))
                    .collect(Collectors.toList());
        }
        if (from != null) {
            LocalDateTime start = from.atStartOfDay();
            list = list.stream().filter(t -> t.getThoiGianThanhToan() != null && !t.getThoiGianThanhToan().isBefore(start)).collect(Collectors.toList());
        }
        if (to != null) {
            LocalDateTime end = to.atTime(LocalTime.MAX);
            list = list.stream().filter(t -> t.getThoiGianThanhToan() != null && !t.getThoiGianThanhToan().isAfter(end)).collect(Collectors.toList());
        }
        return list;
    }

    private List<DatLich> filterAppointments(Optional<ChiNhanh> branchOpt, LocalDate from, LocalDate to) {
        List<DatLich> list = datLichRepository.findAll();
        if (branchOpt.isPresent()) {
            Integer bId = branchOpt.get().getMaChiNhanh();
            list = list.stream()
                    .filter(a -> a.getChiNhanh() != null && bId.equals(a.getChiNhanh().getMaChiNhanh()))
                    .collect(Collectors.toList());
        }
        if (from != null) {
            LocalDateTime start = from.atStartOfDay();
            list = list.stream().filter(a -> a.getThoiGianHen() != null && !a.getThoiGianHen().isBefore(start)).collect(Collectors.toList());
        }
        if (to != null) {
            LocalDateTime end = to.atTime(LocalTime.MAX);
            list = list.stream().filter(a -> a.getThoiGianHen() != null && !a.getThoiGianHen().isAfter(end)).collect(Collectors.toList());
        }
        return list;
    }

    private List<PhieuSuaChua> filterRepairOrders(Optional<ChiNhanh> branchOpt, LocalDate from, LocalDate to) {
        List<PhieuSuaChua> list = phieuSuaChuaRepository.findAll();
        if (branchOpt.isPresent()) {
            Integer bId = branchOpt.get().getMaChiNhanh();
            list = list.stream()
                    .filter(r -> r.getChiNhanh() != null && bId.equals(r.getChiNhanh().getMaChiNhanh()))
                    .collect(Collectors.toList());
        }
        if (from != null) {
            LocalDateTime start = from.atStartOfDay();
            list = list.stream().filter(r -> r.getThoiGianBatDau() != null && !r.getThoiGianBatDau().isBefore(start)).collect(Collectors.toList());
        }
        if (to != null) {
            LocalDateTime end = to.atTime(LocalTime.MAX);
            list = list.stream().filter(r -> r.getThoiGianBatDau() != null && !r.getThoiGianBatDau().isAfter(end)).collect(Collectors.toList());
        }
        return list;
    }
}
