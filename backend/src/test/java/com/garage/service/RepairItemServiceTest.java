package com.garage.service;

import com.garage.dto.CreateRepairItemRequest;
import com.garage.dto.RepairItemResponse;
import com.garage.dto.UpdateRepairItemRequest;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.DichVuRepository;
import com.garage.repository.GiaDichVuChiNhanhRepository;
import com.garage.repository.PhieuSuaChuaDichVuRepository;
import com.garage.repository.PhieuSuaChuaRepository;
import com.garage.security.BranchAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairItemServiceTest {

    @Mock
    private PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository;

    @Mock
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @Mock
    private DichVuRepository dichVuRepository;

    @Mock
    private GiaDichVuChiNhanhRepository giaDichVuChiNhanhRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private RepairItemService repairItemService;

    // Fixtures
    private ChiNhanh branch1;
    private ChiNhanh branch2;
    private PhieuSuaChua order1;
    private LoaiDichVu category;
    private DichVu service1;
    private GiaDichVuChiNhanh branchPrice;
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
        order1.setTrangThai("CHO_XU_LY");

        category = new LoaiDichVu();
        category.setMaLoaiDichVu(1);
        category.setTenLoai("Bảo Dưỡng");

        service1 = new DichVu();
        service1.setMaDichVu(10);
        service1.setTenDichVu("Thay dầu động cơ");
        service1.setLoaiDichVu(category);
        service1.setTrangThai(true);

        branchPrice = new GiaDichVuChiNhanh();
        branchPrice.setChiNhanh(branch1);
        branchPrice.setDichVu(service1);
        branchPrice.setDonGia(new BigDecimal("150000.00"));
        branchPrice.setTrangThai(true);

        item1 = new PhieuSuaChuaDichVu();
        item1.setMaChiTiet(701);
        item1.setPhieuSuaChua(order1);
        item1.setDichVu(service1);
        item1.setSoLuong(2);
        item1.setDonGia(new BigDecimal("150000.00"));
        item1.setTrangThai("CHO_XU_LY");
    }

    // ==========================================
    // 1. ADD REPAIR ITEM & PRICING
    // ==========================================

    @Test
    void addRepairItem_catalogPrice_success() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(dichVuRepository.findById(10)).thenReturn(Optional.of(service1));
        when(phieuSuaChuaDichVuRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndDichVuMaDichVu(601, 10)).thenReturn(false);
        when(giaDichVuChiNhanhRepository.findByChiNhanhMaChiNhanhAndDichVuMaDichVuAndTrangThaiTrue(1, 10))
                .thenReturn(List.of(branchPrice));
        when(phieuSuaChuaDichVuRepository.save(any(PhieuSuaChuaDichVu.class))).thenReturn(item1);

        CreateRepairItemRequest req = new CreateRepairItemRequest(10, 2);
        RepairItemResponse res = repairItemService.addRepairItem(601, req);

        assertThat(res).isNotNull();
        assertThat(res.getMaChiTiet()).isEqualTo(701);
        assertThat(res.getDonGia()).isEqualByComparingTo(new BigDecimal("150000.00"));
        assertThat(res.getThanhTien()).isEqualByComparingTo(new BigDecimal("300000.00")); // 150000 * 2
        verify(phieuSuaChuaDichVuRepository).save(any(PhieuSuaChuaDichVu.class));
    }

    @Test
    void addRepairItem_customPrice_whenNoCatalog_success() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(dichVuRepository.findById(10)).thenReturn(Optional.of(service1));
        when(phieuSuaChuaDichVuRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndDichVuMaDichVu(601, 10)).thenReturn(false);
        when(giaDichVuChiNhanhRepository.findByChiNhanhMaChiNhanhAndDichVuMaDichVuAndTrangThaiTrue(1, 10))
                .thenReturn(List.of()); // No catalog price

        PhieuSuaChuaDichVu customItem = new PhieuSuaChuaDichVu();
        customItem.setMaChiTiet(702);
        customItem.setPhieuSuaChua(order1);
        customItem.setDichVu(service1);
        customItem.setSoLuong(1);
        customItem.setDonGia(new BigDecimal("200000.00"));
        when(phieuSuaChuaDichVuRepository.save(any(PhieuSuaChuaDichVu.class))).thenReturn(customItem);

        CreateRepairItemRequest req = new CreateRepairItemRequest(10, 1, new BigDecimal("200000.00"));
        RepairItemResponse res = repairItemService.addRepairItem(601, req);

        assertThat(res.getDonGia()).isEqualByComparingTo(new BigDecimal("200000.00"));
        assertThat(res.getThanhTien()).isEqualByComparingTo(new BigDecimal("200000.00"));
    }

    @Test
    void addRepairItem_crossBranch_throws403() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(false);

        CreateRepairItemRequest req = new CreateRepairItemRequest(10, 1);

        assertThatThrownBy(() -> repairItemService.addRepairItem(601, req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }

    @Test
    void addRepairItem_duplicateService_throws409() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(dichVuRepository.findById(10)).thenReturn(Optional.of(service1));
        when(phieuSuaChuaDichVuRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndDichVuMaDichVu(601, 10)).thenReturn(true); // already exists

        CreateRepairItemRequest req = new CreateRepairItemRequest(10, 1);

        assertThatThrownBy(() -> repairItemService.addRepairItem(601, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã tồn tại");
    }

    @Test
    void addRepairItem_completedOrder_throws400() {
        order1.setTrangThai("HOAN_TAT");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        CreateRepairItemRequest req = new CreateRepairItemRequest(10, 1);

        assertThatThrownBy(() -> repairItemService.addRepairItem(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("HOAN_TAT");
    }

    @Test
    void addRepairItem_cancelledOrder_throws400() {
        order1.setTrangThai("HUY");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        CreateRepairItemRequest req = new CreateRepairItemRequest(10, 1);

        assertThatThrownBy(() -> repairItemService.addRepairItem(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("HUY");
    }

    @Test
    void addRepairItem_serviceNotFound_throws404() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(dichVuRepository.findById(999)).thenReturn(Optional.empty());

        CreateRepairItemRequest req = new CreateRepairItemRequest(999, 1);

        assertThatThrownBy(() -> repairItemService.addRepairItem(601, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==========================================
    // 2. VIEW & LIST
    // ==========================================

    @Test
    void getRepairItems_success() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phieuSuaChuaDichVuRepository.findByPhieuSuaChuaMaPhieuSuaChua(601)).thenReturn(List.of(item1));

        List<RepairItemResponse> list = repairItemService.getRepairItems(601);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getMaChiTiet()).isEqualTo(701);
        assertThat(list.get(0).getTenDichVu()).isEqualTo("Thay dầu động cơ");
    }

    // ==========================================
    // 3. UPDATE & DELETE
    // ==========================================

    @Test
    void updateRepairItem_success() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phieuSuaChuaDichVuRepository.findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(701, 601))
                .thenReturn(Optional.of(item1));
        when(phieuSuaChuaDichVuRepository.save(any(PhieuSuaChuaDichVu.class))).thenReturn(item1);

        UpdateRepairItemRequest req = new UpdateRepairItemRequest(3, new BigDecimal("160000.00"));
        RepairItemResponse res = repairItemService.updateRepairItem(601, 701, req);

        assertThat(res).isNotNull();
        assertThat(item1.getSoLuong()).isEqualTo(3);
        assertThat(item1.getDonGia()).isEqualByComparingTo(new BigDecimal("160000.00"));
    }

    @Test
    void deleteRepairItem_success() {
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(phieuSuaChuaDichVuRepository.findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(701, 601))
                .thenReturn(Optional.of(item1));

        repairItemService.deleteRepairItem(601, 701);

        verify(phieuSuaChuaDichVuRepository).delete(item1);
    }
}
