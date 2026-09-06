package com.garage.service;

import com.garage.dto.CreatePaymentRequest;
import com.garage.dto.PaymentResponse;
import com.garage.entity.ChiNhanh;
import com.garage.entity.HoaDon;
import com.garage.entity.ThanhToan;
import com.garage.exception.BadRequestException;
import com.garage.repository.HoaDonRepository;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.ThanhToanRepository;
import com.garage.security.BranchAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ThanhToanRepository thanhToanRepository;

    @Mock
    private HoaDonRepository hoaDonRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private PaymentService paymentService;

    private ChiNhanh branch1;
    private HoaDon invoice1;

    @BeforeEach
    void setUp() {
        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
        branch1.setTenChiNhanh("Chi Nhánh 1");

        invoice1 = new HoaDon();
        invoice1.setMaHoaDon(701);
        invoice1.setChiNhanh(branch1);
        invoice1.setThanhTien(new BigDecimal("1000000.00"));
        invoice1.setTrangThai("CHUA_THANH_TOAN");
    }

    private void stubStaffAuth() {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                "receptionist", "password", List.of(new SimpleGrantedAuthority("ROLE_FRONT_DESK"))
        ));
        SecurityContextHolder.setContext(ctx);
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
    }

    @Test
    void createPayment_fullPayment_success_updatesInvoiceStatusToPaid() {
        stubStaffAuth();
        when(hoaDonRepository.findById(701)).thenReturn(Optional.of(invoice1));
        when(thanhToanRepository.findByHoaDonMaHoaDon(701)).thenReturn(List.of());

        ThanhToan savedPayment = new ThanhToan();
        savedPayment.setMaThanhToan(901);
        savedPayment.setHoaDon(invoice1);
        savedPayment.setSoTien(new BigDecimal("1000000.00"));
        savedPayment.setPhuongThuc("TIEN_MAT");
        savedPayment.setTrangThai("THANH_CONG");
        when(thanhToanRepository.save(any(ThanhToan.class))).thenReturn(savedPayment);

        CreatePaymentRequest req = new CreatePaymentRequest(new BigDecimal("1000000.00"), "TIEN_MAT", null);
        PaymentResponse res = paymentService.createPayment(701, req);

        assertThat(res).isNotNull();
        assertThat(res.getSoTien()).isEqualByComparingTo("1000000.00");
        assertThat(invoice1.getTrangThai()).isEqualTo("DA_THANH_TOAN");
        verify(hoaDonRepository).save(invoice1);
    }

    @Test
    void createPayment_partialPayment_success_keepsStatusChuaThanhToan() {
        stubStaffAuth();
        when(hoaDonRepository.findById(701)).thenReturn(Optional.of(invoice1));
        when(thanhToanRepository.findByHoaDonMaHoaDon(701)).thenReturn(List.of());

        ThanhToan savedPayment = new ThanhToan();
        savedPayment.setMaThanhToan(902);
        savedPayment.setHoaDon(invoice1);
        savedPayment.setSoTien(new BigDecimal("400000.00"));
        savedPayment.setPhuongThuc("CHUYEN_KHOAN");
        savedPayment.setTrangThai("THANH_CONG");
        when(thanhToanRepository.save(any(ThanhToan.class))).thenReturn(savedPayment);

        CreatePaymentRequest req = new CreatePaymentRequest(new BigDecimal("400000.00"), "CHUYEN_KHOAN", "TX123");
        PaymentResponse res = paymentService.createPayment(701, req);

        assertThat(res).isNotNull();
        assertThat(invoice1.getTrangThai()).isEqualTo("CHUA_THANH_TOAN");
        verify(hoaDonRepository, never()).save(invoice1);
    }

    @Test
    void createPayment_overpayment_throws400() {
        stubStaffAuth();
        when(hoaDonRepository.findById(701)).thenReturn(Optional.of(invoice1));
        when(thanhToanRepository.findByHoaDonMaHoaDon(701)).thenReturn(List.of());

        CreatePaymentRequest req = new CreatePaymentRequest(new BigDecimal("1500000.00"), "TIEN_MAT", null);

        assertThatThrownBy(() -> paymentService.createPayment(701, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("vượt quá số tiền còn lại");
    }

    @Test
    void createPayment_alreadyPaidInvoice_throws400() {
        stubStaffAuth();
        invoice1.setTrangThai("DA_THANH_TOAN");
        when(hoaDonRepository.findById(701)).thenReturn(Optional.of(invoice1));

        CreatePaymentRequest req = new CreatePaymentRequest(new BigDecimal("100000.00"), "TIEN_MAT", null);

        assertThatThrownBy(() -> paymentService.createPayment(701, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã được thanh toán đầy đủ");
    }

    @Test
    void createPayment_duplicateTransactionCode_throws400() {
        stubStaffAuth();
        when(hoaDonRepository.findById(701)).thenReturn(Optional.of(invoice1));
        when(thanhToanRepository.findByHoaDonMaHoaDon(701)).thenReturn(List.of());
        when(thanhToanRepository.existsByMaGiaoDichAndTrangThai("TX123", "THANH_CONG")).thenReturn(true);

        CreatePaymentRequest req = new CreatePaymentRequest(new BigDecimal("500000.00"), "CHUYEN_KHOAN", "TX123");

        assertThatThrownBy(() -> paymentService.createPayment(701, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã được xử lý trước đó");
    }

    @Test
    void createPayment_crossBranch_throws403() {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                "receptionist", "password", List.of(new SimpleGrantedAuthority("ROLE_FRONT_DESK"))
        ));
        SecurityContextHolder.setContext(ctx);
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(false);
        when(hoaDonRepository.findById(701)).thenReturn(Optional.of(invoice1));

        CreatePaymentRequest req = new CreatePaymentRequest(new BigDecimal("500000.00"), "TIEN_MAT", null);

        assertThatThrownBy(() -> paymentService.createPayment(701, req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }
}
