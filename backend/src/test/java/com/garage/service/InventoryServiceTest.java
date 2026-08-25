package com.garage.service;

import com.garage.dto.InventoryResponse;
import com.garage.dto.PartResponse;
import com.garage.entity.ChiNhanh;
import com.garage.entity.PhuTung;
import com.garage.entity.TonKho;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.PhuTungRepository;
import com.garage.repository.TonKhoRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private PhuTungRepository phuTungRepository;

    @Mock
    private TonKhoRepository tonKhoRepository;

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
        branch1.setMaChiNhanhCode("CN001");
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
    void getInventoryByBranch_allowedBranch_success() {
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(tonKhoRepository.findByIdMaChiNhanhAndPhuTungTrangThaiTrue(1)).thenReturn(List.of(tonKho1));

        List<InventoryResponse> list = inventoryService.getInventoryByBranch(1);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getSoLuongTon()).isEqualTo(10);
        assertThat(list.get(0).getTenPhuTung()).isEqualTo("Lọc dầu động cơ");
    }

    @Test
    void getInventoryByBranch_unauthorizedBranch_throws403() {
        when(branchAuthorizationService.isAllowedBranch(2)).thenReturn(false);

        assertThatThrownBy(() -> inventoryService.getInventoryByBranch(2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chi nhánh khác");
    }

    @Test
    void getInventoryDetail_success() {
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(1, 10)).thenReturn(Optional.of(tonKho1));

        InventoryResponse res = inventoryService.getInventoryDetail(1, 10);

        assertThat(res).isNotNull();
        assertThat(res.getSoLuongTon()).isEqualTo(10);
    }
}
