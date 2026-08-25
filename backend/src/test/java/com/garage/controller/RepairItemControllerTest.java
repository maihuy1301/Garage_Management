package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.CreateRepairItemRequest;
import com.garage.dto.RepairItemResponse;
import com.garage.dto.UpdateRepairItemRequest;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.RepairItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
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
class RepairItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepairItemService repairItemService;

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

    private RepairItemResponse sampleItem(Integer id, Integer orderId, Integer serviceId) {
        return new RepairItemResponse(
                id, orderId, serviceId, "Thay dầu",
                1, "Bảo Dưỡng",
                2, new BigDecimal("150000.00"), new BigDecimal("300000.00"), "CHO_XU_LY"
        );
    }

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_getRepairItems_returns401() throws Exception {
        mockMvc.perform(get("/api/repair-orders/601/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_addRepairItem_returns401() throws Exception {
        mockMvc.perform(post("/api/repair-orders/601/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. Denied Roles (CUSTOMER & TECHNICIAN: 403)
    // ==========================================

    @Test
    void customer_getRepairItems_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/repair-orders/601/items").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void technician_addRepairItem_returns403() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        CreateRepairItemRequest req = new CreateRepairItemRequest(10, 1);

        mockMvc.perform(post("/api/repair-orders/601/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 3. ROLE_MANAGER: Add, View, Update, Delete
    // ==========================================

    @Test
    void branchManager_addRepairItem_ownBranch_returns201() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        CreateRepairItemRequest req = new CreateRepairItemRequest(10, 2);
        when(repairItemService.addRepairItem(eq(601), any(CreateRepairItemRequest.class)))
                .thenReturn(sampleItem(701, 601, 10));

        mockMvc.perform(post("/api/repair-orders/601/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maChiTiet").value(701))
                .andExpect(jsonPath("$.data.thanhTien").value(300000.00));
    }

    @Test
    void branchManager_addRepairItem_crossBranch_returns403() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(repairItemService.addRepairItem(eq(602), any(CreateRepairItemRequest.class)))
                .thenThrow(new AccessDeniedException("Forbidden: Bạn không có quyền truy cập phiếu sửa chữa của chi nhánh khác"));

        CreateRepairItemRequest req = new CreateRepairItemRequest(10, 1);

        mockMvc.perform(post("/api/repair-orders/602/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void branchManager_getRepairItems_returns200() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(repairItemService.getRepairItems(601)).thenReturn(List.of(sampleItem(701, 601, 10)));

        mockMvc.perform(get("/api/repair-orders/601/items").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void branchManager_updateRepairItem_returns200() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        UpdateRepairItemRequest req = new UpdateRepairItemRequest(3, new BigDecimal("160000.00"));
        when(repairItemService.updateRepairItem(eq(601), eq(701), any(UpdateRepairItemRequest.class)))
                .thenReturn(sampleItem(701, 601, 10));

        mockMvc.perform(put("/api/repair-orders/601/items/701")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void branchManager_deleteRepairItem_returns200() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        mockMvc.perform(delete("/api/repair-orders/601/items/701").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==========================================
    // 4. ROLE_FRONT_DESK & ROLE_ADMIN
    // ==========================================

    @Test
    void receptionist_getRepairItems_returns200() throws Exception {
        NguoiDung rec = mockUser(3, "receptionist");
        stubUser(rec, "ROLE_FRONT_DESK");
        String token = jwtService.generateToken("receptionist", List.of("ROLE_FRONT_DESK"));

        when(repairItemService.getRepairItems(601)).thenReturn(List.of(sampleItem(701, 601, 10)));

        mockMvc.perform(get("/api/repair-orders/601/items").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void admin_addRepairItem_returns201() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        CreateRepairItemRequest req = new CreateRepairItemRequest(10, 1);
        when(repairItemService.addRepairItem(eq(602), any(CreateRepairItemRequest.class)))
                .thenReturn(sampleItem(702, 602, 10));

        mockMvc.perform(post("/api/repair-orders/602/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.maChiTiet").value(702));
    }
}
