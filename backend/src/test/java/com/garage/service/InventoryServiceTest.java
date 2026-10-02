package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.ChiNhanh;
import com.garage.entity.GiaoDichKho;
import com.garage.entity.PhuTung;
import com.garage.entity.TonKho;
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
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private PhuTungRepository phuTungRepository;

    @Mock
    private TonKhoRepository tonKhoRepository;

    @Mock
    private ChiNhanhRepository chiNhanhRepository;

    @Mock
    private GiaoDichKhoRepository giaoDichKhoRepository;

    @Mock
    private PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository;

    @Mock
    private DichVuPhuTungRepository dichVuPhuTungRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @InjectMocks
    private InventoryService inventoryService;

    private ChiNhanh branch1;
    private PhuTung part1;
    private TonKho tonKho1;

    @BeforeEach
    void setUp() {
        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);
        branch1.setTenChiNhanh("Chi Nhánh 1");

        part1 = new PhuTung();
        part1.setMaPhuTung(10);
        part1.setMaPhuTungCode("PT001");
        part1.setTenPhuTung("Lọc dầu động cơ");
        part1.setDonViTinh("Cái");
        part1.setGiaNhap(new BigDecimal("80000.00"));
        part1.setGiaBan(new BigDecimal("150000.00"));
        part1.setTrangThai(true);

        tonKho1 = new TonKho(branch1, part1);
        tonKho1.setSoLuongTon(10);
        tonKho1.setSoLuongToiThieu(2);
    }

    @Test
    void getAllParts_success() {
        when(phuTungRepository.findByTrangThaiTrue()).thenReturn(List.of(part1));

        List<PartResponse> parts = inventoryService.getAllParts();

        assertThat(parts).hasSize(1);
        assertThat(parts.get(0).getMaPhuTungCode()).isEqualTo("PT001");
        assertThat(parts.get(0).getGiaBan()).isEqualByComparingTo("150000.00");
    }

    @Test
    void getPartById_found() {
        when(phuTungRepository.findById(10)).thenReturn(Optional.of(part1));

        PartResponse res = inventoryService.getPartById(10);

        assertThat(res).isNotNull();
        assertThat(res.getTenPhuTung()).isEqualTo("Lọc dầu động cơ");
    }

    @Test
    void getPartById_notFound_throws404() {
        when(phuTungRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getPartById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void createPart_adminSuccess() {
        CreatePartRequest req = new CreatePartRequest();
        req.setMaPhuTungCode("PT002");
        req.setTenPhuTung("Bugi Iridium");
        req.setDonViTinh("Cái");
        req.setGiaNhap(new BigDecimal("120000"));
        req.setGiaBan(new BigDecimal("220000"));

        when(phuTungRepository.existsByMaPhuTungCode("PT002")).thenReturn(false);
        when(phuTungRepository.save(any(PhuTung.class))).thenAnswer(i -> {
            PhuTung p = i.getArgument(0);
            p.setMaPhuTung(20);
            return p;
        });
        when(chiNhanhRepository.findAll()).thenReturn(List.of(branch1));

        PartResponse res = inventoryService.createPart(req);

        assertThat(res).isNotNull();
        assertThat(res.getMaPhuTungCode()).isEqualTo("PT002");
        verify(tonKhoRepository, times(1)).save(any(TonKho.class));
    }

    @Test
    void createPart_duplicateCode_throws409() {
        CreatePartRequest req = new CreatePartRequest();
        req.setMaPhuTungCode("PT001");
        req.setTenPhuTung("Trùng code");

        when(phuTungRepository.existsByMaPhuTungCode("PT001")).thenReturn(true);

        assertThatThrownBy(() -> inventoryService.createPart(req))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updatePartStatus_success() {
        when(phuTungRepository.findById(10)).thenReturn(Optional.of(part1));
        when(phuTungRepository.save(any(PhuTung.class))).thenReturn(part1);

        PartResponse res = inventoryService.updatePartStatus(10, new UpdatePartStatusRequest(false));

        assertThat(res).isNotNull();
        assertThat(part1.getTrangThai()).isFalse();
    }

    @Test
    void importStock_managerSuccess_increasesTonKhoAndCreatesGiaoDichKho() {
        StockImportRequest req = new StockImportRequest();
        req.setMaPhuTung(10);
        req.setSoLuong(15);
        req.setBranchId(1);
        req.setGhiChu("Nhập hàng đợt 1");

        when(branchAuthorizationService.resolveUserBranchId(any())).thenReturn(Optional.of(1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(branch1));
        when(phuTungRepository.findById(10)).thenReturn(Optional.of(part1));
        when(tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(1, 10)).thenReturn(Optional.of(tonKho1));
        when(tonKhoRepository.save(any(TonKho.class))).thenReturn(tonKho1);

        InventoryResponse res = inventoryService.importStock(req);

        assertThat(res).isNotNull();
        assertThat(tonKho1.getSoLuongTon()).isEqualTo(25); // 10 + 15
        verify(giaoDichKhoRepository, times(1)).save(argThat(gd ->
                "NHAP".equals(gd.getLoaiGiaoDich()) &&
                gd.getSoLuong() == 15 &&
                gd.getChiNhanh().getMaChiNhanh().equals(1) &&
                gd.getPhuTung().getMaPhuTung().equals(10)
        ));
    }

    @Test
    void importStock_forbiddenBranch_throws403() {
        StockImportRequest req = new StockImportRequest();
        req.setMaPhuTung(10);
        req.setSoLuong(5);
        req.setBranchId(2);

        when(branchAuthorizationService.resolveUserBranchId(any())).thenReturn(Optional.of(1)); // Manager of branch 1
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(false);

        assertThatThrownBy(() -> inventoryService.importStock(req))
                .isInstanceOf(AccessDeniedException.class);
    }
}
