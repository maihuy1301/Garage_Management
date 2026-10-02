package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.*;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.BranchAuthorizationService;
import com.garage.security.JwtService;
import com.garage.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
class PartAndInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private BranchAuthorizationService branchAuthorizationService;

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

    private PartResponse samplePart(Integer id) {
        return new PartResponse(
                id, "PT00" + id, "Lọc dầu", "Cái",
                new BigDecimal("80000.00"), new BigDecimal("150000.00"), true
        );
    }

    private InventoryResponse sampleInventory(Integer branchId, Integer partId) {
        return new InventoryResponse(
                branchId, "Chi Nhánh " + branchId,
                partId, "PT00" + partId, "Lọc dầu", "Cái",
                new BigDecimal("150000.00"), 10, 2
        );
    }

    // ==========================================
    // 1. PARTS CATALOG TESTS
    // ==========================================

    @Test
    void unauthenticated_getParts_returns401() throws Exception {
        mockMvc.perform(get("/api/parts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customer_getParts_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken(cust.getTenDangNhap(), List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/parts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void receptionist_getParts_returns200() throws Exception {
        NguoiDung staff = mockUser(2, "frontdesk");
        stubUser(staff, "ROLE_FRONT_DESK");
        String token = jwtService.generateToken(staff.getTenDangNhap(), List.of("ROLE_FRONT_DESK"));

        when(inventoryService.getAllParts(false)).thenReturn(List.of(samplePart(1)));

        mockMvc.perform(get("/api/parts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tenPhuTung").value("Lọc dầu"));
    }

    @Test
    void admin_createPart_returns201() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken(admin.getTenDangNhap(), List.of("ROLE_ADMIN"));

        CreatePartRequest req = new CreatePartRequest();
        req.setMaPhuTungCode("PT-NEW");
        req.setTenPhuTung("Lọc gió điều hòa");
        req.setDonViTinh("Cái");
        req.setGiaBan(new BigDecimal("250000"));

        when(inventoryService.createPart(any(CreatePartRequest.class))).thenReturn(samplePart(10));

        mockMvc.perform(post("/api/parts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void manager_createPart_returns403() throws Exception {
        NguoiDung manager = mockUser(2, "manager");
        stubUser(manager, "ROLE_MANAGER");
        String token = jwtService.generateToken(manager.getTenDangNhap(), List.of("ROLE_MANAGER"));

        CreatePartRequest req = new CreatePartRequest();
        req.setMaPhuTungCode("PT-NEW");
        req.setTenPhuTung("Lọc gió điều hòa");

        mockMvc.perform(post("/api/parts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void manager_updatePartStatus_returns200() throws Exception {
        NguoiDung manager = mockUser(2, "manager");
        stubUser(manager, "ROLE_MANAGER");
        String token = jwtService.generateToken(manager.getTenDangNhap(), List.of("ROLE_MANAGER"));

        UpdatePartStatusRequest req = new UpdatePartStatusRequest(false);
        when(inventoryService.updatePartStatus(eq(1), any(UpdatePartStatusRequest.class))).thenReturn(samplePart(1));

        mockMvc.perform(patch("/api/parts/1/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ==========================================
    // 2. INVENTORY & STOCK IMPORT TESTS
    // ==========================================

    @Test
    void manager_importStock_returns201() throws Exception {
        NguoiDung manager = mockUser(2, "manager");
        stubUser(manager, "ROLE_MANAGER");
        String token = jwtService.generateToken(manager.getTenDangNhap(), List.of("ROLE_MANAGER"));

        StockImportRequest req = new StockImportRequest();
        req.setMaPhuTung(1);
        req.setSoLuong(20);
        req.setGhiChu("Nhập lô 1");

        when(inventoryService.importStock(any(StockImportRequest.class))).thenReturn(sampleInventory(1, 1));

        mockMvc.perform(post("/api/inventory/import")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void manager_getTransactions_returns200() throws Exception {
        NguoiDung manager = mockUser(2, "manager");
        stubUser(manager, "ROLE_MANAGER");
        String token = jwtService.generateToken(manager.getTenDangNhap(), List.of("ROLE_MANAGER"));

        StockTransactionResponse tx = new StockTransactionResponse(
                1, 1, "Chi Nhánh 1", 1, "PT001", "Lọc dầu", "Cái", "NHAP", 20, null, "Nhập kho", LocalDateTime.now()
        );
        when(inventoryService.getStockTransactions(any())).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/inventory/transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].loaiGiaoDich").value("NHAP"));
    }
}
