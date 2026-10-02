package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.DichVu;
import com.garage.entity.DichVuPhuTung;
import com.garage.entity.LoaiDichVu;
import com.garage.entity.PhuTung;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogServiceTest {

    @Mock
    private DichVuRepository dichVuRepository;

    @Mock
    private LoaiDichVuRepository loaiDichVuRepository;

    @Mock
    private DichVuPhuTungRepository dichVuPhuTungRepository;

    @Mock
    private PhuTungRepository phuTungRepository;

    @Mock
    private PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository;

    @Mock
    private DatLichDichVuRepository datLichDichVuRepository;

    @InjectMocks
    private ServiceCatalogService serviceCatalogService;

    private LoaiDichVu category1;
    private DichVu service1;
    private PhuTung part1;

    @BeforeEach
    void setUp() {
        category1 = new LoaiDichVu();
        category1.setMaLoaiDichVu(1);
        category1.setTenLoai("Bảo dưỡng định kỳ");
        category1.setTrangThai(true);

        service1 = new DichVu();
        service1.setMaDichVu(100);
        service1.setTenDichVu("Thay dầu động cơ");
        service1.setLoaiDichVu(category1);
        service1.setDonGia(new BigDecimal("150000"));
        service1.setTrangThai(true);

        part1 = new PhuTung();
        part1.setMaPhuTung(10);
        part1.setTenPhuTung("Dầu nhớt Castrol");
        part1.setGiaBan(new BigDecimal("350000"));
    }

    @Test
    void getAllCategories_success() {
        when(loaiDichVuRepository.findByTrangThaiTrue()).thenReturn(List.of(category1));
        when(dichVuRepository.findAll()).thenReturn(List.of(service1));

        List<ServiceCategoryResponse> list = serviceCatalogService.getAllCategories(true);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getTenLoai()).isEqualTo("Bảo dưỡng định kỳ");
        assertThat(list.get(0).getSoLuongDichVu()).isEqualTo(1L);
    }

    @Test
    void createCategory_adminSuccess() {
        ServiceCategoryRequest req = new ServiceCategoryRequest();
        req.setTenLoai("Sửa chữa điện tử");

        when(loaiDichVuRepository.existsByTenLoaiIgnoreCase("Sửa chữa điện tử")).thenReturn(false);
        when(loaiDichVuRepository.save(any(LoaiDichVu.class))).thenAnswer(i -> {
            LoaiDichVu c = i.getArgument(0);
            c.setMaLoaiDichVu(2);
            return c;
        });

        ServiceCategoryResponse res = serviceCatalogService.createCategory(req);

        assertThat(res).isNotNull();
        assertThat(res.getTenLoai()).isEqualTo("Sửa chữa điện tử");
    }

    @Test
    void createCategory_duplicate_throws409() {
        ServiceCategoryRequest req = new ServiceCategoryRequest();
        req.setTenLoai("Bảo dưỡng định kỳ");

        when(loaiDichVuRepository.existsByTenLoaiIgnoreCase("Bảo dưỡng định kỳ")).thenReturn(true);

        assertThatThrownBy(() -> serviceCatalogService.createCategory(req))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createService_adminSuccess_withDefaultParts() {
        CreateServiceRequest req = new CreateServiceRequest();
        req.setTenDichVu("Thay dầu nhớt & lọc dầu");
        req.setMaLoaiDichVu(1);
        req.setDonGia(new BigDecimal("200000"));
        req.setDefaultParts(List.of(new ServicePartItemRequest(10, 4)));

        when(loaiDichVuRepository.findById(1)).thenReturn(Optional.of(category1));
        when(dichVuRepository.save(any(DichVu.class))).thenAnswer(i -> {
            DichVu d = i.getArgument(0);
            d.setMaDichVu(101);
            return d;
        });
        when(phuTungRepository.findById(10)).thenReturn(Optional.of(part1));

        ServiceResponse res = serviceCatalogService.createService(req);

        assertThat(res).isNotNull();
        assertThat(res.getTenDichVu()).isEqualTo("Thay dầu nhớt & lọc dầu");
        verify(dichVuPhuTungRepository, times(1)).save(any(DichVuPhuTung.class));
    }

    @Test
    void updateServiceStatus_managerSuccess() {
        when(dichVuRepository.findById(100)).thenReturn(Optional.of(service1));
        when(dichVuRepository.save(any(DichVu.class))).thenReturn(service1);

        ServiceResponse res = serviceCatalogService.updateServiceStatus(100, new UpdateServiceStatusRequest(false));

        assertThat(res).isNotNull();
        assertThat(service1.getTrangThai()).isFalse();
    }

    @Test
    void updateCategoryStatus_cascadesToServices() {
        when(loaiDichVuRepository.findById(1)).thenReturn(Optional.of(category1));
        when(loaiDichVuRepository.save(any(LoaiDichVu.class))).thenReturn(category1);
        when(dichVuRepository.findAll()).thenReturn(List.of(service1));

        ServiceCategoryResponse res = serviceCatalogService.updateCategoryStatus(1, false);

        assertThat(res).isNotNull();
        assertThat(category1.getTrangThai()).isFalse();
        assertThat(service1.getTrangThai()).isFalse();
        verify(dichVuRepository, times(1)).save(service1);
    }
}

