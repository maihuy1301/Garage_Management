package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.*;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.ServiceCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServiceCatalogService serviceCatalogService;

    @MockBean
    private NguoiDungRepository nguoiDungRepository;

    @MockBean
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    private NguoiDung mockUser(Integer id, String username) {
        NguoiDung u = new NguoiDung();
        u.setMaNguoiDung(id);
        u.setTenDangNhap(username);
        u.setHoTen(username + " FullName");
        u.setEmail(username + "@garage.com");
        u.setMatKhauHash("$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO");
        u.setTrangThai(true);
        return u;
    }

    private void stubUser(NguoiDung user, String roleName) {
        VaiTro role = new VaiTro();
        role.setMaVaiTro(user.getMaNguoiDung());
        role.setTenVaiTro(roleName);
        when(nguoiDungRepository.findByTenDangNhapOrEmail(user.getTenDangNhap(), user.getTenDangNhap()))
                .thenReturn(Optional.of(user));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()))
                .thenReturn(List.of(new NguoiDungVaiTro(user, role)));
    }

    private ServiceResponse sampleService(Integer id) {
        return new ServiceResponse(
                id, 1, "Bảo dưỡng", "Thay dầu động cơ", "Mô tả",
                new BigDecimal("150000"), 30, true, List.of(), BigDecimal.ZERO, new BigDecimal("150000")
        );
    }

    @Test
    void customer_getServices_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken(cust.getTenDangNhap(), List.of("ROLE_CUSTOMER"));

        when(serviceCatalogService.getAllServices(false, null)).thenReturn(List.of(sampleService(1)));

        mockMvc.perform(get("/api/services")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tenDichVu").value("Thay dầu động cơ"));
    }

    @Test
    void admin_createService_returns201() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken(admin.getTenDangNhap(), List.of("ROLE_ADMIN"));

        CreateServiceRequest req = new CreateServiceRequest();
        req.setTenDichVu("Bảo dưỡng phanh");
        req.setMaLoaiDichVu(1);
        req.setDonGia(new BigDecimal("200000"));

        when(serviceCatalogService.createService(any(CreateServiceRequest.class))).thenReturn(sampleService(10));

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void manager_createService_returns403() throws Exception {
        NguoiDung manager = mockUser(2, "manager");
        stubUser(manager, "ROLE_MANAGER");
        String token = jwtService.generateToken(manager.getTenDangNhap(), List.of("ROLE_MANAGER"));

        CreateServiceRequest req = new CreateServiceRequest();
        req.setTenDichVu("Bảo dưỡng phanh");
        req.setMaLoaiDichVu(1);
        req.setDonGia(new BigDecimal("200000"));

        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void manager_updateServiceStatus_returns200() throws Exception {
        NguoiDung manager = mockUser(2, "manager");
        stubUser(manager, "ROLE_MANAGER");
        String token = jwtService.generateToken(manager.getTenDangNhap(), List.of("ROLE_MANAGER"));

        UpdateServiceStatusRequest req = new UpdateServiceStatusRequest(false);
        when(serviceCatalogService.updateServiceStatus(eq(1), any(UpdateServiceStatusRequest.class)))
                .thenReturn(sampleService(1));

        mockMvc.perform(patch("/api/services/1/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void manager_deleteService_returns403() throws Exception {
        NguoiDung manager = mockUser(2, "manager");
        stubUser(manager, "ROLE_MANAGER");
        String token = jwtService.generateToken(manager.getTenDangNhap(), List.of("ROLE_MANAGER"));

        mockMvc.perform(delete("/api/services/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_createCategory_returns201() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken(admin.getTenDangNhap(), List.of("ROLE_ADMIN"));

        ServiceCategoryRequest req = new ServiceCategoryRequest();
        req.setTenLoai("Chăm sóc xe");

        when(serviceCatalogService.createCategory(any(ServiceCategoryRequest.class)))
                .thenReturn(new ServiceCategoryResponse(1, "Chăm sóc xe", "", true, 0L));

        mockMvc.perform(post("/api/service-categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void manager_createCategory_returns403() throws Exception {
        NguoiDung manager = mockUser(2, "manager");
        stubUser(manager, "ROLE_MANAGER");
        String token = jwtService.generateToken(manager.getTenDangNhap(), List.of("ROLE_MANAGER"));

        ServiceCategoryRequest req = new ServiceCategoryRequest();
        req.setTenLoai("Chăm sóc xe");

        mockMvc.perform(post("/api/service-categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
