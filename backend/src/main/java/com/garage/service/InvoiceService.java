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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonDichVuRepository hoaDonDichVuRepository;
    private final HoaDonPhuTungRepository hoaDonPhuTungRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository;
    private final PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository;
    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final KhachHangRepository khachHangRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public InvoiceService(HoaDonRepository hoaDonRepository,
                          HoaDonDichVuRepository hoaDonDichVuRepository,
                          HoaDonPhuTungRepository hoaDonPhuTungRepository,
                          ThanhToanRepository thanhToanRepository,
                          PhieuSuaChuaRepository phieuSuaChuaRepository,
                          PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository,
                          PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository,
                          NhanVienRepository nhanVienRepository,
                          NguoiDungRepository nguoiDungRepository,
                          KhachHangRepository khachHangRepository,
                          BranchAuthorizationService branchAuthorizationService) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonDichVuRepository = hoaDonDichVuRepository;
        this.hoaDonPhuTungRepository = hoaDonPhuTungRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.phieuSuaChuaDichVuRepository = phieuSuaChuaDichVuRepository;
        this.phieuSuaChuaPhuTungRepository = phieuSuaChuaPhuTungRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.khachHangRepository = khachHangRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Tạo hóa đơn từ phiếu sửa chữa:
     * - Chống duplicate hóa đơn cho cùng 1 repair order
     * - Tập hợp dịch vụ & phụ tùng đã thực hiện
     * - Tính toán tổng tiền, giảm giá, thuế và thành tiền phía server
     * - Ghi nhận nhân viên thu ngân nếu có
     */
    @Transactional
    public InvoiceResponse createInvoice(Integer repairOrderId, CreateInvoiceRequest request) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa ID: " + repairOrderId));

        validateStaffAccessAuthorization(order.getChiNhanh().getMaChiNhanh());

        if ("HUY".equalsIgnoreCase(order.getTrangThai())) {
            throw new BadRequestException("Không thể tạo hóa đơn cho phiếu sửa chữa đã bị hủy");
        }

        if (hoaDonRepository.existsByPhieuSuaChuaMaPhieuSuaChua(repairOrderId)) {
            throw new DuplicateResourceException("Phiếu sửa chữa ID " + repairOrderId + " đã được tạo hóa đơn trước đó");
        }

        KhachHang customer = order.getPhieuTiepNhan() != null && order.getPhieuTiepNhan().getXe() != null
                ? order.getPhieuTiepNhan().getXe().getKhachHang()
                : null;

        if (customer == null) {
            throw new BadRequestException("Không xác định được thông tin khách hàng từ phiếu sửa chữa");
        }

        // Lấy nhân viên thu ngân từ tài khoản đang đăng nhập (nếu là nhân viên)
        NhanVien cashier = getCurrentEmployee();

        List<PhieuSuaChuaDichVu> repairServices = phieuSuaChuaDichVuRepository.findByPhieuSuaChuaMaPhieuSuaChua(repairOrderId);
        List<PhieuSuaChuaPhuTung> repairParts = phieuSuaChuaPhuTungRepository.findByPhieuSuaChuaMaPhieuSuaChua(repairOrderId);

        BigDecimal totalServiceAmount = BigDecimal.ZERO;
        for (PhieuSuaChuaDichVu s : repairServices) {
            BigDecimal lineTotal = s.getDonGia();
            totalServiceAmount = totalServiceAmount.add(lineTotal);
        }

        BigDecimal totalPartAmount = BigDecimal.ZERO;
        for (PhieuSuaChuaPhuTung p : repairParts) {
            BigDecimal lineTotal = p.getDonGia().multiply(BigDecimal.valueOf(p.getSoLuong()));
            totalPartAmount = totalPartAmount.add(lineTotal);
        }

        BigDecimal tongTien = totalServiceAmount.add(totalPartAmount);
        BigDecimal giamGia = request != null && request.getGiamGia() != null ? request.getGiamGia() : BigDecimal.ZERO;
        BigDecimal thue = request != null && request.getThue() != null ? request.getThue() : BigDecimal.ZERO;
        BigDecimal thanhTien = tongTien.subtract(giamGia).add(thue);
        if (thanhTien.compareTo(BigDecimal.ZERO) < 0) {
            thanhTien = BigDecimal.ZERO;
        }

        HoaDon invoice = new HoaDon();
        invoice.setPhieuSuaChua(order);
        invoice.setKhachHang(customer);
        invoice.setChiNhanh(order.getChiNhanh());
        invoice.setNhanVienThuNgan(cashier);
        invoice.setTongTien(tongTien);
        invoice.setGiamGia(giamGia);
        invoice.setThue(thue);
        invoice.setThanhTien(thanhTien);
        invoice.setTrangThai("CHUA_THANH_TOAN");

        HoaDon savedInvoice = hoaDonRepository.save(invoice);

        // Lưu chi tiết dịch vụ
        List<InvoiceServiceItemResponse> serviceResponses = new ArrayList<>();
        for (PhieuSuaChuaDichVu s : repairServices) {
            HoaDonDichVu item = new HoaDonDichVu();
            item.setHoaDon(savedInvoice);
            item.setDichVu(s.getDichVu());
            item.setDonGia(s.getDonGia());
            HoaDonDichVu savedItem = hoaDonDichVuRepository.save(item);

            BigDecimal lineTotal = s.getDonGia();
            serviceResponses.add(new InvoiceServiceItemResponse(
                    savedItem.getMaChiTiet(),
                    s.getDichVu().getMaDichVu(),
                    s.getDichVu().getTenDichVu(),
                    savedItem.getDonGia(),
                    lineTotal
            ));
        }

        // Lưu chi tiết phụ tùng
        List<InvoicePartItemResponse> partResponses = new ArrayList<>();
        for (PhieuSuaChuaPhuTung p : repairParts) {
            HoaDonPhuTung item = new HoaDonPhuTung();
            item.setHoaDon(savedInvoice);
            item.setPhuTung(p.getPhuTung());
            item.setSoLuong(p.getSoLuong());
            item.setDonGia(p.getDonGia());
            HoaDonPhuTung savedItem = hoaDonPhuTungRepository.save(item);

            BigDecimal lineTotal = p.getDonGia().multiply(BigDecimal.valueOf(p.getSoLuong()));
            partResponses.add(new InvoicePartItemResponse(
                    savedItem.getMaChiTiet(),
                    p.getPhuTung().getMaPhuTung(),
                    p.getPhuTung().getMaPhuTungCode(),
                    p.getPhuTung().getTenPhuTung(),
                    p.getPhuTung().getDonViTinh(),
                    savedItem.getSoLuong(),
                    savedItem.getDonGia(),
                    lineTotal
            ));
        }

        return mapInvoiceToResponse(savedInvoice, serviceResponses, partResponses, new ArrayList<>());
    }

    /**
     * Xem hóa đơn của phiếu sửa chữa
     */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByRepairOrder(Integer repairOrderId) {
        HoaDon invoice = hoaDonRepository.findByPhieuSuaChuaMaPhieuSuaChua(repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa có hóa đơn cho phiếu sửa chữa ID: " + repairOrderId));

        validateReadAuthorization(invoice);

        return loadAndMapInvoiceResponse(invoice);
    }

    /**
     * Xem chi tiết hóa đơn theo ID
     */
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Integer invoiceId) {
        HoaDon invoice = hoaDonRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + invoiceId));

        validateReadAuthorization(invoice);

        return loadAndMapInvoiceResponse(invoice);
    }

    /**
     * Lấy danh sách hóa đơn (có filter theo quyền)
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoices(Integer branchId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            NguoiDung user = nguoiDungRepository.findByTenDangNhapOrEmail(auth.getName(), auth.getName()).orElse(null);
            if (user != null) {
                KhachHang cust = khachHangRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()).orElse(null);
                if (cust != null) {
                    return hoaDonRepository.findByKhachHangMaKhachHang(cust.getMaKhachHang())
                            .stream()
                            .map(this::loadAndMapInvoiceResponse)
                            .collect(Collectors.toList());
                }
            }
            return List.of();
        }

        if (branchId != null) {
            validateStaffAccessAuthorization(branchId);
            return hoaDonRepository.findByChiNhanhMaChiNhanh(branchId)
                    .stream()
                    .map(this::loadAndMapInvoiceResponse)
                    .collect(Collectors.toList());
        }

        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return hoaDonRepository.findAll()
                    .stream()
                    .map(this::loadAndMapInvoiceResponse)
                    .collect(Collectors.toList());
        }

        NhanVien emp = getCurrentEmployee();
        if (emp != null && emp.getChiNhanh() != null) {
            return hoaDonRepository.findByChiNhanhMaChiNhanh(emp.getChiNhanh().getMaChiNhanh())
                    .stream()
                    .map(this::loadAndMapInvoiceResponse)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    // --- Helpers ---

    private void validateStaffAccessAuthorization(Integer branchId) {
        if (!branchAuthorizationService.isAllowedBranch(branchId)) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền thao tác trên chi nhánh khác");
        }
    }

    private void validateReadAuthorization(HoaDon invoice) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            validateCustomerOwnership(invoice, auth.getName());
            return;
        }
        validateStaffAccessAuthorization(invoice.getChiNhanh().getMaChiNhanh());
    }

    private void validateCustomerOwnership(HoaDon invoice, String username) {
        try {
            NguoiDung user = nguoiDungRepository.findByTenDangNhapOrEmail(username, username)
                    .orElseThrow(() -> new AccessDeniedException("Forbidden: Không tìm thấy tài khoản khách hàng"));

            KhachHang owner = invoice.getKhachHang();
            if (owner != null && owner.getNguoiDung() != null && owner.getNguoiDung().getMaNguoiDung().equals(user.getMaNguoiDung())) {
                return; // Ownership verified
            }
        } catch (Exception ignored) {}

        throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập hóa đơn của khách hàng khác");
    }

    private NhanVien getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            NguoiDung user = nguoiDungRepository.findByTenDangNhapOrEmail(auth.getName(), auth.getName()).orElse(null);
            if (user != null) {
                return nhanVienRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()).orElse(null);
            }
        }
        return null;
    }

    public InvoiceResponse loadAndMapInvoiceResponse(HoaDon invoice) {
        List<InvoiceServiceItemResponse> services = hoaDonDichVuRepository.findByHoaDonMaHoaDon(invoice.getMaHoaDon())
                .stream()
                .map(item -> {
                    BigDecimal lineTotal = item.getDonGia();
                    return new InvoiceServiceItemResponse(
                            item.getMaChiTiet(),
                            item.getDichVu().getMaDichVu(),
                            item.getDichVu().getTenDichVu(),
                            item.getDonGia(),
                            lineTotal
                    );
                })
                .collect(Collectors.toList());

        List<InvoicePartItemResponse> parts = hoaDonPhuTungRepository.findByHoaDonMaHoaDon(invoice.getMaHoaDon())
                .stream()
                .map(item -> {
                    BigDecimal lineTotal = item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong()));
                    return new InvoicePartItemResponse(
                            item.getMaChiTiet(),
                            item.getPhuTung().getMaPhuTung(),
                            item.getPhuTung().getMaPhuTungCode(),
                            item.getPhuTung().getTenPhuTung(),
                            item.getPhuTung().getDonViTinh(),
                            item.getSoLuong(),
                            item.getDonGia(),
                            lineTotal
                    );
                })
                .collect(Collectors.toList());

        List<PaymentResponse> payments = thanhToanRepository.findByHoaDonMaHoaDon(invoice.getMaHoaDon())
                .stream()
                .map(p -> new PaymentResponse(
                        p.getMaThanhToan(),
                        p.getHoaDon().getMaHoaDon(),
                        p.getSoTien(),
                        p.getPhuongThuc(),
                        p.getMaGiaoDich(),
                        p.getThoiGianThanhToan(),
                        p.getTrangThai()
                ))
                .collect(Collectors.toList());

        return mapInvoiceToResponse(invoice, services, parts, payments);
    }

    private InvoiceResponse mapInvoiceToResponse(HoaDon invoice,
                                                 List<InvoiceServiceItemResponse> services,
                                                 List<InvoicePartItemResponse> parts,
                                                 List<PaymentResponse> payments) {
        BigDecimal daThanhToan = payments.stream()
                .filter(p -> "THANH_CONG".equalsIgnoreCase(p.getTrangThai()))
                .map(PaymentResponse::getSoTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal conLai = invoice.getThanhTien().subtract(daThanhToan);
        if (conLai.compareTo(BigDecimal.ZERO) < 0) {
            conLai = BigDecimal.ZERO;
        }

        KhachHang kh = invoice.getKhachHang();
        String tenKh = (kh != null && kh.getNguoiDung() != null) ? kh.getNguoiDung().getHoTen() : null;
        ChiNhanh cn = invoice.getChiNhanh();
        String tenCn = cn != null ? cn.getTenChiNhanh() : null;
        NhanVien cashier = invoice.getNhanVienThuNgan();
        String tenCashier = (cashier != null && cashier.getNguoiDung() != null) ? cashier.getNguoiDung().getHoTen() : null;

        return new InvoiceResponse(
                invoice.getMaHoaDon(),
                invoice.getPhieuSuaChua() != null ? invoice.getPhieuSuaChua().getMaPhieuSuaChua() : null,
                kh != null ? kh.getMaKhachHang() : null,
                tenKh,
                cn != null ? cn.getMaChiNhanh() : null,
                tenCn,
                cashier != null ? cashier.getMaNhanVien() : null,
                tenCashier,
                invoice.getTongTien(),
                invoice.getGiamGia(),
                invoice.getThue(),
                invoice.getThanhTien(),
                daThanhToan,
                conLai,
                invoice.getTrangThai(),
                invoice.getNgayLap(),
                services,
                parts,
                payments
        );
    }
}
