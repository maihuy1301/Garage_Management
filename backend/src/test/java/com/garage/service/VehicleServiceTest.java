package com.garage.service;

import com.garage.dto.CreateVehicleRequest;
import com.garage.dto.UpdateVehicleRequest;
import com.garage.dto.VehicleResponse;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private XeRepository xeRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private HangXeRepository hangXeRepository;

    @Mock
    private ModelXeRepository modelXeRepository;

    @InjectMocks
    private VehicleService vehicleService;

    // ---- Fixtures ----

    private NguoiDung user1;
    private NguoiDung user2;
    private KhachHang customer1;
    private KhachHang customer2;
    private HangXe brandToyota;
    private HangXe brandHonda;
    private ModelXe modelCamry;
    private ModelXe modelCivic;
    private Xe vehicle1;
    private Xe vehicle2;

    @BeforeEach
    void setUp() {
        user1 = new NguoiDung();
        user1.setMaNguoiDung(10);
        user1.setTenDangNhap("customer1");
        user1.setHoTen("Customer One");

        user2 = new NguoiDung();
        user2.setMaNguoiDung(11);
        user2.setTenDangNhap("customer2");
        user2.setHoTen("Customer Two");

        customer1 = new KhachHang();
        customer1.setMaKhachHang(1);
        customer1.setNguoiDung(user1);

        customer2 = new KhachHang();
        customer2.setMaKhachHang(2);
        customer2.setNguoiDung(user2);

        brandToyota = new HangXe("Toyota");
        brandToyota.setMaHangXe(1);

        brandHonda = new HangXe("Honda");
        brandHonda.setMaHangXe(2);

        modelCamry = new ModelXe(brandToyota, "Camry");
        modelCamry.setMaModel(10);

        modelCivic = new ModelXe(brandHonda, "Civic");
        modelCivic.setMaModel(20);

        vehicle1 = new Xe();
        vehicle1.setMaXe(100);
        vehicle1.setBienSo("51A-11111");
        vehicle1.setModelXe(modelCamry);
        vehicle1.setTrangThai(true);
        vehicle1.setSoKmHienTai(0);
        vehicle1.setKhachHang(customer1);

        vehicle2 = new Xe();
        vehicle2.setMaXe(200);
        vehicle2.setBienSo("51B-22222");
        vehicle2.setModelXe(modelCivic);
        vehicle2.setTrangThai(true);
        vehicle2.setSoKmHienTai(0);
        vehicle2.setKhachHang(customer2);
    }

    // ---- Helper to set Security Context ----

    private void setCustomerAuth(NguoiDung user, KhachHang customer) {
        CustomUserDetails userDetails = new CustomUserDetails(user,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        when(khachHangRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()))
                .thenReturn(Optional.of(customer));
    }

    private void setAdminAuth() {
        NguoiDung adminUser = new NguoiDung();
        adminUser.setMaNguoiDung(999);
        adminUser.setTenDangNhap("admin");
        CustomUserDetails userDetails = new CustomUserDetails(adminUser,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ============================
    // CUSTOMER — getVehicles (own only)
    // ============================

    @Test
    void customer_getVehicles_returnsOwnVehiclesOnly() {
        setCustomerAuth(user1, customer1);
        when(xeRepository.findByKhachHangMaKhachHang(1)).thenReturn(List.of(vehicle1));

        List<VehicleResponse> result = vehicleService.getVehicles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBienSo()).isEqualTo("51A-11111");
        assertThat(result.get(0).getTenHangXe()).isEqualTo("Toyota");
        assertThat(result.get(0).getTenModel()).isEqualTo("Camry");
        verify(xeRepository, never()).findAll();
    }

    // ============================
    // SYSTEM_ADMIN — getVehicles (all)
    // ============================

    @Test
    void admin_getVehicles_returnsAllVehicles() {
        setAdminAuth();
        when(xeRepository.findAll()).thenReturn(List.of(vehicle1, vehicle2));

        List<VehicleResponse> result = vehicleService.getVehicles();

        assertThat(result).hasSize(2);
    }

    // ============================
    // CUSTOMER — getVehicleById (own vehicle: 200 OK)
    // ============================

    @Test
    void customer_getOwnVehicle_returns200() {
        setCustomerAuth(user1, customer1);
        when(xeRepository.findByMaXeAndKhachHangMaKhachHang(100, 1)).thenReturn(Optional.of(vehicle1));

        VehicleResponse result = vehicleService.getVehicleById(100);

        assertThat(result.getMaXe()).isEqualTo(100);
        assertThat(result.getBienSo()).isEqualTo("51A-11111");
        assertThat(result.getMaHangXe()).isEqualTo(1);
        assertThat(result.getMaModel()).isEqualTo(10);
    }

    // ============================
    // CUSTOMER — getVehicleById (other customer's vehicle: 403)
    // ============================

    @Test
    void customer_getOtherCustomerVehicle_throws403() {
        setCustomerAuth(user1, customer1);
        when(xeRepository.findByMaXeAndKhachHangMaKhachHang(200, 1)).thenReturn(Optional.empty());
        when(xeRepository.existsById(200)).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.getVehicleById(200))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ============================
    // Case A: CUSTOMER — createVehicle (valid brand & model: SUCCESS)
    // ============================

    @Test
    void customer_createVehicle_caseA_validBrandAndModel_success() {
        setCustomerAuth(user1, customer1);

        CreateVehicleRequest req = new CreateVehicleRequest();
        req.setBienSo("51C-33333");
        req.setMaHangXe(1);
        req.setMaModel(10);
        req.setNamSanXuat(2022);

        when(modelXeRepository.findById(10)).thenReturn(Optional.of(modelCamry));
        when(xeRepository.existsByBienSo("51C-33333")).thenReturn(false);

        Xe saved = new Xe();
        saved.setMaXe(300);
        saved.setBienSo("51C-33333");
        saved.setModelXe(modelCamry);
        saved.setNamSanXuat(2022);
        saved.setTrangThai(true);
        saved.setSoKmHienTai(0);
        saved.setKhachHang(customer1);
        when(xeRepository.save(any(Xe.class))).thenReturn(saved);

        VehicleResponse result = vehicleService.createVehicle(req);

        assertThat(result.getMaXe()).isEqualTo(300);
        assertThat(result.getMaKhachHang()).isEqualTo(1);
        assertThat(result.getBienSo()).isEqualTo("51C-33333");
        assertThat(result.getMaHangXe()).isEqualTo(1);
        assertThat(result.getTenHangXe()).isEqualTo("Toyota");
        assertThat(result.getMaModel()).isEqualTo(10);
        assertThat(result.getTenModel()).isEqualTo("Camry");
        verify(xeRepository).save(any(Xe.class));
    }

    // ============================
    // Case B: CUSTOMER — createVehicle (mismatched Brand and Model: REJECT 400)
    // ============================

    @Test
    void customer_createVehicle_caseB_mismatchedBrandAndModel_throws400() {
        setCustomerAuth(user1, customer1);

        CreateVehicleRequest req = new CreateVehicleRequest();
        req.setBienSo("51C-33333");
        req.setMaHangXe(2); // Honda
        req.setMaModel(10); // Camry (belongs to Toyota maHangXe=1)

        when(modelXeRepository.findById(10)).thenReturn(Optional.of(modelCamry));

        assertThatThrownBy(() -> vehicleService.createVehicle(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thuộc hãng xe ID 2");
    }

    // ============================
    // Case C: CUSTOMER — createVehicle (Model not found: REJECT 404)
    // ============================

    @Test
    void customer_createVehicle_caseC_modelNotFound_throws404() {
        setCustomerAuth(user1, customer1);

        CreateVehicleRequest req = new CreateVehicleRequest();
        req.setBienSo("51C-33333");
        req.setMaHangXe(1);
        req.setMaModel(999999);

        when(modelXeRepository.findById(999999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.createVehicle(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy model xe với ID: 999999");
    }

    // ============================
    // Case D: CUSTOMER — createVehicle (Brand not matching / invalid: REJECT 400)
    // ============================

    @Test
    void customer_createVehicle_caseD_invalidBrandMismatch_throws400() {
        setCustomerAuth(user1, customer1);

        CreateVehicleRequest req = new CreateVehicleRequest();
        req.setBienSo("51C-33333");
        req.setMaHangXe(999999);
        req.setMaModel(10);

        when(modelXeRepository.findById(10)).thenReturn(Optional.of(modelCamry));

        assertThatThrownBy(() -> vehicleService.createVehicle(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thuộc hãng xe ID 999999");
    }

    // ============================
    // Case E: CUSTOMER — createVehicle (Null Model: REJECT 400)
    // ============================

    @Test
    void customer_createVehicle_caseE_nullModel_throws400() {
        setCustomerAuth(user1, customer1);

        CreateVehicleRequest req = new CreateVehicleRequest();
        req.setBienSo("51C-33333");
        req.setMaHangXe(1);
        req.setMaModel(null);

        assertThatThrownBy(() -> vehicleService.createVehicle(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Model xe không được để trống");
    }

    // ============================
    // Case F: CUSTOMER — createVehicle (duplicate bienSo: REJECT 409)
    // ============================

    @Test
    void customer_createVehicle_caseF_duplicateBienSo_throws409() {
        setCustomerAuth(user1, customer1);

        CreateVehicleRequest req = new CreateVehicleRequest();
        req.setBienSo("51A-11111");
        req.setMaHangXe(1);
        req.setMaModel(10);

        when(modelXeRepository.findById(10)).thenReturn(Optional.of(modelCamry));
        when(xeRepository.existsByBienSo("51A-11111")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("51A-11111");
    }

    // ============================
    // CUSTOMER — updateVehicle (own vehicle: OK)
    // ============================

    @Test
    void customer_updateOwnVehicle_returns200() {
        setCustomerAuth(user1, customer1);
        when(xeRepository.findByMaXeAndKhachHangMaKhachHang(100, 1)).thenReturn(Optional.of(vehicle1));

        UpdateVehicleRequest req = new UpdateVehicleRequest();
        req.setMauXe("White");
        req.setSoKmHienTai(5000);

        vehicle1.setMauXe("White");
        vehicle1.setSoKmHienTai(5000);
        when(xeRepository.save(vehicle1)).thenReturn(vehicle1);

        VehicleResponse result = vehicleService.updateVehicle(100, req);

        assertThat(result.getMauXe()).isEqualTo("White");
        assertThat(result.getSoKmHienTai()).isEqualTo(5000);
    }

    // ============================
    // CUSTOMER — updateVehicle (other customer: 403)
    // ============================

    @Test
    void customer_updateOtherCustomerVehicle_throws403() {
        setCustomerAuth(user1, customer1);
        when(xeRepository.findByMaXeAndKhachHangMaKhachHang(200, 1)).thenReturn(Optional.empty());
        when(xeRepository.existsById(200)).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.updateVehicle(200, new UpdateVehicleRequest()))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ============================
    // CUSTOMER — deleteVehicle (own vehicle: OK)
    // ============================

    @Test
    void customer_deleteOwnVehicle_succeeds() {
        setCustomerAuth(user1, customer1);
        when(xeRepository.findByMaXeAndKhachHangMaKhachHang(100, 1)).thenReturn(Optional.of(vehicle1));
        doNothing().when(xeRepository).delete(vehicle1);

        vehicleService.deleteVehicle(100);

        verify(xeRepository).delete(vehicle1);
    }

    // ============================
    // CUSTOMER — vehicle not found: 404
    // ============================

    @Test
    void customer_getVehicle_notFound_throws404() {
        setCustomerAuth(user1, customer1);
        when(xeRepository.findByMaXeAndKhachHangMaKhachHang(999, 1)).thenReturn(Optional.empty());
        when(xeRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.getVehicleById(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
