package com.garage.service;

import com.garage.dto.BrandResponse;
import com.garage.dto.CreateBrandRequest;
import com.garage.dto.CreateModelRequest;
import com.garage.dto.ModelResponse;
import com.garage.entity.HangXe;
import com.garage.entity.ModelXe;
import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.HangXeRepository;
import com.garage.repository.ModelXeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleBrandModelServiceTest {

    @Mock
    private HangXeRepository hangXeRepository;

    @Mock
    private ModelXeRepository modelXeRepository;

    @InjectMocks
    private VehicleBrandModelService brandModelService;

    private HangXe toyota;
    private HangXe honda;
    private ModelXe camry;
    private ModelXe civic;

    @BeforeEach
    void setUp() {
        toyota = new HangXe();
        toyota.setMaHangXe(1);
        toyota.setTenHangXe("Toyota");

        honda = new HangXe();
        honda.setMaHangXe(2);
        honda.setTenHangXe("Honda");

        camry = new ModelXe();
        camry.setMaModel(101);
        camry.setHangXe(toyota);
        camry.setTenModel("Camry");

        civic = new ModelXe();
        civic.setMaModel(201);
        civic.setHangXe(honda);
        civic.setTenModel("Civic");
    }

    @Test
    void getAllBrands_returnsList() {
        when(hangXeRepository.findAll()).thenReturn(List.of(honda, toyota));

        List<BrandResponse> brands = brandModelService.getAllBrands();

        assertThat(brands).hasSize(2);
        assertThat(brands.get(0).getTenHangXe()).isEqualTo("Honda");
        assertThat(brands.get(1).getTenHangXe()).isEqualTo("Toyota");
    }

    @Test
    void getModelsByBrand_success() {
        when(hangXeRepository.existsById(1)).thenReturn(true);
        when(modelXeRepository.findByHangXeMaHangXeAndTrangThaiTrue(1)).thenReturn(List.of(camry));

        List<ModelResponse> models = brandModelService.getModelsByBrand(1);

        assertThat(models).hasSize(1);
        assertThat(models.get(0).getMaModel()).isEqualTo(101);
        assertThat(models.get(0).getTenModel()).isEqualTo("Camry");
        assertThat(models.get(0).getMaHangXe()).isEqualTo(1);
        assertThat(models.get(0).getTenHangXe()).isEqualTo("Toyota");
    }

    @Test
    void getModelsByBrand_notFound_throwsException() {
        when(hangXeRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> brandModelService.getModelsByBrand(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy hãng xe");
    }

    @Test
    void createBrand_success() {
        when(hangXeRepository.existsByTenHangXeIgnoreCase("Toyota")).thenReturn(false);
        when(hangXeRepository.save(any(HangXe.class))).thenAnswer(inv -> {
            HangXe h = inv.getArgument(0);
            h.setMaHangXe(1);
            return h;
        });

        CreateBrandRequest req = new CreateBrandRequest("Toyota");
        BrandResponse res = brandModelService.createBrand(req);

        assertThat(res).isNotNull();
        assertThat(res.getMaHangXe()).isEqualTo(1);
        assertThat(res.getTenHangXe()).isEqualTo("Toyota");
    }

    @Test
    void createBrand_duplicate_throwsBadRequest() {
        when(hangXeRepository.existsByTenHangXeIgnoreCase("Toyota")).thenReturn(true);

        CreateBrandRequest req = new CreateBrandRequest("Toyota");

        assertThatThrownBy(() -> brandModelService.createBrand(req))
                .isInstanceOf(com.garage.exception.DuplicateResourceException.class)
                .hasMessageContaining("đã tồn tại");
    }

    @Test
    void createModel_success() {
        when(hangXeRepository.findById(1)).thenReturn(Optional.of(toyota));
        when(modelXeRepository.existsByHangXeMaHangXeAndTenModelIgnoreCase(1, "Camry")).thenReturn(false);
        when(modelXeRepository.save(any(ModelXe.class))).thenAnswer(inv -> {
            ModelXe m = inv.getArgument(0);
            m.setMaModel(101);
            return m;
        });

        CreateModelRequest req = new CreateModelRequest("Camry");
        ModelResponse res = brandModelService.createModel(1, req);

        assertThat(res).isNotNull();
        assertThat(res.getMaModel()).isEqualTo(101);
        assertThat(res.getTenModel()).isEqualTo("Camry");
        assertThat(res.getMaHangXe()).isEqualTo(1);
        assertThat(res.getTenHangXe()).isEqualTo("Toyota");
    }

    @Test
    void createModel_duplicate_throwsBadRequest() {
        when(hangXeRepository.findById(1)).thenReturn(Optional.of(toyota));
        when(modelXeRepository.existsByHangXeMaHangXeAndTenModelIgnoreCase(1, "Camry")).thenReturn(true);

        CreateModelRequest req = new CreateModelRequest("Camry");

        assertThatThrownBy(() -> brandModelService.createModel(1, req))
                .isInstanceOf(com.garage.exception.DuplicateResourceException.class)
                .hasMessageContaining("đã tồn tại");
    }
}
