package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.PhieuSuaChuaRepository;
import com.garage.repository.PhieuTiepNhanRepository;
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
class RepairOrderServiceTest {

    @Mock
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @Mock
    private PhieuTiepNhanRepository phieuTiepNhanRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private RepairOrderService repairOrderService;

    // Fixtures
    private NguoiDung managerUser;
    private ChiNhanh branch1;
    private ChiNhanh branch2;
    private Xe vehicle;
    private PhieuTiepNhan reception1;
    private PhieuTiepNhan reception2;
    private PhieuSuaChua repairOrder1;

    @BeforeEach
    void setUp() {
        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
        branch1.setMaChiNhanhCode("CN001");
        branch1.setTenChiNhanh("Chi Nhánh 1");

        branch2 = new ChiNhanh();
        branch2.setMaChiNhanh(2);
        branch2.setMaChiNhanhCode("CN002");
        branch2.setTenChiNhanh("Chi Nhánh 2");

        managerUser = new NguoiDung();
        managerUser.setMaNguoiDung(2);
        managerUser.setTenDangNhap("manager");
        managerUser.setHoTen("Nguyễn Văn Quản Lý");

        vehicle = new Xe();
        vehicle.setMaXe(100);
        vehicle.setBienSo("51A-11111");

        reception1 = new PhieuTiepNhan();
        reception1.setMaTiepNhan(501);
        reception1.setChiNhanh(branch1);
        reception1.setXe(vehicle);
        reception1.setTrangThai("DA_TIEP_NHAN");

        reception2 = new PhieuTiepNhan();
        reception2.setMaTiepNhan(502);
        reception2.setChiNhanh(branch2);
        reception2.setXe(vehicle);
        reception2.setTrangThai("DA_TIEP_NHAN");

        repairOrder1 = new PhieuSuaChua();
        repairOrder1.setMaPhieuSuaChua(601);
        repairOrder1.setPhieuTiepNhan(reception1);
        repairOrder1.setChiNhanh(branch1);
        repairOrder1.setTrangThai("CHO_XU_LY");
        repairOrder1.setGhiChu("Kiểm tra động cơ");
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
    // 1. CREATE REPAIR ORDER
    // ==========================================

    @Test
    void createRepairOrder_ownBranch_success() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        when(phieuTiepNhanRepository.findById(501)).thenReturn(Optional.of(reception1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phieuSuaChuaRepository.existsByPhieuTiepNhanMaTiepNhan(501)).thenReturn(false);
        when(phieuSuaChuaRepository.save(any(PhieuSuaChua.class))).thenReturn(repairOrder1);

        CreateRepairOrderRequest req = new CreateRepairOrderRequest(501, "Kiểm tra động cơ");
        RepairOrderResponse res = repairOrderService.createRepairOrder(req);

        assertThat(res.getMaPhieuSuaChua()).isEqualTo(601);
        assertThat(res.getTrangThai()).isEqualTo("CHO_XU_LY");
        verify(phieuSuaChuaRepository).save(any(PhieuSuaChua.class));
    }

    @Test
    void createRepairOrder_crossBranch_throws403() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        when(phieuTiepNhanRepository.findById(502)).thenReturn(Optional.of(reception2)); // reception is branch 2
        when(branchAuthorizationService.isAllowedBranch(2)).thenReturn(false);

        CreateRepairOrderRequest req = new CreateRepairOrderRequest(502, "Kiểm tra");

        assertThatThrownBy(() -> repairOrderService.createRepairOrder(req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }

    @Test
    void createRepairOrder_duplicate_throws409() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        when(phieuTiepNhanRepository.findById(501)).thenReturn(Optional.of(reception1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phieuSuaChuaRepository.existsByPhieuTiepNhanMaTiepNhan(501)).thenReturn(true); // already exists

        CreateRepairOrderRequest req = new CreateRepairOrderRequest(501, "Kiểm tra");

        assertThatThrownBy(() -> repairOrderService.createRepairOrder(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã tồn tại");
    }

    @Test
    void createRepairOrder_cancelledReception_throws400() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        reception1.setTrangThai("HUY");
        when(phieuTiepNhanRepository.findById(501)).thenReturn(Optional.of(reception1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        CreateRepairOrderRequest req = new CreateRepairOrderRequest(501, "Kiểm tra");

        assertThatThrownBy(() -> repairOrderService.createRepairOrder(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã bị hủy");
    }

    @Test
    void createRepairOrder_receptionNotFound_throws404() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        when(phieuTiepNhanRepository.findById(999)).thenReturn(Optional.empty());

        CreateRepairOrderRequest req = new CreateRepairOrderRequest(999, "Kiểm tra");

        assertThatThrownBy(() -> repairOrderService.createRepairOrder(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==========================================
    // 2. VIEW & DETAIL
    // ==========================================

    @Test
    void getRepairOrders_ownBranch_returnsList() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        when(branchAuthorizationService.resolveUserBranchId(any())).thenReturn(Optional.of(1));
        when(phieuSuaChuaRepository.findByChiNhanhMaChiNhanh(1)).thenReturn(List.of(repairOrder1));

        List<RepairOrderResponse> list = repairOrderService.getRepairOrders();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getMaPhieuSuaChua()).isEqualTo(601);
    }

    @Test
    void getRepairOrderById_crossBranch_throws403() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        PhieuSuaChua order2 = new PhieuSuaChua();
        order2.setMaPhieuSuaChua(602);
        order2.setChiNhanh(branch2);

        when(phieuSuaChuaRepository.findById(602)).thenReturn(Optional.of(order2));
        when(branchAuthorizationService.isAllowedBranch(2)).thenReturn(false);

        assertThatThrownBy(() -> repairOrderService.getRepairOrderById(602))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }

    // ==========================================
    // 3. UPDATE & STATUS TRANSITION
    // ==========================================

    @Test
    void updateRepairOrder_success() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(repairOrder1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phieuSuaChuaRepository.save(any(PhieuSuaChua.class))).thenReturn(repairOrder1);

        UpdateRepairOrderRequest req = new UpdateRepairOrderRequest(null, null, "Cập nhật ghi chú");
        RepairOrderResponse res = repairOrderService.updateRepairOrder(601, req);

        assertThat(res).isNotNull();
        assertThat(repairOrder1.getGhiChu()).isEqualTo("Cập nhật ghi chú");
    }

    @Test
    void updateStatus_toDangSua_success() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(repairOrder1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phieuSuaChuaRepository.save(any(PhieuSuaChua.class))).thenReturn(repairOrder1);

        UpdateRepairOrderStatusRequest req = new UpdateRepairOrderStatusRequest("DANG_SUA");
        RepairOrderResponse res = repairOrderService.updateStatus(601, req);

        assertThat(res).isNotNull();
        assertThat(repairOrder1.getTrangThai()).isEqualTo("DANG_SUA");
    }

    @Test
    void updateStatus_invalidStatus_throws400() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(repairOrder1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        UpdateRepairOrderStatusRequest req = new UpdateRepairOrderStatusRequest("INVALID_STATUS");

        assertThatThrownBy(() -> repairOrderService.updateStatus(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không hợp lệ");
    }

    @Test
    void updateStatus_alreadyCompleted_throws400() {
        setStaffAuth(managerUser, "ROLE_MANAGER");
        repairOrder1.setTrangThai("HOAN_TAT");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(repairOrder1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        UpdateRepairOrderStatusRequest req = new UpdateRepairOrderStatusRequest("DANG_SUA");

        assertThatThrownBy(() -> repairOrderService.updateStatus(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("kết thúc");
    }
}
