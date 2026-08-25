package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.*;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.RepairOrderService;
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
class RepairOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepairOrderService repairOrderService;

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

    private RepairOrderResponse sampleOrder(Integer id, Integer receptionId, Integer branchId) {
        return new RepairOrderResponse(
                id, receptionId, 1001,
                100, "51A-11111", "Toyota", "Camry",
                1, "KH001", "Phạm Văn Khách Hàng", "0900000005",
                branchId, "CN00" + branchId, "Chi Nhánh " + branchId,
                LocalDateTime.now(), null, "CHO_XU_LY", "Kiểm tra động cơ"
        );
    }

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_getRepairOrders_returns401() throws Exception {
        mockMvc.perform(get("/api/repair-orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_createRepairOrder_returns401() throws Exception {
        mockMvc.perform(post("/api/repair-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. Denied Roles (CUSTOMER & TECHNICIAN: 403)
    // ==========================================

    @Test
    void customer_getRepairOrders_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/repair-orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void technician_getRepairOrders_returns403() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(get("/api/repair-orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 3. ROLE_MANAGER: Create, View, Update, Status
    // ==========================================

    @Test
    void branchManager_createRepairOrder_returns201() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        CreateRepairOrderRequest req = new CreateRepairOrderRequest(501, "Kiểm tra");
        when(repairOrderService.createRepairOrder(any(CreateRepairOrderRequest.class)))
                .thenReturn(sampleOrder(601, 501, 1));

        mockMvc.perform(post("/api/repair-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maPhieuSuaChua").value(601));
    }

    @Test
    void branchManager_createRepairOrder_crossBranch_returns403() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(repairOrderService.createRepairOrder(any(CreateRepairOrderRequest.class)))
                .thenThrow(new AccessDeniedException("Forbidden: Bạn không có quyền tạo phiếu sửa chữa thuộc chi nhánh khác"));

        CreateRepairOrderRequest req = new CreateRepairOrderRequest(502, "Kiểm tra");

        mockMvc.perform(post("/api/repair-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void branchManager_getRepairOrders_returns200() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(repairOrderService.getRepairOrders()).thenReturn(List.of(sampleOrder(601, 501, 1)));

        mockMvc.perform(get("/api/repair-orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void branchManager_updateRepairOrder_returns200() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        UpdateRepairOrderRequest req = new UpdateRepairOrderRequest(null, null, "Ghi chú mới");
        when(repairOrderService.updateRepairOrder(eq(601), any(UpdateRepairOrderRequest.class)))
                .thenReturn(sampleOrder(601, 501, 1));

        mockMvc.perform(put("/api/repair-orders/601")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void branchManager_updateStatus_returns200() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        UpdateRepairOrderStatusRequest req = new UpdateRepairOrderStatusRequest("DANG_SUA");
        when(repairOrderService.updateStatus(eq(601), any(UpdateRepairOrderStatusRequest.class)))
                .thenReturn(sampleOrder(601, 501, 1));

        mockMvc.perform(patch("/api/repair-orders/601/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==========================================
    // 4. ROLE_FRONT_DESK: Get list & detail
    // ==========================================

    @Test
    void receptionist_getRepairOrders_returns200() throws Exception {
        NguoiDung rec = mockUser(3, "receptionist");
        stubUser(rec, "ROLE_FRONT_DESK");
        String token = jwtService.generateToken("receptionist", List.of("ROLE_FRONT_DESK"));

        when(repairOrderService.getRepairOrders()).thenReturn(List.of(sampleOrder(601, 501, 1)));

        mockMvc.perform(get("/api/repair-orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==========================================
    // 5. ROLE_ADMIN: Global Access
    // ==========================================

    @Test
    void admin_getRepairOrders_returns200() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        when(repairOrderService.getRepairOrders()).thenReturn(List.of(
                sampleOrder(601, 501, 1),
                sampleOrder(602, 502, 2)
        ));

        mockMvc.perform(get("/api/repair-orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void admin_getRepairOrderById_notFound_returns404() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        when(repairOrderService.getRepairOrderById(999))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: 999"));

        mockMvc.perform(get("/api/repair-orders/999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
