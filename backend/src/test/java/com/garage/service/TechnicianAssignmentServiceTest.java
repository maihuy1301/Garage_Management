package com.garage.service;

import com.garage.dto.AssignmentResponse;
import com.garage.dto.CreateAssignmentRequest;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.NhanVienRepository;
import com.garage.repository.PhanCongRepository;
import com.garage.repository.PhieuSuaChuaRepository;
import com.garage.security.BranchAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianAssignmentServiceTest {

    @Mock
    private PhanCongRepository phanCongRepository;

    @Mock
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private TechnicianAssignmentService technicianAssignmentService;

    // Fixtures
    private ChiNhanh branch1;
    private ChiNhanh branch2;
    private PhieuSuaChua order1;
    private NguoiDung userTech1;
    private NhanVien tech1;
    private VaiTro roleTech;
    private NguoiDungVaiTro userRoleTech;
    private PhanCong assignment1;

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

        order1 = new PhieuSuaChua();
        order1.setMaPhieuSuaChua(601);
        order1.setChiNhanh(branch1);
        order1.setTrangThai("CHO_XU_LY");

        userTech1 = new NguoiDung();
        userTech1.setMaNguoiDung(10);
        userTech1.setTenDangNhap("technician1");
        userTech1.setHoTen("Nguyễn Văn Kỹ Thuật");
        userTech1.setTrangThai(true);

        tech1 = new NhanVien();
        tech1.setMaNhanVien(100);
        tech1.setMaNhanVienCode("NV001");
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
        assignment1.setNhanVien(tech1);
        assignment1.setVaiTroTrongCongViec("Kỹ thuật viên chính");
        assignment1.setTrangThai("DA_GIAO");
    }

    // ==========================================
    // 1. CREATE ASSIGNMENT TESTS
    // ==========================================

    @Test
    void createAssignment_success_updatesOrderStatusToAssigned() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(nhanVienRepository.findById(100)).thenReturn(Optional.of(tech1));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(10)).thenReturn(List.of(userRoleTech));
        when(phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienMaNhanVien(601, 100)).thenReturn(false);
        when(phanCongRepository.save(any(PhanCong.class))).thenReturn(assignment1);

        CreateAssignmentRequest req = new CreateAssignmentRequest(100, "Kỹ thuật viên chính");
        AssignmentResponse res = technicianAssignmentService.createAssignment(601, req);

        assertThat(res).isNotNull();
        assertThat(res.getMaPhanCong()).isEqualTo(801);
        assertThat(res.getMaNhanVien()).isEqualTo(100);
        assertThat(res.getTenNhanVien()).isEqualTo("Nguyễn Văn Kỹ Thuật");
        assertThat(order1.getTrangThai()).isEqualTo("DA_PHAN_CONG");
        verify(phieuSuaChuaRepository).save(order1);
        verify(phanCongRepository).save(any(PhanCong.class));
    }

    @Test
    void createAssignment_crossBranch_throws403() {
        tech1.setChiNhanh(branch2); // Technician belongs to CN002, order is CN001
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(nhanVienRepository.findById(100)).thenReturn(Optional.of(tech1));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(10)).thenReturn(List.of(userRoleTech));

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);

        assertThatThrownBy(() -> technicianAssignmentService.createAssignment(601, req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh");
    }

    @Test
    void createAssignment_callerNotAuthorizedForBranch_throws403() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(false);

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);

        assertThatThrownBy(() -> technicianAssignmentService.createAssignment(601, req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }

    @Test
    void createAssignment_duplicateTechnician_throws409() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(nhanVienRepository.findById(100)).thenReturn(Optional.of(tech1));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(10)).thenReturn(List.of(userRoleTech));
        when(phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienMaNhanVien(601, 100)).thenReturn(true); // already assigned

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);

        assertThatThrownBy(() -> technicianAssignmentService.createAssignment(601, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã được phân công");
    }

    @Test
    void createAssignment_employeeNotTechnicianRole_throws400() {
        VaiTro roleMgr = new VaiTro();
        roleMgr.setMaVaiTro(2);
        roleMgr.setTenVaiTro("ROLE_MANAGER");
        NguoiDungVaiTro userRoleMgr = new NguoiDungVaiTro(userTech1, roleMgr);

        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(nhanVienRepository.findById(100)).thenReturn(Optional.of(tech1));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(10)).thenReturn(List.of(userRoleMgr));

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);

        assertThatThrownBy(() -> technicianAssignmentService.createAssignment(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ROLE_TECHNICIAN");
    }

    @Test
    void createAssignment_completedOrder_throws400() {
        order1.setTrangThai("HOAN_TAT");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);

        assertThatThrownBy(() -> technicianAssignmentService.createAssignment(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("HOAN_TAT");
    }

    @Test
    void createAssignment_cancelledOrder_throws400() {
        order1.setTrangThai("HUY");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);

        assertThatThrownBy(() -> technicianAssignmentService.createAssignment(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("HUY");
    }

    @Test
    void createAssignment_technicianNotFound_throws404() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(nhanVienRepository.findById(999)).thenReturn(Optional.empty());

        CreateAssignmentRequest req = new CreateAssignmentRequest(999);

        assertThatThrownBy(() -> technicianAssignmentService.createAssignment(601, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==========================================
    // 2. VIEW ASSIGNMENTS
    // ==========================================

    @Test
    void getAssignments_success() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phanCongRepository.findByPhieuSuaChuaMaPhieuSuaChua(601)).thenReturn(List.of(assignment1));

        List<AssignmentResponse> list = technicianAssignmentService.getAssignments(601);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getMaPhanCong()).isEqualTo(801);
        assertThat(list.get(0).getTenNhanVien()).isEqualTo("Nguyễn Văn Kỹ Thuật");
    }

    // ==========================================
    // 3. DELETE ASSIGNMENT
    // ==========================================

    @Test
    void deleteAssignment_lastAssignment_revertsStatusToPending() {
        order1.setTrangThai("DA_PHAN_CONG");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phanCongRepository.findByMaPhanCongAndPhieuSuaChuaMaPhieuSuaChua(801, 601))
                .thenReturn(Optional.of(assignment1));
        when(phanCongRepository.countByPhieuSuaChuaMaPhieuSuaChua(601)).thenReturn(0L); // 0 left

        technicianAssignmentService.deleteAssignment(601, 801);

        verify(phanCongRepository).delete(assignment1);
        assertThat(order1.getTrangThai()).isEqualTo("CHO_XU_LY");
        verify(phieuSuaChuaRepository).save(order1);
    }
}
