package com.garage.service;

import com.garage.dto.BranchResponse;
import com.garage.dto.CreateBranchRequest;
import com.garage.dto.UpdateBranchRequest;
import com.garage.dto.UpdateBranchStatusRequest;
import com.garage.entity.ChiNhanh;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private ChiNhanhRepository chiNhanhRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private DatLichRepository datLichRepository;

    @Mock
    private PhieuTiepNhanRepository phieuTiepNhanRepository;

    @Mock
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @Mock
    private HoaDonRepository hoaDonRepository;

    @Mock
    private TonKhoRepository tonKhoRepository;

    @InjectMocks
    private BranchService branchService;

    private ChiNhanh branch1;
    private ChiNhanh branch2Inactive;

    @BeforeEach
    void setUp() {
        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
        branch1.setTenChiNhanh("Chi Nhánh 1");
        branch1.setDiaChi("123 Đường A");
        branch1.setSoDienThoai("0900000001");
        branch1.setEmail("cn1@garage.com");
        branch1.setTrangThai(true);
        branch1.setNgayTao(LocalDateTime.now());

        branch2Inactive = new ChiNhanh();
        branch2Inactive.setMaChiNhanh(2);
        branch2Inactive.setTenChiNhanh("Chi Nhánh 2 Đóng Cửa");
        branch2Inactive.setDiaChi("456 Đường B");
        branch2Inactive.setSoDienThoai("0900000002");
        branch2Inactive.setEmail("cn2@garage.com");
        branch2Inactive.setTrangThai(false);
        branch2Inactive.setNgayTao(LocalDateTime.now());
    }

    @Test
    void getAllBranches_Default_ReturnsOnlyActive() {
        when(chiNhanhRepository.findByTrangThaiTrue()).thenReturn(List.of(branch1));

        List<BranchResponse> list = branchService.getAllBranches();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getMaChiNhanh()).isEqualTo(1);
        verify(chiNhanhRepository).findByTrangThaiTrue();
    }

    @Test
    void getAllBranches_AdminWithIncludeInactive_ReturnsAll() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "pwd", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        when(chiNhanhRepository.findAll()).thenReturn(List.of(branch1, branch2Inactive));

        List<BranchResponse> list = branchService.getAllBranches(true);

        assertThat(list).hasSize(2);
        assertThat(list.get(1).getTrangThai()).isFalse();
        verify(chiNhanhRepository).findAll();
    }

    @Test
    void getBranchById_Found_ReturnsResponse() {
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));

        BranchResponse resp = branchService.getBranchById(1);

        assertThat(resp).isNotNull();
        assertThat(resp.getTenChiNhanh()).isEqualTo("Chi Nhánh 1");
    }

    @Test
    void getBranchById_NotFound_ThrowsException() {
        when(chiNhanhRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> branchService.getBranchById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createBranch_Success() {
        CreateBranchRequest req = new CreateBranchRequest("Chi Nhánh 3", "789 Đường C", "0900000003", "cn3@garage.com", true);
        when(chiNhanhRepository.existsByTenChiNhanh("Chi Nhánh 3")).thenReturn(false);

        ChiNhanh saved = new ChiNhanh();
        saved.setMaChiNhanh(3);
        saved.setTenChiNhanh(req.getTenChiNhanh());
        saved.setDiaChi(req.getDiaChi());
        saved.setSoDienThoai(req.getSoDienThoai());
        saved.setEmail(req.getEmail());
        saved.setTrangThai(true);

        when(chiNhanhRepository.save(any(ChiNhanh.class))).thenReturn(saved);

        BranchResponse resp = branchService.createBranch(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getMaChiNhanh()).isEqualTo(3);
        assertThat(resp.getTenChiNhanh()).isEqualTo("Chi Nhánh 3");
    }

    @Test
    void createBranch_DuplicateName_ThrowsException() {
        CreateBranchRequest req = new CreateBranchRequest("Chi Nhánh 1", "123 Đường A", "0900000001", "cn1@garage.com", true);
        when(chiNhanhRepository.existsByTenChiNhanh("Chi Nhánh 1")).thenReturn(true);

        assertThatThrownBy(() -> branchService.createBranch(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã tồn tại");
    }

    @Test
    void updateBranch_Success() {
        UpdateBranchRequest req = new UpdateBranchRequest("Chi Nhánh 1 Sửa", "123 Đường A Mới", "0900000001", "cn1_new@garage.com", true);
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));
        when(chiNhanhRepository.existsByTenChiNhanhAndMaChiNhanhNot("Chi Nhánh 1 Sửa", 1)).thenReturn(false);
        when(chiNhanhRepository.save(any(ChiNhanh.class))).thenAnswer(i -> i.getArgument(0));

        BranchResponse resp = branchService.updateBranch(1, req);

        assertThat(resp.getTenChiNhanh()).isEqualTo("Chi Nhánh 1 Sửa");
        assertThat(resp.getDiaChi()).isEqualTo("123 Đường A Mới");
    }

    @Test
    void updateBranch_DuplicateName_ThrowsException() {
        UpdateBranchRequest req = new UpdateBranchRequest("Chi Nhánh Trùng", "123 Đường A", "0900000001", "cn1@garage.com", true);
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));
        when(chiNhanhRepository.existsByTenChiNhanhAndMaChiNhanhNot("Chi Nhánh Trùng", 1)).thenReturn(true);

        assertThatThrownBy(() -> branchService.updateBranch(1, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã được sử dụng");
    }

    @Test
    void updateBranchStatus_Success() {
        UpdateBranchStatusRequest req = new UpdateBranchStatusRequest(false);
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));
        when(chiNhanhRepository.save(any(ChiNhanh.class))).thenAnswer(i -> i.getArgument(0));

        BranchResponse resp = branchService.updateBranchStatus(1, req);

        assertThat(resp.getTrangThai()).isFalse();
    }

    @Test
    void deleteBranch_NoDependencies_Success() {
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));
        when(nhanVienRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(false);
        when(datLichRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(false);
        when(phieuTiepNhanRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(false);
        when(phieuSuaChuaRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(false);
        when(hoaDonRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(false);
        when(tonKhoRepository.existsByIdMaChiNhanh(1)).thenReturn(false);

        branchService.deleteBranch(1);

        verify(chiNhanhRepository).delete(branch1);
    }

    @Test
    void deleteBranch_HasEmployees_ThrowsBadRequestException() {
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));
        when(nhanVienRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(true);

        assertThatThrownBy(() -> branchService.deleteBranch(1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thể xóa cứng");

        verify(chiNhanhRepository, never()).delete(any());
    }

    @Test
    void deleteBranch_HasInventory_ThrowsBadRequestException() {
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));
        when(nhanVienRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(false);
        when(datLichRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(false);
        when(phieuTiepNhanRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(false);
        when(phieuSuaChuaRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(false);
        when(hoaDonRepository.existsByChiNhanhMaChiNhanh(1)).thenReturn(false);
        when(tonKhoRepository.existsByIdMaChiNhanh(1)).thenReturn(true);

        assertThatThrownBy(() -> branchService.deleteBranch(1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thể xóa cứng");

        verify(chiNhanhRepository, never()).delete(any());
    }
}
