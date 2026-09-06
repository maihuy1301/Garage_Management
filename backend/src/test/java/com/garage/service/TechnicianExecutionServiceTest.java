package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
class TechnicianExecutionServiceTest {

    @Mock
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @Mock
    private PhanCongRepository phanCongRepository;

    @Mock
    private PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @InjectMocks
    private TechnicianExecutionService technicianExecutionService;

    // Fixtures
    private ChiNhanh branch1;
    private ChiNhanh branch2;
    private PhieuSuaChua order1;
    private PhieuSuaChua order2;
    private NguoiDung userTech1;
    private NhanVien tech1;
    private VaiTro roleTech;
    private NguoiDungVaiTro userRoleTech;
    private PhanCong assignment1;
    private DichVu service1;
    private PhieuSuaChuaDichVu item1;

    @BeforeEach
    void setUp() {
        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
        branch1.setTenChiNhanh("Chi Nhánh 1");

        branch2 = new ChiNhanh();
        branch2.setMaChiNhanh(2);
        branch2.setTenChiNhanh("Chi Nhánh 2");

        order1 = new PhieuSuaChua();
        order1.setMaPhieuSuaChua(601);
        order1.setChiNhanh(branch1);
        order1.setTrangThai("DA_PHAN_CONG");

        order2 = new PhieuSuaChua();
        order2.setMaPhieuSuaChua(602);
        order2.setChiNhanh(branch1);
        order2.setTrangThai("DA_PHAN_CONG");

        userTech1 = new NguoiDung();
        userTech1.setMaNguoiDung(10);
        userTech1.setTenDangNhap("technician1");
        userTech1.setHoTen("Nguyễn Văn Kỹ Thuật");
        userTech1.setTrangThai(true);

        tech1 = new NhanVien();
        tech1.setMaNhanVien(100);
        tech1.setChiNhanh(branch1);
        tech1.setNguoiDung(userTech1);
        tech1.setTrangThai(true);

        roleTech = new VaiTro();
        roleTech.setMaVaiTro(4);
        roleTech.setTenVaiTro("ROLE_TECHNICIAN");

        userRoleTech = new NguoiDungVaiTro(userTech1, roleTech);

        assignment1 = new PhanCong();
        assignment1.setMaPhanCong(801);
        assignment1.setPhieuSuaChua(order1);
        assignment1.setNhanVienDuocPhanCong(tech1);
        assignment1.setTrangThai("DA_GIAO");

        service1 = new DichVu();
        service1.setMaDichVu(10);
        service1.setTenDichVu("Thay dầu");

        item1 = new PhieuSuaChuaDichVu();
        item1.setMaChiTiet(701);
        item1.setPhieuSuaChua(order1);
        item1.setDichVu(service1);
        item1.setDonGia(new BigDecimal("150000.00"));
        item1.setTrangThai("CHO_XU_LY");
    }

