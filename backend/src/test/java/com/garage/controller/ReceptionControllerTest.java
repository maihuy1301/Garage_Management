package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.CheckInRequest;
import com.garage.dto.ReceptionResponse;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.ReceptionService;
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
class ReceptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReceptionService receptionService;

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

    private ReceptionResponse sampleReception(Integer id, Integer appointmentId, Integer branchId) {
        return new ReceptionResponse(
                id, appointmentId,
                100, "51A-11111", "Toyota", "Camry",
                1, "Phạm Văn Khách Hàng", "0900000005",
                branchId, "Chi Nhánh " + branchId,
                10, "Trần Thị Tiếp Nhận",
                LocalDateTime.now(), 15500, "Xước cản trước", "Thay dầu", "DA_TIEP_NHAN"
        );
    }

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_checkIn_returns401() throws Exception {
        mockMvc.perform(post("/api/reception/check-in/1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_getReceptionSlips_returns401() throws Exception {
        mockMvc.perform(get("/api/reception"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. Denied Roles (TECHNICIAN & CUSTOMER: 403)
    // ==========================================

    @Test
    void customer_checkIn_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(post("/api/reception/check-in/1001")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void technician_checkIn_returns403() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(post("/api/reception/check-in/1001")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 3. ROLE_FRONT_DESK: Check-in own branch vs cross branch
    // ==========================================

    @Test
    void receptionist_checkIn_ownBranch_returns201() throws Exception {
        NguoiDung rec = mockUser(3, "receptionist");
        stubUser(rec, "ROLE_FRONT_DESK");
        String token = jwtService.generateToken("receptionist", List.of("ROLE_FRONT_DESK"));

        when(receptionService.checkIn(eq(1001), any(CheckInRequest.class)))
                .thenReturn(sampleReception(501, 1001, 1));

        CheckInRequest req = new CheckInRequest(15500, "Xước cản", "Thay dầu");

        mockMvc.perform(post("/api/reception/check-in/1001")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maTiepNhan").value(501))
                .andExpect(jsonPath("$.data.trangThai").value("DA_TIEP_NHAN"));
    }

    @Test
    void receptionist_checkIn_crossBranch_returns403() throws Exception {
        NguoiDung rec = mockUser(3, "receptionist");
        stubUser(rec, "ROLE_FRONT_DESK");
        String token = jwtService.generateToken("receptionist", List.of("ROLE_FRONT_DESK"));

        when(receptionService.checkIn(eq(1002), any(CheckInRequest.class)))
                .thenThrow(new AccessDeniedException("Forbidden: Bạn không có quyền tiếp nhận lịch hẹn thuộc chi nhánh khác"));

        mockMvc.perform(post("/api/reception/check-in/1002")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void receptionist_getReceptionSlips_returns200() throws Exception {
        NguoiDung rec = mockUser(3, "receptionist");
        stubUser(rec, "ROLE_FRONT_DESK");
        String token = jwtService.generateToken("receptionist", List.of("ROLE_FRONT_DESK"));

        when(receptionService.getReceptionSlips()).thenReturn(List.of(sampleReception(501, 1001, 1)));

        mockMvc.perform(get("/api/reception").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==========================================
    // 4. ROLE_MANAGER
    // ==========================================

    @Test
    void branchManager_getReceptionSlips_returns200() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(receptionService.getReceptionSlips()).thenReturn(List.of(sampleReception(501, 1001, 1)));

        mockMvc.perform(get("/api/reception").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==========================================
    // 5. ROLE_ADMIN: Global Access
    // ==========================================

    @Test
    void admin_checkIn_returns201() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        when(receptionService.checkIn(eq(1002), any(CheckInRequest.class)))
                .thenReturn(sampleReception(502, 1002, 2));

        mockMvc.perform(post("/api/reception/check-in/1002")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.maTiepNhan").value(502));
    }

    @Test
    void admin_getReceptionSlipById_notFound_returns404() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        when(receptionService.getReceptionSlipById(999))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy phiếu tiếp nhận với ID: 999"));

        mockMvc.perform(get("/api/reception/999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
