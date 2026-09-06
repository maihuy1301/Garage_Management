package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.AssignmentResponse;
import com.garage.dto.CreateAssignmentRequest;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.TechnicianAssignmentService;
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
class TechnicianAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TechnicianAssignmentService technicianAssignmentService;

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
        u.setMaPinHash("$2a$10$hashedPinValue");
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

    private AssignmentResponse sampleAssignment(Integer id, Integer orderId, Integer techId) {
        return new AssignmentResponse(
                id, orderId, 2, "Nguyễn Văn Quản Lý",
                techId, "Nguyễn Văn Kỹ Thuật",
                LocalDateTime.now(), "DA_PHAN_CONG"
        );
    }

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_getAssignments_returns401() throws Exception {
        mockMvc.perform(get("/api/repair-orders/601/assignments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_createAssignment_returns401() throws Exception {
        mockMvc.perform(post("/api/repair-orders/601/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. Denied Roles (CUSTOMER, TECHNICIAN, RECEPTIONIST: 403)
    // ==========================================

    @Test
    void customer_getAssignments_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/repair-orders/601/assignments").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void technician_createAssignment_returns403() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);

        mockMvc.perform(post("/api/repair-orders/601/assignments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void receptionist_createAssignment_returns403() throws Exception {
        NguoiDung rec = mockUser(3, "receptionist");
        stubUser(rec, "ROLE_FRONT_DESK");
        String token = jwtService.generateToken("receptionist", List.of("ROLE_FRONT_DESK"));

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);

        mockMvc.perform(post("/api/repair-orders/601/assignments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 3. ROLE_MANAGER: Assign, View, Delete
    // ==========================================

    @Test
    void branchManager_createAssignment_ownBranch_returns201() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);
        when(technicianAssignmentService.createAssignment(eq(601), any(CreateAssignmentRequest.class)))
                .thenReturn(sampleAssignment(801, 601, 100));

        mockMvc.perform(post("/api/repair-orders/601/assignments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maPhanCong").value(801))
                .andExpect(jsonPath("$.data.tenNhanVienDuocPhanCong").value("Nguyễn Văn Kỹ Thuật"));
    }

    @Test
    void branchManager_createAssignment_crossBranch_returns403() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(technicianAssignmentService.createAssignment(eq(602), any(CreateAssignmentRequest.class)))
                .thenThrow(new AccessDeniedException("Forbidden: Bạn không có quyền truy cập phiếu sửa chữa của chi nhánh khác"));

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);

        mockMvc.perform(post("/api/repair-orders/602/assignments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void branchManager_getAssignments_returns200() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(technicianAssignmentService.getAssignments(601)).thenReturn(List.of(sampleAssignment(801, 601, 100)));

        mockMvc.perform(get("/api/repair-orders/601/assignments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void branchManager_deleteAssignment_returns200() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        mockMvc.perform(delete("/api/repair-orders/601/assignments/801").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==========================================
    // 4. ROLE_ADMIN: Assign any branch
    // ==========================================

    @Test
    void admin_createAssignment_returns201() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        CreateAssignmentRequest req = new CreateAssignmentRequest(100);
        when(technicianAssignmentService.createAssignment(eq(602), any(CreateAssignmentRequest.class)))
                .thenReturn(sampleAssignment(802, 602, 100));

        mockMvc.perform(post("/api/repair-orders/602/assignments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.maPhanCong").value(802));
    }
}
