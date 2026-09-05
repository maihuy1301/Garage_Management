package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    @Mock
    private BaoGiaPhatSinhRepository baoGiaPhatSinhRepository;

    @Mock
    private BaoGiaPhatSinhDichVuRepository baoGiaPhatSinhDichVuRepository;

    @Mock
    private BaoGiaPhatSinhPhuTungRepository baoGiaPhatSinhPhuTungRepository;

    @Mock
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @Mock
    private DichVuRepository dichVuRepository;

    @Mock
    private PhuTungRepository phuTungRepository;

    @Mock
    private GiaDichVuChiNhanhRepository giaDichVuChiNhanhRepository;

    @Mock
    private PhanCongRepository phanCongRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private QuotationService quotationService;

    // Fixtures
    private ChiNhanh branch1;
    private NguoiDung userCustomer1;
    private KhachHang customer1;
    private Xe vehicle1;
    private PhieuTiepNhan ptn1;
    private PhieuSuaChua order1;
    private DichVu service1;
    private GiaDichVuChiNhanh branchServicePrice1;
    private PhuTung part1;
    private BaoGiaPhatSinh quotation1;

    @BeforeEach
    void setUp() {
        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
        branch1.setMaChiNhanhCode("CN001");
        branch1.setTenChiNhanh("Chi Nhánh 1");

        userCustomer1 = new NguoiDung();
        userCustomer1.setMaNguoiDung(10);
        userCustomer1.setTenDangNhap("customer1");
        userCustomer1.setHoTen("Nguyễn Văn Khách");

        customer1 = new KhachHang();
        customer1.setMaKhachHang(1);
        customer1.setNguoiDung(userCustomer1);

        vehicle1 = new Xe();
        vehicle1.setMaXe(100);
        vehicle1.setBienSo("51A-11111");
        vehicle1.setKhachHang(customer1);

        ptn1 = new PhieuTiepNhan();
        ptn1.setMaTiepNhan(501);
        ptn1.setXe(vehicle1);
        ptn1.setChiNhanh(branch1);

        order1 = new PhieuSuaChua();
        order1.setMaPhieuSuaChua(601);
        order1.setPhieuTiepNhan(ptn1);
        order1.setChiNhanh(branch1);
        order1.setTrangThai("DANG_SUA");

        service1 = new DichVu();
        service1.setMaDichVu(10);
        service1.setTenDichVu("Bảo dưỡng phanh");
        service1.setTrangThai(true);

        branchServicePrice1 = new GiaDichVuChiNhanh();
        branchServicePrice1.setChiNhanh(branch1);
        branchServicePrice1.setDichVu(service1);
        branchServicePrice1.setDonGia(new BigDecimal("200000.00"));
        branchServicePrice1.setTrangThai(true);

        part1 = new PhuTung();
        part1.setMaPhuTung(20);
        part1.setMaPhuTungCode("PT002");
        part1.setTenPhuTung("Má phanh");
        part1.setGiaBan(new BigDecimal("500000.00"));
        part1.setTrangThai(true);

        quotation1 = new BaoGiaPhatSinh();
        quotation1.setMaBaoGia(801);
        quotation1.setPhieuSuaChua(order1);
        quotation1.setLyDoPhatSinh("Phát hiện mòn má phanh");
        quotation1.setTongTien(new BigDecimal("700000.00"));
        quotation1.setTrangThai("CHO_KHACH_DUYET");
        quotation1.setThoiGianTao(LocalDateTime.now());
    }

    private void stubStaffAuth() {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                "manager", "password", List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
        ));
        SecurityContextHolder.setContext(ctx);
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
    }

    private void stubCustomerAuth(String username, NguoiDung user) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                username, "password", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        ));
        SecurityContextHolder.setContext(ctx);
        when(nguoiDungRepository.findByTenDangNhapOrEmail(username, username)).thenReturn(Optional.of(user));
    }

    // ==========================================
    // 1. CREATE QUOTATION
    // ==========================================

    @Test
    void createQuotation_success_calculatesTotal() {
        stubStaffAuth();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(baoGiaPhatSinhRepository.save(any(BaoGiaPhatSinh.class))).thenReturn(quotation1);

        when(dichVuRepository.findById(10)).thenReturn(Optional.of(service1));
        when(giaDichVuChiNhanhRepository.findByChiNhanhMaChiNhanhAndDichVuMaDichVuAndTrangThaiTrue(1, 10))
                .thenReturn(List.of(branchServicePrice1));

        BaoGiaPhatSinhDichVu savedSvcItem = new BaoGiaPhatSinhDichVu();
        savedSvcItem.setMaChiTiet(1);
        savedSvcItem.setDichVu(service1);
        savedSvcItem.setSoLuong(1);
        savedSvcItem.setDonGia(new BigDecimal("200000.00"));
        when(baoGiaPhatSinhDichVuRepository.save(any(BaoGiaPhatSinhDichVu.class))).thenReturn(savedSvcItem);

        when(phuTungRepository.findById(20)).thenReturn(Optional.of(part1));
        BaoGiaPhatSinhPhuTung savedPartItem = new BaoGiaPhatSinhPhuTung();
        savedPartItem.setMaChiTiet(2);
        savedPartItem.setPhuTung(part1);
        savedPartItem.setSoLuong(1);
        savedPartItem.setDonGia(new BigDecimal("500000.00"));
        when(baoGiaPhatSinhPhuTungRepository.save(any(BaoGiaPhatSinhPhuTung.class))).thenReturn(savedPartItem);

        CreateQuotationRequest req = new CreateQuotationRequest(
                "Phát hiện mòn má phanh",
                List.of(new QuotationServiceItemRequest(10, 1)),
                List.of(new QuotationPartItemRequest(20, 1))
        );

        QuotationResponse res = quotationService.createQuotation(601, req);

        assertThat(res).isNotNull();
        assertThat(res.getTongTien()).isEqualByComparingTo("700000.00"); // 200k + 500k = 700k
        assertThat(res.getTrangThai()).isEqualTo("CHO_KHACH_DUYET");
        assertThat(res.getServices()).hasSize(1);
        assertThat(res.getParts()).hasSize(1);

        verify(baoGiaPhatSinhRepository, atLeastOnce()).save(any(BaoGiaPhatSinh.class));
        verify(baoGiaPhatSinhDichVuRepository).save(any(BaoGiaPhatSinhDichVu.class));
        verify(baoGiaPhatSinhPhuTungRepository).save(any(BaoGiaPhatSinhPhuTung.class));
    }

    @Test
    void createQuotation_emptyItems_throws400() {
        stubStaffAuth();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));

        CreateQuotationRequest req = new CreateQuotationRequest("Không có items", List.of(), List.of());

        assertThatThrownBy(() -> quotationService.createQuotation(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ít nhất một");
    }

    @Test
    void createQuotation_completedOrder_throws400() {
        stubStaffAuth();
        order1.setTrangThai("HOAN_TAT");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));

        CreateQuotationRequest req = new CreateQuotationRequest(
                "Thử thêm", List.of(new QuotationServiceItemRequest(10, 1)), List.of()
        );

        assertThatThrownBy(() -> quotationService.createQuotation(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("HOAN_TAT");
    }

    @Test
    void createQuotation_unauthorizedBranch_throws403() {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                "manager", "password", List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
        ));
        SecurityContextHolder.setContext(ctx);
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(false);
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));

        CreateQuotationRequest req = new CreateQuotationRequest(
                "Thử thêm", List.of(new QuotationServiceItemRequest(10, 1)), List.of()
        );

        assertThatThrownBy(() -> quotationService.createQuotation(601, req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }

    // ==========================================
    // 2. APPROVE / REJECT QUOTATION
    // ==========================================

    @Test
    void approveQuotation_byOwnerCustomer_success() {
        stubCustomerAuth("customer1", userCustomer1);
        when(baoGiaPhatSinhRepository.findById(801)).thenReturn(Optional.of(quotation1));
        when(baoGiaPhatSinhRepository.save(any(BaoGiaPhatSinh.class))).thenReturn(quotation1);

        QuotationResponse res = quotationService.approveQuotation(801);

        assertThat(res).isNotNull();
        assertThat(quotation1.getTrangThai()).isEqualTo("DA_DUYET");
        assertThat(quotation1.getThoiGianDuyet()).isNotNull();
        verify(baoGiaPhatSinhRepository).save(quotation1);
    }

    @Test
    void rejectQuotation_byOwnerCustomer_success() {
        stubCustomerAuth("customer1", userCustomer1);
        when(baoGiaPhatSinhRepository.findById(801)).thenReturn(Optional.of(quotation1));
        when(baoGiaPhatSinhRepository.save(any(BaoGiaPhatSinh.class))).thenReturn(quotation1);

        QuotationResponse res = quotationService.rejectQuotation(801);

        assertThat(res).isNotNull();
        assertThat(quotation1.getTrangThai()).isEqualTo("TU_CHOI");
        assertThat(quotation1.getThoiGianDuyet()).isNotNull();
        verify(baoGiaPhatSinhRepository).save(quotation1);
    }

    @Test
    void approveQuotation_byOtherCustomer_throws403() {
        NguoiDung otherCustomerUser = new NguoiDung();
        otherCustomerUser.setMaNguoiDung(99);
        otherCustomerUser.setTenDangNhap("customer2");

        stubCustomerAuth("customer2", otherCustomerUser);
        when(baoGiaPhatSinhRepository.findById(801)).thenReturn(Optional.of(quotation1));

        assertThatThrownBy(() -> quotationService.approveQuotation(801))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("khách hàng khác");
    }

    @Test
    void approveQuotation_alreadyApproved_throws400() {
        quotation1.setTrangThai("DA_DUYET");
        stubCustomerAuth("customer1", userCustomer1);
        when(baoGiaPhatSinhRepository.findById(801)).thenReturn(Optional.of(quotation1));

        assertThatThrownBy(() -> quotationService.approveQuotation(801))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DA_DUYET");
    }

    @Test
    void cancelQuotation_byStaff_success() {
        stubStaffAuth();
        when(baoGiaPhatSinhRepository.findById(801)).thenReturn(Optional.of(quotation1));
        when(baoGiaPhatSinhRepository.save(any(BaoGiaPhatSinh.class))).thenReturn(quotation1);

        QuotationResponse res = quotationService.cancelQuotation(801);

        assertThat(res).isNotNull();
        assertThat(quotation1.getTrangThai()).isEqualTo("HUY");
        verify(baoGiaPhatSinhRepository).save(quotation1);
    }
}
