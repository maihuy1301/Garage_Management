package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.BranchResponse;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.BranchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private BranchService branchService;

    @MockBean
    private NguoiDungRepository nguoiDungRepository;

    @MockBean
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    private NguoiDung createMockUser(Integer id, String username) {
        NguoiDung user = new NguoiDung();
        user.setMaNguoiDung(id);
        user.setTenDangNhap(username);
        user.setHoTen(username + " FullName");
        user.setEmail(username + "@garage.com");
        user.setMatKhauHash("$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO");
        user.setTrangThai(true);
        return user;
    }

    private VaiTro createMockRole(Integer id, String roleName) {
        VaiTro role = new VaiTro();
        role.setMaVaiTro(id);
        role.setTenVaiTro(roleName);
        return role;
    }

    private void stubAuthenticatedUser(NguoiDung user, VaiTro role) {
        when(nguoiDungRepository.findByTenDangNhapOrEmail(user.getTenDangNhap(), user.getTenDangNhap()))
                .thenReturn(Optional.of(user));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()))
                .thenReturn(List.of(new NguoiDungVaiTro(user, role)));
    }

    @Test
    void getAllBranches_SystemAdmin_Returns200() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        BranchResponse b1 = new BranchResponse(1, "Chi Nhánh 1", "Địa chỉ 1", "0900000001", "cn1@garage.com", true, LocalDateTime.now());
        BranchResponse b2 = new BranchResponse(2, "Chi Nhánh 2", "Địa chỉ 2", "0900000002", "cn2@garage.com", true, LocalDateTime.now());
        when(branchService.getAllBranches()).thenReturn(List.of(b1, b2));

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/branches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maChiNhanh").value(1))
                .andExpect(jsonPath("$.data[1].maChiNhanh").value(2));
    }

    @Test
    void getAllBranches_BranchManager_Returns200() throws Exception {
        NguoiDung manager = createMockUser(2, "manager");
        VaiTro mgrRole = createMockRole(2, "ROLE_MANAGER");
        stubAuthenticatedUser(manager, mgrRole);

        BranchResponse b1 = new BranchResponse(1, "Chi Nhánh 1", "Địa chỉ 1", "0900000001", "cn1@garage.com", true, LocalDateTime.now());
        when(branchService.getAllBranches()).thenReturn(List.of(b1));

        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/api/branches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maChiNhanh").value(1));
    }

    @Test
    void getBranchById_Customer_Returns200() throws Exception {
        NguoiDung customer = createMockUser(5, "customer");
        VaiTro cusRole = createMockRole(5, "ROLE_CUSTOMER");
        stubAuthenticatedUser(customer, cusRole);

        BranchResponse b1 = new BranchResponse(1, "Chi Nhánh 1", "Địa chỉ 1", "0900000001", "cn1@garage.com", true, LocalDateTime.now());
        when(branchService.getBranchById(1)).thenReturn(b1);

        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/branches/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tenChiNhanh").value("Chi Nhánh 1"));
    }

    @Test
    void getAllBranches_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/branches"))
                .andExpect(status().isUnauthorized());
    }
}
