package com.garage.service;

import com.garage.dto.CreateRepairPartRequest;
import com.garage.dto.RepairPartResponse;
import com.garage.dto.UpdateRepairPartRequest;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairPartServiceTest {

    @Mock
    private PhieuSuaChuaRepository phieuSuaChuaRepository;

    @Mock
    private PhuTungRepository phuTungRepository;

    @Mock
    private TonKhoRepository tonKhoRepository;

    @Mock
    private PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository;

    @Mock
    private GiaoDichKhoRepository giaoDichKhoRepository;

    @Mock
    private PhanCongRepository phanCongRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private RepairPartService repairPartService;

    // Fixtures
    private ChiNhanh branch1;
    private PhieuSuaChua order1;
    private PhuTung part1;
    private TonKho tonKho1;
    private PhieuSuaChuaPhuTung item1;

    @BeforeEach
    void setUp() {
        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
        branch1.setTenChiNhanh("Chi Nhánh 1");

        order1 = new PhieuSuaChua();
        order1.setMaPhieuSuaChua(601);
        order1.setChiNhanh(branch1);
        order1.setTrangThai("DANG_SUA");

        part1 = new PhuTung();
        part1.setMaPhuTung(10);
        part1.setMaPhuTungCode("PT001");
        part1.setTenPhuTung("Lọc dầu động cơ");
        part1.setDonViTinh("Cái");
        part1.setGiaBan(new BigDecimal("150000.00"));
        part1.setTrangThai(true);

        tonKho1 = new TonKho(branch1, part1);
        tonKho1.setSoLuongTon(10);
        tonKho1.setSoLuongToiThieu(2);

        item1 = new PhieuSuaChuaPhuTung();
        item1.setMaChiTiet(701);
        item1.setPhieuSuaChua(order1);
        item1.setPhuTung(part1);
        item1.setSoLuong(2);
        item1.setDonGia(new BigDecimal("150000.00"));
    }

    private void stubAdminAuth() {
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
    }

    // ==========================================
    // 1. ADD PART TO REPAIR ORDER
    // ==========================================

    @Test
    void addPartToRepairOrder_success_decreasesStockAndRecordsTransaction() {
        stubAdminAuth();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phuTungRepository.findById(10)).thenReturn(Optional.of(part1));
        when(phieuSuaChuaPhuTungRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndPhuTungMaPhuTung(601, 10)).thenReturn(false);
        when(tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(1, 10)).thenReturn(Optional.of(tonKho1));
        when(phieuSuaChuaPhuTungRepository.save(any(PhieuSuaChuaPhuTung.class))).thenReturn(item1);

        CreateRepairPartRequest req = new CreateRepairPartRequest(10, 2);
        RepairPartResponse res = repairPartService.addPartToRepairOrder(601, req);

        assertThat(res).isNotNull();
        assertThat(tonKho1.getSoLuongTon()).isEqualTo(8); // 10 - 2 = 8
        assertThat(res.getDonGia()).isEqualByComparingTo("150000.00");
        assertThat(res.getThanhTien()).isEqualByComparingTo("300000.00");

        verify(tonKhoRepository).save(tonKho1);
        verify(giaoDichKhoRepository).save(any(GiaoDichKho.class));
        verify(phieuSuaChuaPhuTungRepository).save(any(PhieuSuaChuaPhuTung.class));
    }

    @Test
    void addPartToRepairOrder_insufficientStock_throws400() {
        stubAdminAuth();
        tonKho1.setSoLuongTon(3);
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phuTungRepository.findById(10)).thenReturn(Optional.of(part1));
        when(phieuSuaChuaPhuTungRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndPhuTungMaPhuTung(601, 10)).thenReturn(false);
        when(tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(1, 10)).thenReturn(Optional.of(tonKho1));

        CreateRepairPartRequest req = new CreateRepairPartRequest(10, 5); // request 5 > stock 3

        assertThatThrownBy(() -> repairPartService.addPartToRepairOrder(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tồn kho không đủ");
    }

    @Test
    void addPartToRepairOrder_duplicatePart_throws409() {
        stubAdminAuth();
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phuTungRepository.findById(10)).thenReturn(Optional.of(part1));
        when(phieuSuaChuaPhuTungRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndPhuTungMaPhuTung(601, 10)).thenReturn(true);

        CreateRepairPartRequest req = new CreateRepairPartRequest(10, 2);

        assertThatThrownBy(() -> repairPartService.addPartToRepairOrder(601, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã có trong phiếu sửa chữa");
    }

    @Test
    void addPartToRepairOrder_inactivePart_throws400() {
        stubAdminAuth();
        part1.setTrangThai(false);
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phuTungRepository.findById(10)).thenReturn(Optional.of(part1));

        CreateRepairPartRequest req = new CreateRepairPartRequest(10, 2);

        assertThatThrownBy(() -> repairPartService.addPartToRepairOrder(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ngưng hoạt động");
    }

    @Test
    void addPartToRepairOrder_completedOrder_throws400() {
        stubAdminAuth();
        order1.setTrangThai("HOAN_TAT");
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));

        CreateRepairPartRequest req = new CreateRepairPartRequest(10, 2);

        assertThatThrownBy(() -> repairPartService.addPartToRepairOrder(601, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("HOAN_TAT");
    }

    @Test
    void addPartToRepairOrder_unauthorizedBranch_throws403() {
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(false);
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));

        CreateRepairPartRequest req = new CreateRepairPartRequest(10, 2);

        assertThatThrownBy(() -> repairPartService.addPartToRepairOrder(601, req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }

    // ==========================================
    // 2. UPDATE QUANTITY & STOCK ADJUSTMENT
    // ==========================================

    @Test
    void updatePartQuantity_increaseQuantity_deductsExtraStock() {
        stubAdminAuth();
        tonKho1.setSoLuongTon(10);
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phieuSuaChuaPhuTungRepository.findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(701, 601))
                .thenReturn(Optional.of(item1)); // old quantity = 2
        when(tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(1, 10)).thenReturn(Optional.of(tonKho1));
        when(phieuSuaChuaPhuTungRepository.save(any(PhieuSuaChuaPhuTung.class))).thenReturn(item1);

        UpdateRepairPartRequest req = new UpdateRepairPartRequest(5); // increase 2 -> 5 (diff = +3)
        RepairPartResponse res = repairPartService.updatePartQuantity(601, 701, req);

        assertThat(res).isNotNull();
        assertThat(tonKho1.getSoLuongTon()).isEqualTo(7); // 10 - 3 = 7
        assertThat(item1.getSoLuong()).isEqualTo(5);
        verify(tonKhoRepository).save(tonKho1);
        verify(giaoDichKhoRepository).save(any(GiaoDichKho.class));
    }

    @Test
    void updatePartQuantity_decreaseQuantity_returnsStock() {
        stubAdminAuth();
        item1.setSoLuong(5); // old quantity = 5
        tonKho1.setSoLuongTon(7);
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phieuSuaChuaPhuTungRepository.findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(701, 601))
                .thenReturn(Optional.of(item1));
        when(tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(1, 10)).thenReturn(Optional.of(tonKho1));
        when(phieuSuaChuaPhuTungRepository.save(any(PhieuSuaChuaPhuTung.class))).thenReturn(item1);

        UpdateRepairPartRequest req = new UpdateRepairPartRequest(2); // decrease 5 -> 2 (diff = -3)
        RepairPartResponse res = repairPartService.updatePartQuantity(601, 701, req);

        assertThat(res).isNotNull();
        assertThat(tonKho1.getSoLuongTon()).isEqualTo(10); // 7 + 3 = 10
        assertThat(item1.getSoLuong()).isEqualTo(2);
        verify(tonKhoRepository).save(tonKho1);
        verify(giaoDichKhoRepository).save(any(GiaoDichKho.class));
    }

    // ==========================================
    // 3. DELETE PART & STOCK RETURN
    // ==========================================

    @Test
    void deletePartFromRepairOrder_returnsStockAndDeletesItem() {
        stubAdminAuth();
        tonKho1.setSoLuongTon(8);
        item1.setSoLuong(2);
        when(phieuSuaChuaRepository.findById(601)).thenReturn(Optional.of(order1));
        when(phieuSuaChuaPhuTungRepository.findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(701, 601))
                .thenReturn(Optional.of(item1));
        when(tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(1, 10)).thenReturn(Optional.of(tonKho1));

        repairPartService.deletePartFromRepairOrder(601, 701);

        assertThat(tonKho1.getSoLuongTon()).isEqualTo(10); // 8 + 2 = 10
        verify(tonKhoRepository).save(tonKho1);
        verify(giaoDichKhoRepository).save(any(GiaoDichKho.class));
        verify(phieuSuaChuaPhuTungRepository).delete(item1);
    }
}
