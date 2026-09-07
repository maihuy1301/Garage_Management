package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.repository.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private HoaDonRepository hoaDonRepository;

    @Mock
    private ThanhToanRepository thanhToanRepository;

    @Mock
    private DatLichRepository datLichRepository;

    @Mock
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @Mock
    private PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository;

    @Mock
    private PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository;

    @Mock
    private TonKhoRepository tonKhoRepository;

    @Mock
    private PhanCongRepository phanCongRepository;

    @Mock
    private ChiNhanhRepository chiNhanhRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private XeRepository xeRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private ReportService reportService;

    private ChiNhanh branch1;
    private HoaDon invoicePaid;
    private HoaDon invoiceUnpaid;
    private ThanhToan payment;
    private DatLich appointment;
    private PhieuSuaChua repairOrder;

    @BeforeEach
    void setUp() {
        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
        branch1.setTenChiNhanh("Chi nhánh Quận 1");

        invoicePaid = new HoaDon();
        invoicePaid.setMaHoaDon(101);
        invoicePaid.setChiNhanh(branch1);
        invoicePaid.setThanhTien(new BigDecimal("1500000.00"));
        invoicePaid.setTrangThai("DA_THANH_TOAN");
        invoicePaid.setNgayLap(LocalDateTime.now());

        invoiceUnpaid = new HoaDon();
        invoiceUnpaid.setMaHoaDon(102);
        invoiceUnpaid.setChiNhanh(branch1);
        invoiceUnpaid.setThanhTien(new BigDecimal("500000.00"));
        invoiceUnpaid.setTrangThai("CHUA_THANH_TOAN");
        invoiceUnpaid.setNgayLap(LocalDateTime.now());

        payment = new ThanhToan();
        payment.setMaThanhToan(201);
        payment.setHoaDon(invoicePaid);
        payment.setSoTien(new BigDecimal("1500000.00"));
        payment.setTrangThai("THANH_CONG");
        payment.setThoiGianThanhToan(LocalDateTime.now());

        appointment = new DatLich();
        appointment.setMaDatLich(301);
        appointment.setChiNhanh(branch1);
        appointment.setTrangThai("DA_XAC_NHAN");
        appointment.setThoiGianHen(LocalDateTime.now());

        repairOrder = new PhieuSuaChua();
        repairOrder.setMaPhieuSuaChua(401);
        repairOrder.setChiNhanh(branch1);
        repairOrder.setTrangThai("DANG_SUA");
        repairOrder.setThoiGianBatDau(LocalDateTime.now());
    }

    private void stubAuth(String username, String role) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                username, "pass", List.of(new SimpleGrantedAuthority("ROLE_" + role))
        ));
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void getDashboardReport_admin_success() {
        stubAuth("admin", "ADMIN");
        when(hoaDonRepository.findAll()).thenReturn(List.of(invoicePaid, invoiceUnpaid));
        when(thanhToanRepository.findAll()).thenReturn(List.of(payment));
        when(datLichRepository.findAll()).thenReturn(List.of(appointment));
        when(phieuSuaChuaRepository.findAll()).thenReturn(List.of(repairOrder));
        when(khachHangRepository.count()).thenReturn(10L);
        when(xeRepository.count()).thenReturn(15L);

        DashboardResponse res = reportService.getDashboardReport(null, null, null);

        assertThat(res).isNotNull();
        assertThat(res.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("1500000.00"));
        assertThat(res.getTotalInvoices()).isEqualTo(2);
        assertThat(res.getTotalPayments()).isEqualTo(1);
        assertThat(res.getRepairOrdersInProgress()).isEqualTo(1);
        assertThat(res.getTotalCustomers()).isEqualTo(10);
        assertThat(res.getTotalVehicles()).isEqualTo(15);
    }

    @Test
    void getRevenueReport_admin_success_calculatesSums() {
        stubAuth("admin", "ADMIN");
        when(hoaDonRepository.findAll()).thenReturn(List.of(invoicePaid, invoiceUnpaid));
        when(thanhToanRepository.findAll()).thenReturn(List.of(payment));

        RevenueReportResponse res = reportService.getRevenueReport(null, null, null);

        assertThat(res).isNotNull();
        assertThat(res.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("1500000.00"));
        assertThat(res.getTotalPaid()).isEqualByComparingTo(new BigDecimal("1500000.00"));
        assertThat(res.getTotalUnpaid()).isEqualByComparingTo(new BigDecimal("500000.00"));
        assertThat(res.getTotalInvoices()).isEqualTo(2);
        assertThat(res.getPeriods()).isNotEmpty();
    }

    @Test
    void getAppointmentReport_calculatesStatusBreakdown() {
        stubAuth("admin", "ADMIN");
        when(datLichRepository.findAll()).thenReturn(List.of(appointment));

        AppointmentReportResponse res = reportService.getAppointmentReport(null, null, null);

        assertThat(res.getTotal()).isEqualTo(1);
        assertThat(res.getDaXacNhan()).isEqualTo(1);
        assertThat(res.getChoXacNhan()).isEqualTo(0);
    }

    @Test
    void getRepairOrderReport_calculatesStatusBreakdown() {
        stubAuth("admin", "ADMIN");
        when(phieuSuaChuaRepository.findAll()).thenReturn(List.of(repairOrder));

        RepairOrderReportResponse res = reportService.getRepairOrderReport(null, null, null);

        assertThat(res.getTotal()).isEqualTo(1);
        assertThat(res.getDangSua()).isEqualTo(1);
        assertThat(res.getHoanTat()).isEqualTo(0);
    }

    @Test
    void getInventoryReport_checksLowStock() {
        stubAuth("admin", "ADMIN");
        PhuTung pt = new PhuTung();
        pt.setMaPhuTung(1);
        pt.setMaPhuTungCode("PT001");
        pt.setTenPhuTung("Lọc nhớt");
        pt.setGiaBan(new BigDecimal("120000"));

        TonKho tk = new TonKho(branch1, pt);
        tk.setSoLuongTon(2);
        tk.setSoLuongToiThieu(5);

        when(tonKhoRepository.findAll()).thenReturn(List.of(tk));

        List<InventoryReportResponse> list = reportService.getInventoryReport(null);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).isSapHetHang()).isTrue();
        assertThat(list.get(0).getGiaTriTonKho()).isEqualByComparingTo(new BigDecimal("240000"));
    }

    @Test
    void customer_accessReport_throws403() {
        stubAuth("customer", "CUSTOMER");

        assertThatThrownBy(() -> reportService.getDashboardReport(null, null, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Khách hàng không có quyền");
    }

    @Test
    void dateValidation_fromAfterTo_throwsBadRequest() {
        stubAuth("admin", "ADMIN");
        LocalDate from = LocalDate.of(2026, 5, 20);
        LocalDate to = LocalDate.of(2026, 5, 10);

        assertThatThrownBy(() -> reportService.getDashboardReport(null, from, to))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("phải trước hoặc bằng");
    }

    @Test
    void branchManager_otherBranch_throws403() {
        stubAuth("manager1", "ROLE_MANAGER");
        when(branchAuthorizationService.resolveUserBranchId(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.of(1));
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));

        assertThatThrownBy(() -> reportService.getDashboardReport("2", null, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }
}
