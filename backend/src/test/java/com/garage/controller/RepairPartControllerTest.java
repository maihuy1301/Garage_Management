package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.CreateRepairPartRequest;
import com.garage.dto.RepairPartResponse;
import com.garage.dto.UpdateRepairPartRequest;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.RepairPartService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RepairPartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepairPartService repairPartService;

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

    private RepairPartResponse sampleRepairPart(Integer id, Integer orderId) {
        return new RepairPartResponse(
                id, orderId, 10, "PT001", "Lọc dầu", "Cái",
                2, new BigDecimal("150000.00"), new BigDecimal("300000.00")
        );
    }

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_getRepairParts_returns401() throws Exception {
        mockMvc.perform(get("/api/repair-orders/601/parts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_addRepairPart_returns401() throws Exception {
        mockMvc.perform(post("/api/repair-orders/601/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. CUSTOMER (403)
    // ==========================================

    @Test
    void customer_getRepairParts_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/repair-orders/601/parts").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 3. ROLE_ADMIN / ROLE_MANAGER (200 / 201)
    // ==========================================

    @Test
    void branchManager_getRepairParts_returns200() throws Exception {
        NguoiDung bm = mockUser(2, "manager");
        stubUser(bm, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(repairPartService.getRepairOrderParts(601)).thenReturn(List.of(sampleRepairPart(701, 601)));

        mockMvc.perform(get("/api/repair-orders/601/parts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maChiTiet").value(701));
    }

    @Test
    void branchManager_addRepairPart_returns201() throws Exception {
        NguoiDung bm = mockUser(2, "manager");
        stubUser(bm, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        CreateRepairPartRequest req = new CreateRepairPartRequest(10, 2);
        when(repairPartService.addPartToRepairOrder(eq(601), any(CreateRepairPartRequest.class)))
                .thenReturn(sampleRepairPart(701, 601));

        mockMvc.perform(post("/api/repair-orders/601/parts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.soLuong").value(2))
                .andExpect(jsonPath("$.data.thanhTien").value(300000.00));
    }

    @Test
    void branchManager_updateRepairPart_returns200() throws Exception {
        NguoiDung bm = mockUser(2, "manager");
        stubUser(bm, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        UpdateRepairPartRequest req = new UpdateRepairPartRequest(5);
        RepairPartResponse updated = new RepairPartResponse(
                701, 601, 10, "PT001", "Lọc dầu", "Cái",
                5, new BigDecimal("150000.00"), new BigDecimal("750000.00")
        );
        when(repairPartService.updatePartQuantity(eq(601), eq(701), any(UpdateRepairPartRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/repair-orders/601/parts/701")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.soLuong").value(5))
                .andExpect(jsonPath("$.data.thanhTien").value(750000.00));
    }

    @Test
    void branchManager_deleteRepairPart_returns200() throws Exception {
        NguoiDung bm = mockUser(2, "manager");
        stubUser(bm, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        doNothing().when(repairPartService).deletePartFromRepairOrder(601, 701);

        mockMvc.perform(delete("/api/repair-orders/601/parts/701")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
