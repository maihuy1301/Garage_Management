package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.*;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

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
    void getCurrentCustomerProfile_Customer_Returns200() throws Exception {
        NguoiDung customer = createMockUser(5, "customer");
        VaiTro cusRole = createMockRole(5, "ROLE_CUSTOMER");
        stubAuthenticatedUser(customer, cusRole);

        CustomerResponse res = new CustomerResponse(
                1, "KH001", "123 Đường Số 1", LocalDate.of(1990, 1, 1),
                5, "customer", "Phạm Văn Khách Hàng", "customer@garage.com", "0900000005",
                null, true, LocalDateTime.now(), List.of("ROLE_CUSTOMER")
        );
        when(customerService.getCurrentCustomerProfile()).thenReturn(res);

        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/customers/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maKhachHangCode").value("KH001"));
    }

    @Test
    void getAllCustomers_SystemAdmin_Returns200() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        CustomerResponse res = new CustomerResponse(
                1, "KH001", "123 Đường Số 1", LocalDate.of(1990, 1, 1),
                5, "customer", "Phạm Văn Khách Hàng", "customer@garage.com", "0900000005",
                null, true, LocalDateTime.now(), List.of("ROLE_CUSTOMER")
        );
        when(customerService.getAllCustomers()).thenReturn(List.of(res));

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maKhachHangCode").value("KH001"));
    }

    @Test
    void getAllCustomers_Customer_Returns403() throws Exception {
        NguoiDung customer = createMockUser(5, "customer");
        VaiTro cusRole = createMockRole(5, "ROLE_CUSTOMER");
        stubAuthenticatedUser(customer, cusRole);

        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllCustomers_Technician_Returns403() throws Exception {
        NguoiDung tech = createMockUser(4, "technician");
        VaiTro techRole = createMockRole(4, "ROLE_TECHNICIAN");
        stubAuthenticatedUser(tech, techRole);

        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentCustomerProfile_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/customers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCustomer_SystemAdmin_Returns201() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        CreateCustomerRequest req = new CreateCustomerRequest("KH002", 6, "456 Đường Số 2", LocalDate.of(1992, 2, 2));
        CustomerResponse res = new CustomerResponse(
                2, "KH002", "456 Đường Số 2", LocalDate.of(1992, 2, 2),
                6, "customer2", "Khách Hàng 2", "customer2@garage.com", "0900000006",
                null, true, LocalDateTime.now(), List.of("ROLE_CUSTOMER")
        );
        when(customerService.createCustomer(any(CreateCustomerRequest.class))).thenReturn(res);

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maKhachHangCode").value("KH002"));
    }
}