    private void stubCurrentTechnician() {
        org.springframework.security.core.context.SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                "technician1", "password",
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TECHNICIAN"))
        ));
        SecurityContextHolder.setContext(ctx);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("technician1", "technician1"))
                .thenReturn(Optional.of(userTech1));
        when(nhanVienRepository.findByNguoiDungMaNguoiDung(10)).thenReturn(Optional.of(tech1));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(10)).thenReturn(List.of(userRoleTech));
    }

    // ==========================================
    // 1. GET ASSIGNED REPAIR ORDERS
    // ==========================================

    @Test
    void getMyRepairOrders_success() {
        stubCurrentTechnician();
        when(phanCongRepository.findByNhanVienDuocPhanCongMaNhanVien(100)).thenReturn(List.of(assignment1));

        List<RepairOrderResponse> list = technicianExecutionService.getMyRepairOrders();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getMaPhieuSuaChua()).isEqualTo(601);
    }

    @Test
    void getRepairOrderDetail_assigned_success() {
        stubCurrentTechnician();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(601, 100)).thenReturn(true);

        RepairOrderResponse res = technicianExecutionService.getRepairOrderDetail(601);

        assertThat(res).isNotNull();
        assertThat(res.getMaPhieuSuaChua()).isEqualTo(601);
    }

    @Test
    void getRepairOrderDetail_unassignedOrder_throws403() {
        stubCurrentTechnician();
        when(phieuSuaChuaRepository.findById(602)).thenReturn(Optional.of(order2));
        when(phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(602, 100)).thenReturn(false);

        assertThatThrownBy(() -> technicianExecutionService.getRepairOrderDetail(602))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("không được phân công");
    }

    // ==========================================
    // 2. UPDATE REPAIR PROGRESS
    // ==========================================

    @Test
    void updateProgress_toInProgress_setsStartTimeAndStatus() {
        stubCurrentTechnician();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(601, 100)).thenReturn(true);
        when(phieuSuaChuaRepository.save(any(PhieuSuaChua.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProgressRequest req = new UpdateProgressRequest("DANG_SUA", null, null, "Đang xả dầu cũ");
        RepairOrderResponse res = technicianExecutionService.updateProgress(601, req);

        assertThat(res).isNotNull();
        assertThat(order1.getTrangThai()).isEqualTo("DANG_SUA");
        assertThat(order1.getThoiGianBatDau()).isNotNull();
        verify(phieuSuaChuaRepository).save(order1);
    }

    @Test
    void updateProgress_toComplete_setsFinishTimeAndStatus() {
        stubCurrentTechnician();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(601, 100)).thenReturn(true);
        when(phieuSuaChuaRepository.save(any(PhieuSuaChua.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProgressRequest req = new UpdateProgressRequest("HOAN_TAT", null, null, "Đã hoàn tất toàn bộ");
        RepairOrderResponse res = technicianExecutionService.updateProgress(601, req);

        assertThat(res).isNotNull();
        assertThat(order1.getTrangThai()).isEqualTo("HOAN_TAT");
        assertThat(order1.getThoiGianHoanTat()).isNotNull();
        verify(phieuSuaChuaRepository).save(order1);
    }

    @Test
    void updateProgress_completedOrder_throws400() {
        order1.setTrangThai("HOAN_TAT");
        stubCurrentTechnician();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(601, 100)).thenReturn(true);

        UpdateProgressRequest req = new UpdateProgressRequest("DANG_SUA", null, null, "Cố gắng sửa");

        assertThatThrownBy(() -> technicianExecutionService.updateProgress(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("HOAN_TAT");
    }

    @Test
    void updateProgress_cancelledOrder_throws400() {
        order1.setTrangThai("HUY");
        stubCurrentTechnician();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(601, 100)).thenReturn(true);

        UpdateProgressRequest req = new UpdateProgressRequest("DANG_SUA", null, null, "Cố gắng sửa");

        assertThatThrownBy(() -> technicianExecutionService.updateProgress(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("HUY");
    }

    // ==========================================
    // 3. SERVICE ITEMS EXECUTION
    // ==========================================

    @Test
    void updateItemStatus_assigned_success() {
        stubCurrentTechnician();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(601, 100)).thenReturn(true);
        when(phieuSuaChuaDichVuRepository.findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(701, 601))
                .thenReturn(Optional.of(item1));
        when(phieuSuaChuaDichVuRepository.save(any(PhieuSuaChuaDichVu.class))).thenReturn(item1);

        UpdateServiceItemStatusRequest req = new UpdateServiceItemStatusRequest("HOAN_TAT");
        RepairItemResponse res = technicianExecutionService.updateItemStatus(601, 701, req);

        assertThat(res).isNotNull();
        assertThat(item1.getTrangThai()).isEqualTo("HOAN_TAT");
        verify(phieuSuaChuaDichVuRepository).save(item1);
    }

    @Test
    void updateItemStatus_unassigned_throws403() {
        stubCurrentTechnician();
        when(phieuSuaChuaRepository.findById(602)).thenReturn(Optional.of(order2));
        when(phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(602, 100)).thenReturn(false);

        UpdateServiceItemStatusRequest req = new UpdateServiceItemStatusRequest("HOAN_TAT");

        assertThatThrownBy(() -> technicianExecutionService.updateItemStatus(602, 701, req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("không được phân công");
    }
}
