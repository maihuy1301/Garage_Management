package com.garage.service;

import com.garage.dto.CreateInvoiceRequest;
import com.garage.dto.InvoiceResponse;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
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
class InvoiceServiceTest {

    @Mock
    private HoaDonRepository hoaDonRepository;

    @Mock
    private HoaDonDichVuRepository hoaDonDichVuRepository;

    @Mock
    private HoaDonPhuTungRepository hoaDonPhuTungRepository;

    @Mock
    private ThanhToanRepository thanhToanRepository;

    @Mock
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @Mock
    private PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository;

    @Mock
    private PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private InvoiceService invoiceService;

    private ChiNhanh branch1;
    private NguoiDung userCustomer1;
    private KhachHang customer1;
    private Xe vehicle1;
    private PhieuTiepNhan ptn1;
    private PhieuSuaChua order1;
    private DichVu service1;
    private PhuTung part1;
    private PhieuSuaChuaDichVu repairSvc1;
    private PhieuSuaChuaPhuTung repairPart1;
    private HoaDon invoice1;

    @BeforeEach
    void setUp() {
        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
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
        order1.setTrangThai("HOAN_TAT");

        service1 = new DichVu();
        service1.setMaDichVu(10);
        service1.setTenDichVu("Thay dầu");
        service1.setDonGia(new BigDecimal("300000.00"));

        repairSvc1 = new PhieuSuaChuaDichVu();
        repairSvc1.setMaChiTiet(1);
        repairSvc1.setPhieuSuaChua(order1);
        repairSvc1.setDichVu(service1);
        repairSvc1.setSoLuong(1);
        repairSvc1.setDonGia(new BigDecimal("300000.00"));

        part1 = new PhuTung();
        part1.setMaPhuTung(20);
        part1.setMaPhuTungCode("PT001");
        part1.setTenPhuTung("Dầu nhớt");
        part1.setDonViTinh("Chai");
        part1.setGiaBan(new BigDecimal("150000.00"));

        repairPart1 = new PhieuSuaChuaPhuTung();
        repairPart1.setMaChiTiet(2);
        repairPart1.setPhieuSuaChua(order1);
        repairPart1.setPhuTung(part1);
        repairPart1.setDichVuChiTiet(repairSvc1);
        repairPart1.setSoLuong(2);
        repairPart1.setDonGia(new BigDecimal("150000.00"));

        invoice1 = new HoaDon();
        invoice1.setMaHoaDon(701);
        invoice1.setPhieuSuaChua(order1);
        invoice1.setKhachHang(customer1);
        invoice1.setChiNhanh(branch1);
        invoice1.setTongTien(new BigDecimal("600000.00"));
        invoice1.setGiamGia(BigDecimal.ZERO);
        invoice1.setThue(BigDecimal.ZERO);
        invoice1.setThanhTien(new BigDecimal("600000.00"));
        invoice1.setTrangThai("CHUA_THANH_TOAN");
        invoice1.setNgayLap(LocalDateTime.now());
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

    @Test
    void createInvoice_success_calculatesTotalAndSavesItems() {
        stubStaffAuth();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(hoaDonRepository.existsByPhieuSuaChuaMaPhieuSuaChua(601)).thenReturn(false);
        when(phieuSuaChuaDichVuRepository.findByPhieuSuaChuaMaPhieuSuaChua(601)).thenReturn(List.of(repairSvc1));
        when(phieuSuaChuaPhuTungRepository.findByPhieuSuaChuaMaPhieuSuaChua(601)).thenReturn(List.of(repairPart1));

        when(hoaDonRepository.save(any(HoaDon.class))).thenReturn(invoice1);

        HoaDonDichVu hdSvc = new HoaDonDichVu();
        hdSvc.setMaChiTiet(11);
        hdSvc.setPhieuDichVu(repairSvc1);
        hdSvc.setDonGia(new BigDecimal("300000.00"));
        when(hoaDonDichVuRepository.save(any(HoaDonDichVu.class))).thenReturn(hdSvc);

        HoaDonPhuTung hdPart = new HoaDonPhuTung();
        hdPart.setMaChiTiet(12);
        hdPart.setPhieuPhuTung(repairPart1);
        hdPart.setHoaDonDichVu(hdSvc);
        hdPart.setSoLuong(2);
        hdPart.setDonGia(new BigDecimal("150000.00"));
        when(hoaDonPhuTungRepository.save(any(HoaDonPhuTung.class))).thenReturn(hdPart);

        CreateInvoiceRequest req = new CreateInvoiceRequest(new BigDecimal("50000.00"), new BigDecimal("20000.00"));
        InvoiceResponse res = invoiceService.createInvoice(601, req);

        assertThat(res).isNotNull();
        assertThat(res.getMaHoaDon()).isEqualTo(701);
        assertThat(res.getServices()).hasSize(1);
        assertThat(res.getParts()).hasSize(1);
        verify(hoaDonRepository).save(any(HoaDon.class));
        verify(hoaDonDichVuRepository).save(any(HoaDonDichVu.class));
        verify(hoaDonPhuTungRepository).save(any(HoaDonPhuTung.class));
    }

    @Test
    void createInvoice_duplicateForOrder_throws409() {
        stubStaffAuth();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(hoaDonRepository.existsByPhieuSuaChuaMaPhieuSuaChua(601)).thenReturn(true);

        CreateInvoiceRequest req = new CreateInvoiceRequest();
        assertThatThrownBy(() -> invoiceService.createInvoice(601, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã được tạo hóa đơn");
    }

    @Test
    void createInvoice_cancelledOrder_throws400() {
        stubStaffAuth();
        order1.setTrangThai("HUY");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));

        CreateInvoiceRequest req = new CreateInvoiceRequest();
        assertThatThrownBy(() -> invoiceService.createInvoice(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã bị hủy");
    }

    @Test
    void createInvoice_crossBranch_throws403() {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                "manager", "password", List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
        ));
        SecurityContextHolder.setContext(ctx);
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(false);
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));

        CreateInvoiceRequest req = new CreateInvoiceRequest();
        assertThatThrownBy(() -> invoiceService.createInvoice(601, req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }

    @Test
    void getInvoice_customerOwner_success() {
        stubCustomerAuth("customer1", userCustomer1);
        when(hoaDonRepository.findById(701)).thenReturn(Optional.of(invoice1));
        when(hoaDonDichVuRepository.findByHoaDonMaHoaDon(701)).thenReturn(List.of());
        when(hoaDonPhuTungRepository.findByHoaDonMaHoaDon(701)).thenReturn(List.of());
        when(thanhToanRepository.findByHoaDonMaHoaDon(701)).thenReturn(List.of());

        InvoiceResponse res = invoiceService.getInvoiceById(701);
        assertThat(res).isNotNull();
        assertThat(res.getMaHoaDon()).isEqualTo(701);
    }

    @Test
    void getInvoice_crossCustomer_throws403() {
        NguoiDung otherCustUser = new NguoiDung();
        otherCustUser.setMaNguoiDung(99);
        otherCustUser.setTenDangNhap("customer2");

        stubCustomerAuth("customer2", otherCustUser);
        when(hoaDonRepository.findById(701)).thenReturn(Optional.of(invoice1));

        assertThatThrownBy(() -> invoiceService.getInvoiceById(701))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("khách hàng khác");
    }
}
