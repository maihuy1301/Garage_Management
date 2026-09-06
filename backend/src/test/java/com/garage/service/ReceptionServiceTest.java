package com.garage.service;

import com.garage.dto.CheckInRequest;
import com.garage.dto.ReceptionResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceptionServiceTest {

    @Mock
    private PhieuTiepNhanRepository phieuTiepNhanRepository;

    @Mock
    private DatLichRepository datLichRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private XeRepository xeRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private ReceptionService receptionService;

    // Fixtures
    private NguoiDung receptionistUser;
    private NhanVien receptionist;
    private NguoiDung customerUser;
    private KhachHang customer;
    private ChiNhanh branch1;
    private ChiNhanh branch2;
    private Xe vehicle;
    private DatLich appointmentBranch1;
    private DatLich appointmentBranch2;
    private PhieuTiepNhan receptionSlip1;

    @BeforeEach
    void setUp() {
        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
        branch1.setTenChiNhanh("Chi Nhánh 1");

        branch2 = new ChiNhanh();
        branch2.setMaChiNhanh(2);
        branch2.setTenChiNhanh("Chi Nhánh 2");

        receptionistUser = new NguoiDung();
        receptionistUser.setMaNguoiDung(3);
        receptionistUser.setTenDangNhap("receptionist");
        receptionistUser.setHoTen("Trần Thị Tiếp Nhận");

        receptionist = new NhanVien();
        receptionist.setMaNhanVien(10);
        receptionist.setNguoiDung(receptionistUser);
        receptionist.setChiNhanh(branch1);

        customerUser = new NguoiDung();
        customerUser.setMaNguoiDung(5);
        customerUser.setTenDangNhap("customer");
        customerUser.setHoTen("Phạm Văn Khách Hàng");
        customerUser.setSoDienThoai("0900000005");

        customer = new KhachHang();
        customer.setMaKhachHang(1);
        customer.setNguoiDung(customerUser);

        vehicle = new Xe();
        vehicle.setMaXe(100);
        vehicle.setBienSo("51A-11111");
        vehicle.setHangXe("Toyota");
        vehicle.setModel("Camry");
        vehicle.setSoKmHienTai(15000);
        vehicle.setKhachHang(customer);

        appointmentBranch1 = new DatLich();
        appointmentBranch1.setMaDatLich(1001);
        appointmentBranch1.setKhachHang(customer);
        appointmentBranch1.setXe(vehicle);
        appointmentBranch1.setChiNhanh(branch1);
        appointmentBranch1.setThoiGianHen(LocalDateTime.now().plusHours(2));
        appointmentBranch1.setTrangThai("CHO_XAC_NHAN");

        appointmentBranch2 = new DatLich();
        appointmentBranch2.setMaDatLich(1002);
        appointmentBranch2.setKhachHang(customer);
        appointmentBranch2.setXe(vehicle);
        appointmentBranch2.setChiNhanh(branch2);
        appointmentBranch2.setThoiGianHen(LocalDateTime.now().plusHours(3));
        appointmentBranch2.setTrangThai("CHO_XAC_NHAN");

        receptionSlip1 = new PhieuTiepNhan();
        receptionSlip1.setMaTiepNhan(501);
        receptionSlip1.setDatLich(appointmentBranch1);
        receptionSlip1.setXe(vehicle);
        receptionSlip1.setChiNhanh(branch1);
        receptionSlip1.setNhanVienTiepNhan(receptionist);
        receptionSlip1.setThoiGianTiepNhan(LocalDateTime.now());
        receptionSlip1.setSoKm(15500);
        receptionSlip1.setTrangThai("DA_TIEP_NHAN");
    }

    private void setStaffAuth(NguoiDung user, String role) {
        CustomUserDetails userDetails = new CustomUserDetails(user,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ==========================================
    // 1. ROLE_FRONT_DESK CHECK-IN: Own branch vs Cross branch
    // ==========================================

    @Test
    void receptionist_checkIn_ownBranch_success() {
        setStaffAuth(receptionistUser, "ROLE_FRONT_DESK");
        when(datLichRepository.findById(1001)).thenReturn(Optional.of(appointmentBranch1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phieuTiepNhanRepository.existsByDatLichMaDatLich(1001)).thenReturn(false);
        when(nhanVienRepository.findByNguoiDungMaNguoiDung(3)).thenReturn(Optional.of(receptionist));
        when(phieuTiepNhanRepository.save(any(PhieuTiepNhan.class))).thenReturn(receptionSlip1);

        CheckInRequest req = new CheckInRequest(15500, "Xước cản trước", "Thay dầu");
        ReceptionResponse res = receptionService.checkIn(1001, req);

        assertThat(res).isNotNull();
        assertThat(res.getMaTiepNhan()).isEqualTo(501);
        assertThat(res.getTrangThai()).isEqualTo("DA_TIEP_NHAN");
        assertThat(appointmentBranch1.getTrangThai()).isEqualTo("DA_TIEP_NHAN");
        verify(datLichRepository).save(appointmentBranch1);
        verify(xeRepository).save(vehicle);
    }

    @Test
    void receptionist_checkIn_crossBranch_throws403() {
        setStaffAuth(receptionistUser, "ROLE_FRONT_DESK");
        when(datLichRepository.findById(1002)).thenReturn(Optional.of(appointmentBranch2)); // appointment is branch 2
        when(branchAuthorizationService.isAllowedBranch(2)).thenReturn(false);

        CheckInRequest req = new CheckInRequest(15500, "Bình thường", "Bảo dưỡng");

        assertThatThrownBy(() -> receptionService.checkIn(1002, req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }

    // ==========================================
    // 2. BUSINESS VALIDATIONS
    // ==========================================

    @Test
    void checkIn_cancelledAppointment_throws400() {
        setStaffAuth(receptionistUser, "ROLE_FRONT_DESK");
        appointmentBranch1.setTrangThai("HUY");
        when(datLichRepository.findById(1001)).thenReturn(Optional.of(appointmentBranch1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        assertThatThrownBy(() -> receptionService.checkIn(1001, new CheckInRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã bị hủy");
    }

    @Test
    void checkIn_alreadyCheckedInStatus_throws409() {
        setStaffAuth(receptionistUser, "ROLE_FRONT_DESK");
        appointmentBranch1.setTrangThai("DA_TIEP_NHAN");
        when(datLichRepository.findById(1001)).thenReturn(Optional.of(appointmentBranch1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        assertThatThrownBy(() -> receptionService.checkIn(1001, new CheckInRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã được tiếp nhận trước đó");
    }

    @Test
    void checkIn_duplicateSlipExists_throws409() {
        setStaffAuth(receptionistUser, "ROLE_FRONT_DESK");
        when(datLichRepository.findById(1001)).thenReturn(Optional.of(appointmentBranch1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phieuTiepNhanRepository.existsByDatLichMaDatLich(1001)).thenReturn(true); // already slip exists

        assertThatThrownBy(() -> receptionService.checkIn(1001, new CheckInRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Phiếu tiếp nhận đã tồn tại");
    }

    @Test
    void checkIn_appointmentNotFound_throws404() {
        setStaffAuth(receptionistUser, "ROLE_FRONT_DESK");
        when(datLichRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receptionService.checkIn(999, new CheckInRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==========================================
    // 3. ROLE_ADMIN: Global check-in
    // ==========================================

    @Test
    void admin_checkIn_anyBranch_success() {
        NguoiDung admin = new NguoiDung();
        admin.setMaNguoiDung(1);
        admin.setTenDangNhap("admin");
        setStaffAuth(admin, "ROLE_ADMIN");

        when(datLichRepository.findById(1002)).thenReturn(Optional.of(appointmentBranch2));
        when(branchAuthorizationService.isAllowedBranch(2)).thenReturn(true);
        when(phieuTiepNhanRepository.existsByDatLichMaDatLich(1002)).thenReturn(false);
        when(nhanVienRepository.findByNguoiDungMaNguoiDung(1)).thenReturn(Optional.empty());
        when(nhanVienRepository.findByChiNhanhMaChiNhanh(2)).thenReturn(List.of(receptionist));
        when(phieuTiepNhanRepository.save(any(PhieuTiepNhan.class))).thenReturn(receptionSlip1);

        ReceptionResponse res = receptionService.checkIn(1002, new CheckInRequest());

        assertThat(res).isNotNull();
        assertThat(appointmentBranch2.getTrangThai()).isEqualTo("DA_TIEP_NHAN");
    }

    // ==========================================
    // 4. VIEW RECEPTION SLIPS
    // ==========================================

    @Test
    void receptionist_getReceptionSlips_returnsOwnBranchOnly() {
        setStaffAuth(receptionistUser, "ROLE_FRONT_DESK");
        when(branchAuthorizationService.resolveUserBranchId(any())).thenReturn(Optional.of(1));
        when(phieuTiepNhanRepository.findByChiNhanhMaChiNhanh(1)).thenReturn(List.of(receptionSlip1));

        List<ReceptionResponse> list = receptionService.getReceptionSlips();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getMaTiepNhan()).isEqualTo(501);
    }

    @Test
    void receptionist_getReceptionSlipById_crossBranch_throws403() {
        setStaffAuth(receptionistUser, "ROLE_FRONT_DESK");
        PhieuTiepNhan slipBranch2 = new PhieuTiepNhan();
        slipBranch2.setMaTiepNhan(502);
        slipBranch2.setChiNhanh(branch2);

        when(phieuTiepNhanRepository.findById(502)).thenReturn(Optional.of(slipBranch2));
        when(branchAuthorizationService.isAllowedBranch(2)).thenReturn(false);

        assertThatThrownBy(() -> receptionService.getReceptionSlipById(502))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }
}
