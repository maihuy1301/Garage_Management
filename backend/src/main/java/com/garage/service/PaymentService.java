package com.garage.service;

import com.garage.dto.CreatePaymentRequest;
import com.garage.dto.PaymentResponse;
import com.garage.entity.HoaDon;
import com.garage.entity.KhachHang;
import com.garage.entity.NguoiDung;
import com.garage.entity.ThanhToan;
import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.HoaDonRepository;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.ThanhToanRepository;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final ThanhToanRepository thanhToanRepository;
    private final HoaDonRepository hoaDonRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public PaymentService(ThanhToanRepository thanhToanRepository,
                          HoaDonRepository hoaDonRepository,
                          NguoiDungRepository nguoiDungRepository,
                          BranchAuthorizationService branchAuthorizationService) {
        this.thanhToanRepository = thanhToanRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Thanh toán hóa đơn (Full hoặc Partial Payment):
     * - Kiểm tra trạng thái hóa đơn
     * - Kiểm tra số tiền thanh toán (không được vượt quá số tiền còn lại)
     * - Kiểm tra idempotency (nếu có mã giao dịch)
     * - Cập nhật trạng thái hóa đơn thành DA_THANH_TOAN khi thanh toán đủ
     */
    @Transactional
    public PaymentResponse createPayment(Integer invoiceId, CreatePaymentRequest request) {
        HoaDon invoice = hoaDonRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + invoiceId));

        validateStaffAccessAuthorization(invoice.getChiNhanh().getMaChiNhanh());

        String status = invoice.getTrangThai();
        if ("DA_THANH_TOAN".equalsIgnoreCase(status)) {
            throw new BadRequestException("Hóa đơn ID " + invoiceId + " đã được thanh toán đầy đủ");
        }
        if ("HUY".equalsIgnoreCase(status)) {
            throw new BadRequestException("Không thể thanh toán cho hóa đơn đã bị hủy");
        }

        if (request.getSoTien() == null || request.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Số tiền thanh toán phải lớn hơn 0");
        }

        List<ThanhToan> existingPayments = thanhToanRepository.findByHoaDonMaHoaDon(invoiceId);
        BigDecimal paidSoFar = existingPayments.stream()
                .filter(p -> "THANH_CONG".equalsIgnoreCase(p.getTrangThai()))
                .map(ThanhToan::getSoTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = invoice.getThanhTien().subtract(paidSoFar);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        if (request.getSoTien().compareTo(remaining) > 0) {
            throw new BadRequestException("Số tiền thanh toán (" + request.getSoTien()
                    + ") vượt quá số tiền còn lại cần thanh toán (" + remaining + ")");
        }

        if (request.getMaGiaoDich() != null && !request.getMaGiaoDich().trim().isEmpty()) {
            String txCode = request.getMaGiaoDich().trim();
            if (thanhToanRepository.existsByMaGiaoDichAndTrangThai(txCode, "THANH_CONG")) {
                throw new BadRequestException("Giao dịch với mã giao dịch '" + txCode + "' đã được xử lý trước đó");
            }
        }

        ThanhToan payment = new ThanhToan();
        payment.setHoaDon(invoice);
        payment.setSoTien(request.getSoTien());
        payment.setPhuongThuc(request.getPhuongThuc());
        payment.setMaGiaoDich(request.getMaGiaoDich() != null ? request.getMaGiaoDich().trim() : null);
        payment.setTrangThai("THANH_CONG");

        ThanhToan savedPayment = thanhToanRepository.save(payment);

        // Kiểm tra nếu tổng thanh toán đã đủ
        BigDecimal newTotalPaid = paidSoFar.add(request.getSoTien());
        if (newTotalPaid.compareTo(invoice.getThanhTien()) >= 0) {
            invoice.setTrangThai("DA_THANH_TOAN");
            hoaDonRepository.save(invoice);
        }

        return new PaymentResponse(
                savedPayment.getMaThanhToan(),
                invoice.getMaHoaDon(),
                savedPayment.getSoTien(),
                savedPayment.getPhuongThuc(),
                savedPayment.getMaGiaoDich(),
                savedPayment.getThoiGianThanhToan(),
                savedPayment.getTrangThai()
        );
    }

    /**
     * Lấy danh sách thanh toán của hóa đơn
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByInvoice(Integer invoiceId) {
        HoaDon invoice = hoaDonRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + invoiceId));

        validateReadAuthorization(invoice);

        return thanhToanRepository.findByHoaDonMaHoaDon(invoiceId)
                .stream()
                .map(p -> new PaymentResponse(
                        p.getMaThanhToan(),
                        invoice.getMaHoaDon(),
                        p.getSoTien(),
                        p.getPhuongThuc(),
                        p.getMaGiaoDich(),
                        p.getThoiGianThanhToan(),
                        p.getTrangThai()
                ))
                .collect(Collectors.toList());
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

        throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập thanh toán của khách hàng khác");
    }
}
