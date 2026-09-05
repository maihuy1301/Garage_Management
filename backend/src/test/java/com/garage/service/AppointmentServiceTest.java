package com.garage.service;

import com.garage.dto.AppointmentResponse;
import com.garage.dto.CreateAppointmentRequest;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import com.garage.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private DatLichRepository datLichRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private XeRepository xeRepository;

    @Mock
    private ChiNhanhRepository chiNhanhRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private AppointmentService appointmentService;

    // Fixtures
    private NguoiDung user1;
    private NguoiDung user2;
    private NguoiDung managerUser;
    private KhachHang customer1;
    private KhachHang customer2;
    private Xe vehicle1;
    private Xe vehicle2;
    private ChiNhanh branch1;
    private ChiNhanh branch2;
    private DatLich appointment1;
    private DatLich appointment2;

    @BeforeEach
    void setUp() {
        user1 = new NguoiDung();
        user1.setMaNguoiDung(10);
        user1.setTenDangNhap("customer1");
        user1.setHoTen("Customer One");
        user1.setSoDienThoai("0900000001");

        user2 = new NguoiDung();
        user2.setMaNguoiDung(11);
        user2.setTenDangNhap("customer2");
        user2.setHoTen("Customer Two");
        user2.setSoDienThoai("0900000002");

        managerUser = new NguoiDung();
        managerUser.setMaNguoiDung(20);
        managerUser.setTenDangNhap("manager1");
        managerUser.setHoTen("Manager One");

        customer1 = new KhachHang();
        customer1.setMaKhachHang(1);
        customer1.setNguoiDung(user1);

        customer2 = new KhachHang();
        customer2.setMaKhachHang(2);
        customer2.setNguoiDung(user2);

        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
        branch1.setMaChiNhanhCode("CN001");
        branch1.setTenChiNhanh("Chi Nhánh 1");
        branch1.setTrangThai(true);

        branch2 = new ChiNhanh();
        branch2.setMaChiNhanh(2);
        branch2.setMaChiNhanhCode("CN002");
        branch2.setTenChiNhanh("Chi Nhánh 2");
        branch2.setTrangThai(true);

        vehicle1 = new Xe();
        vehicle1.setMaXe(100);
        vehicle1.setBienSo("51A-11111");
        vehicle1.setHangXe("Toyota");
        vehicle1.setModel("Camry");
        vehicle1.setKhachHang(customer1);

        vehicle2 = new Xe();
        vehicle2.setMaXe(200);
        vehicle2.setBienSo("51B-22222");
        vehicle2.setHangXe("Honda");
        vehicle2.setModel("Civic");
        vehicle2.setKhachHang(customer2);

        appointment1 = new DatLich();
        appointment1.setMaDatLich(1001);
        appointment1.setKhachHang(customer1);
        appointment1.setXe(vehicle1);
        appointment1.setChiNhanh(branch1);
        appointment1.setThoiGianHen(LocalDateTime.now().plusDays(1));
        appointment1.setTrangThai("CHO_XAC_NHAN");
        appointment1.setGhiChu("Bảo dưỡng định kỳ");

        appointment2 = new DatLich();
        appointment2.setMaDatLich(1002);
        appointment2.setKhachHang(customer2);
        appointment2.setXe(vehicle2);
        appointment2.setChiNhanh(branch2);
        appointment2.setThoiGianHen(LocalDateTime.now().plusDays(2));
        appointment2.setTrangThai("CHO_XAC_NHAN");
        appointment2.setGhiChu("Thay dầu máy");
    }

    private void setCustomerAuth(NguoiDung user, KhachHang customer) {
        CustomUserDetails userDetails = new CustomUserDetails(user,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        when(khachHangRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()))
                .thenReturn(Optional.of(customer));
    }

    private void setManagerAuth(NguoiDung user, Integer branchId) {
        CustomUserDetails userDetails = new CustomUserDetails(user,
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    private void setAdminAuth() {
        NguoiDung admin = new NguoiDung();
        admin.setMaNguoiDung(999);
        admin.setTenDangNhap("admin");
        CustomUserDetails userDetails = new CustomUserDetails(admin,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ==========================================
    // 1. CUSTOMER: List, View, Create, Conflict, Cancel
    // ==========================================

    @Test
    void customer_getAppointments_returnsOwnAppointments() {
        setCustomerAuth(user1, customer1);
        when(datLichRepository.findByKhachHangMaKhachHang(1)).thenReturn(List.of(appointment1));

        List<AppointmentResponse> res = appointmentService.getAppointments();

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getMaDatLich()).isEqualTo(1001);
        assertThat(res.get(0).getBienSoXe()).isEqualTo("51A-11111");
        verify(datLichRepository, never()).findAll();
    }

    @Test
    void customer_getOwnAppointment_returns200() {
        setCustomerAuth(user1, customer1);
        when(datLichRepository.findById(1001)).thenReturn(Optional.of(appointment1));

        AppointmentResponse res = appointmentService.getAppointmentById(1001);

        assertThat(res.getMaDatLich()).isEqualTo(1001);
        assertThat(res.getBienSoXe()).isEqualTo("51A-11111");
    }

    @Test
    void customer_getOtherCustomerAppointment_throws403() {
        setCustomerAuth(user1, customer1);
        when(datLichRepository.findById(1002)).thenReturn(Optional.of(appointment2)); // appointment2 belongs to customer2

        assertThatThrownBy(() -> appointmentService.getAppointmentById(1002))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("khách hàng khác");
    }

    @Test
    void customer_createAppointment_success() {
        setCustomerAuth(user1, customer1);

        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        CreateAppointmentRequest req = new CreateAppointmentRequest(100, 1, futureTime, "Bảo dưỡng");

        when(xeRepository.findById(100)).thenReturn(Optional.of(vehicle1));
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));
        when(datLichRepository.existsByXeMaXeAndThoiGianHenAndTrangThaiNotIn(eq(100), eq(futureTime), any()))
                .thenReturn(false);

        DatLich saved = new DatLich();
        saved.setMaDatLich(1003);
        saved.setKhachHang(customer1);
        saved.setXe(vehicle1);
        saved.setChiNhanh(branch1);
        saved.setThoiGianHen(futureTime);
        saved.setTrangThai("CHO_XAC_NHAN");
        saved.setGhiChu("Bảo dưỡng");
        when(datLichRepository.save(any(DatLich.class))).thenReturn(saved);

        AppointmentResponse res = appointmentService.createAppointment(req);

        assertThat(res.getMaDatLich()).isEqualTo(1003);
        assertThat(res.getTrangThai()).isEqualTo("CHO_XAC_NHAN");
        assertThat(res.getBienSoXe()).isEqualTo("51A-11111");
        verify(datLichRepository).save(any(DatLich.class));
    }

    @Test
    void customer_createAppointment_otherCustomerVehicle_throws403() {
        setCustomerAuth(user1, customer1);

        CreateAppointmentRequest req = new CreateAppointmentRequest(200, 1, LocalDateTime.now().plusDays(1), "Bảo dưỡng");
        when(xeRepository.findById(200)).thenReturn(Optional.of(vehicle2)); // vehicle2 belongs to customer2

        assertThatThrownBy(() -> appointmentService.createAppointment(req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("xe thuộc sở hữu");
    }

    @Test
    void customer_createAppointment_pastTime_throws400() {
        setCustomerAuth(user1, customer1);

        CreateAppointmentRequest req = new CreateAppointmentRequest(100, 1, LocalDateTime.now().minusHours(1), "Bảo dưỡng");
        when(xeRepository.findById(100)).thenReturn(Optional.of(vehicle1));
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));

        assertThatThrownBy(() -> appointmentService.createAppointment(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tương lai");
    }

    @Test
    void customer_createAppointment_timeConflict_throws409() {
        setCustomerAuth(user1, customer1);

        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        CreateAppointmentRequest req = new CreateAppointmentRequest(100, 1, futureTime, "Bảo dưỡng");

        when(xeRepository.findById(100)).thenReturn(Optional.of(vehicle1));
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));
        when(datLichRepository.existsByXeMaXeAndThoiGianHenAndTrangThaiNotIn(eq(100), eq(futureTime), any()))
                .thenReturn(true); // already active appointment

        assertThatThrownBy(() -> appointmentService.createAppointment(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã có lịch hẹn");
    }

    @Test
    void customer_cancelOwnAppointment_success() {
        setCustomerAuth(user1, customer1);
        when(datLichRepository.findById(1001)).thenReturn(Optional.of(appointment1));
        when(datLichRepository.save(any(DatLich.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse res = appointmentService.cancelAppointment(1001);

        assertThat(res.getTrangThai()).isEqualTo("HUY");
    }

    @Test
    void customer_cancelOtherCustomerAppointment_throws403() {
        setCustomerAuth(user1, customer1);
        when(datLichRepository.findById(1002)).thenReturn(Optional.of(appointment2)); // appointment2 belongs to customer2

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1002))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void customer_cancelAlreadyCancelledAppointment_throws400() {
        setCustomerAuth(user1, customer1);
        appointment1.setTrangThai("HUY");
        when(datLichRepository.findById(1001)).thenReturn(Optional.of(appointment1));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1001))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã được hủy");
    }

    // ==========================================
    // 2. BRANCH_MANAGER: Own branch vs Other branch
    // ==========================================

    @Test
    void branchManager_getAppointments_returnsOwnBranchOnly() {
        setManagerAuth(managerUser, 1);
        when(branchAuthorizationService.resolveUserBranchId(any())).thenReturn(Optional.of(1));
        when(datLichRepository.findByChiNhanhMaChiNhanh(1)).thenReturn(List.of(appointment1));

        List<AppointmentResponse> res = appointmentService.getAppointments();

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getMaChiNhanh()).isEqualTo(1);
    }

    @Test
    void branchManager_getOwnBranchAppointment_returns200() {
        setManagerAuth(managerUser, 1);
        when(datLichRepository.findById(1001)).thenReturn(Optional.of(appointment1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        AppointmentResponse res = appointmentService.getAppointmentById(1001);

        assertThat(res.getMaDatLich()).isEqualTo(1001);
    }

    @Test
    void branchManager_getOtherBranchAppointment_throws403() {
        setManagerAuth(managerUser, 1);
        when(datLichRepository.findById(1002)).thenReturn(Optional.of(appointment2)); // appointment2 is branch 2
        when(branchAuthorizationService.isAllowedBranch(2)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.getAppointmentById(1002))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }

    // ==========================================
    // 3. SYSTEM_ADMIN: Global Access
    // ==========================================

    @Test
    void admin_getAppointments_returnsAll() {
        setAdminAuth();
        when(datLichRepository.findAll()).thenReturn(List.of(appointment1, appointment2));

        List<AppointmentResponse> res = appointmentService.getAppointments();

        assertThat(res).hasSize(2);
    }

    @Test
    void admin_getAnyAppointment_returns200() {
        setAdminAuth();
        when(datLichRepository.findById(1002)).thenReturn(Optional.of(appointment2));

        AppointmentResponse res = appointmentService.getAppointmentById(1002);

        assertThat(res.getMaDatLich()).isEqualTo(1002);
    }

    @Test
    void appointmentNotFound_throws404() {
        setAdminAuth();
        when(datLichRepository.findById(9999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(9999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
