package com.garage.controller;

import com.garage.dto.InventoryResponse;
import com.garage.dto.PartResponse;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PartAndInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

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
        u.setMaPinHash("$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO");
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
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/parts").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void branchManager_getParts_returns200() throws Exception {
        NguoiDung bm = mockUser(2, "manager");
        stubUser(bm, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(inventoryService.getAllParts()).thenReturn(List.of(samplePart(1)));

        mockMvc.perform(get("/api/parts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==========================================
    // 2. INVENTORY TESTS
    // ==========================================

    @Test
    void unauthenticated_getInventory_returns401() throws Exception {
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customer_getInventory_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/inventory").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void branchManager_getOwnBranchInventory_returns200() throws Exception {
        NguoiDung bm = mockUser(2, "manager");
        stubUser(bm, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(inventoryService.getInventoryByBranch(1)).thenReturn(List.of(sampleInventory(1, 10)));

        mockMvc.perform(get("/api/inventory?branchId=1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].soLuongTon").value(10));
    }

    @Test
    void branchManager_getOtherBranchInventory_returns403() throws Exception {
        NguoiDung bm = mockUser(2, "manager");
        stubUser(bm, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(inventoryService.getInventoryByBranch(2))
                .thenThrow(new AccessDeniedException("Forbidden: Bạn không có quyền truy cập tồn kho của chi nhánh khác"));

        mockMvc.perform(get("/api/inventory?branchId=2").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
