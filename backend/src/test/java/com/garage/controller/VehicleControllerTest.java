package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.CreateVehicleRequest;
import com.garage.dto.UpdateVehicleRequest;
import com.garage.dto.VehicleResponse;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private NguoiDungRepository nguoiDungRepository;

    @MockBean
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    // ---- Fixtures ----

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

    private VehicleResponse sampleVehicle(Integer maXe, Integer maKhachHang, String bienSo) {
        return new VehicleResponse(maXe, maKhachHang,
                "Chủ Xe " + maKhachHang, bienSo, "Toyota", "Camry",
                2022, "Black", null, 0, true, LocalDateTime.now());
    }

    // ============================
    // No JWT → 401
    // ============================

    @Test
    void noJwt_getVehicles_returns401() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void noJwt_getVehicleById_returns401() throws Exception {
        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void noJwt_createVehicle_returns401() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ============================
    // Wrong role → 403
    // ============================

    @Test
    void branchManager_getVehicles_returns403() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/api/vehicles").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void receptionist_getVehicles_returns403() throws Exception {
        NguoiDung rec = mockUser(3, "receptionist");
        stubUser(rec, "ROLE_FRONT_DESK");
        String token = jwtService.generateToken("receptionist", List.of("ROLE_FRONT_DESK"));

        mockMvc.perform(get("/api/vehicles").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void technician_getVehicles_returns403() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(get("/api/vehicles").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ============================
    // SYSTEM_ADMIN → toàn bộ xe
    // ============================

    @Test
    void admin_getVehicles_returns200() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        when(vehicleService.getVehicles())
                .thenReturn(List.of(sampleVehicle(100, 1, "51A-11111"), sampleVehicle(200, 2, "51B-22222")));

        mockMvc.perform(get("/api/vehicles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void admin_getVehicleById_returns200() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        when(vehicleService.getVehicleById(100)).thenReturn(sampleVehicle(100, 1, "51A-11111"));

        mockMvc.perform(get("/api/vehicles/100").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bienSo").value("51A-11111"));
    }

    // ============================
    // CUSTOMER — own vehicle: 200 OK
    // ============================

    @Test
    void customer_getOwnVehicle_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        when(vehicleService.getVehicleById(100)).thenReturn(sampleVehicle(100, 1, "51A-11111"));

        mockMvc.perform(get("/api/vehicles/100").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bienSo").value("51A-11111"));
    }

    @Test
    void customer_getVehicles_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        when(vehicleService.getVehicles()).thenReturn(List.of(sampleVehicle(100, 1, "51A-11111")));

        mockMvc.perform(get("/api/vehicles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ============================
    // CUSTOMER — cross-owner: service throws AccessDeniedException → 403
    // ============================

    @Test
    void customer_crossOwnerVehicle_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        when(vehicleService.getVehicleById(200))
                .thenThrow(new AccessDeniedException("Forbidden: Bạn không có quyền truy cập xe này"));

        mockMvc.perform(get("/api/vehicles/200").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ============================
    // CUSTOMER — create vehicle: 201
    // ============================

    @Test
    void customer_createVehicle_returns201() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        CreateVehicleRequest req = new CreateVehicleRequest();
        req.setBienSo("51C-33333");
        req.setHangXe("Toyota");
        req.setModel("Vios");

        when(vehicleService.createVehicle(any(CreateVehicleRequest.class)))
                .thenReturn(sampleVehicle(300, 1, "51C-33333"));

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bienSo").value("51C-33333"));
    }

    // ============================
    // CUSTOMER — update own vehicle: 200
    // ============================

    @Test
    void customer_updateOwnVehicle_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        UpdateVehicleRequest req = new UpdateVehicleRequest();
        req.setMauXe("White");

        VehicleResponse updated = sampleVehicle(100, 1, "51A-11111");
        when(vehicleService.updateVehicle(eq(100), any(UpdateVehicleRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/vehicles/100")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ============================
    // Vehicle not found → 404
    // ============================

    @Test
    void customer_vehicleNotFound_returns404() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        when(vehicleService.getVehicleById(999))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy xe với ID: 999"));

        mockMvc.perform(get("/api/vehicles/999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ============================
    // DELETE — customer own vehicle: 200
    // ============================

    @Test
    void customer_deleteOwnVehicle_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        mockMvc.perform(delete("/api/vehicles/100").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
