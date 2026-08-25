package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.*;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.TechnicianExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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
class TechnicianExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TechnicianExecutionService technicianExecutionService;

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

    private RepairOrderResponse sampleOrder(Integer id) {
        RepairOrderResponse res = new RepairOrderResponse();
        res.setMaPhieuSuaChua(id);
        res.setMaTiepNhan(501);
        res.setMaChiNhanh(1);
        res.setTenChiNhanh("Chi Nhánh 1");
        res.setThoiGianBatDau(LocalDateTime.now());
        res.setTrangThai("DANG_SUA");
        res.setGhiChu("Ghi chú sửa chữa");
        return res;
    }

    private RepairProgressResponse sampleProgress(Integer id, Integer orderId) {
        return new RepairProgressResponse(
                id, orderId, 100, "Nguyễn Văn Kỹ Thuật",
                "DANG_SUA", 40, "Đang tiến hành", LocalDateTime.now()
        );
    }

    private RepairItemResponse sampleItem(Integer id, Integer orderId) {
        return new RepairItemResponse(
                id, orderId, 10, "Thay dầu",
                1, "Bảo Dưỡng",
                1, new BigDecimal("150000.00"), new BigDecimal("150000.00"), "DANG_SUA"
        );
    }

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_getMyRepairOrders_returns401() throws Exception {
        mockMvc.perform(get("/api/technician/repair-orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_updateProgress_returns401() throws Exception {
        mockMvc.perform(patch("/api/technician/repair-orders/601/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. Denied Roles (CUSTOMER, RECEPTIONIST: 403)
    // ==========================================

    @Test
    void customer_getMyRepairOrders_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/technician/repair-orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void receptionist_updateProgress_returns403() throws Exception {
        NguoiDung rec = mockUser(3, "receptionist");
        stubUser(rec, "ROLE_FRONT_DESK");
        String token = jwtService.generateToken("receptionist", List.of("ROLE_FRONT_DESK"));

        UpdateRepairProgressRequest req = new UpdateRepairProgressRequest("DANG_SUA", 30, "Lễ tân cố sửa");

        mockMvc.perform(patch("/api/technician/repair-orders/601/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 3. ROLE_TECHNICIAN: Access assigned orders
    // ==========================================

    @Test
    void technician_getMyRepairOrders_returns200() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        when(technicianExecutionService.getMyRepairOrders()).thenReturn(List.of(sampleOrder(601)));

        mockMvc.perform(get("/api/technician/repair-orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].maPhieuSuaChua").value(601));
    }

    @Test
    void technician_getRepairOrderDetail_assigned_returns200() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        when(technicianExecutionService.getRepairOrderDetail(601)).thenReturn(sampleOrder(601));

        mockMvc.perform(get("/api/technician/repair-orders/601").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maPhieuSuaChua").value(601));
    }

    @Test
    void technician_getRepairOrderDetail_unassigned_returns403() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        when(technicianExecutionService.getRepairOrderDetail(602))
                .thenThrow(new AccessDeniedException("Forbidden: Bạn không được phân công phụ trách phiếu sửa chữa này"));

        mockMvc.perform(get("/api/technician/repair-orders/602").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void technician_updateProgress_returns200() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        UpdateRepairProgressRequest req = new UpdateRepairProgressRequest("DANG_SUA", 40, "Đang xử lý");
        when(technicianExecutionService.updateProgress(eq(601), any(UpdateRepairProgressRequest.class)))
                .thenReturn(sampleProgress(901, 601));

        mockMvc.perform(patch("/api/technician/repair-orders/601/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maTienDo").value(901))
                .andExpect(jsonPath("$.data.phanTramHoanThanh").value(40));
    }

    @Test
    void technician_updateItemStatus_returns200() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        UpdateServiceItemStatusRequest req = new UpdateServiceItemStatusRequest("DANG_SUA");
        when(technicianExecutionService.updateItemStatus(eq(601), eq(701), any(UpdateServiceItemStatusRequest.class)))
                .thenReturn(sampleItem(701, 601));

        mockMvc.perform(patch("/api/technician/repair-orders/601/items/701")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maChiTiet").value(701));
    }

    @Test
    void technician_getProgressHistory_returns200() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        when(technicianExecutionService.getProgressHistory(601)).thenReturn(List.of(sampleProgress(901, 601)));

        mockMvc.perform(get("/api/technician/repair-orders/601/progress-history").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }
}
