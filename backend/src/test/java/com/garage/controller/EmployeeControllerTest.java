package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.*;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

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
    void getAllEmployees_SystemAdmin_Returns200() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        EmployeeResponse emp = new EmployeeResponse(
                1, "Quản lý", LocalDate.now(), true, 2, "manager", "Manager FullName", "manager@garage.com", "0900000002", List.of("ROLE_MANAGER"), 1, "Chi Nhánh 1"
        );
        when(employeeService.getAllEmployees()).thenReturn(List.of(emp));

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maNhanVien").value(1));
    }

    @Test
    void getAllEmployees_BranchManager_Returns200() throws Exception {
        NguoiDung manager = createMockUser(2, "manager");
        VaiTro mgrRole = createMockRole(2, "ROLE_MANAGER");
        stubAuthenticatedUser(manager, mgrRole);

        EmployeeResponse emp = new EmployeeResponse(
                1, "Quản lý", LocalDate.now(), true, 2, "manager", "Manager FullName", "manager@garage.com", "0900000002", List.of("ROLE_MANAGER"), 1, "Chi Nhánh 1"
        );
        when(employeeService.getAllEmployees()).thenReturn(List.of(emp));

        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maNhanVien").value(1));
    }

    @Test
    void getAllEmployees_Receptionist_Returns403() throws Exception {
        NguoiDung receptionist = createMockUser(3, "receptionist");
        VaiTro recRole = createMockRole(3, "ROLE_FRONT_DESK");
        stubAuthenticatedUser(receptionist, recRole);

        String token = jwtService.generateToken("receptionist", List.of("ROLE_FRONT_DESK"));

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllEmployees_Technician_Returns403() throws Exception {
        NguoiDung technician = createMockUser(4, "technician");
        VaiTro techRole = createMockRole(4, "ROLE_TECHNICIAN");
        stubAuthenticatedUser(technician, techRole);

        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllEmployees_Customer_Returns403() throws Exception {
        NguoiDung customer = createMockUser(5, "customer");
        VaiTro cusRole = createMockRole(5, "ROLE_CUSTOMER");
        stubAuthenticatedUser(customer, cusRole);

        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllEmployees_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createEmployee_Returns201() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        CreateEmployeeRequest req = new CreateEmployeeRequest(4, 1, "Thợ máy", LocalDate.now());
        EmployeeResponse created = new EmployeeResponse(
                2, "Thợ máy", LocalDate.now(), true, 4, "technician", "Tech Name", "tech@garage.com", "0900000004", List.of("ROLE_TECHNICIAN"), 1, "Chi Nhánh 1"
        );
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(created);

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maNhanVien").value(2));
    }

    @Test
    void updateEmployee_Returns200() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        UpdateEmployeeRequest req = new UpdateEmployeeRequest("Thợ máy chính", LocalDate.now(), 1);
        EmployeeResponse updated = new EmployeeResponse(
                2, "Thợ máy chính", LocalDate.now(), true, 4, "technician", "Tech Name", "tech@garage.com", "0900000004", List.of("ROLE_TECHNICIAN"), 1, "Chi Nhánh 1"
        );
        when(employeeService.updateEmployee(eq(2), any(UpdateEmployeeRequest.class))).thenReturn(updated);

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(put("/api/employees/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chucVu").value("Thợ máy chính"));
    }

    @Test
    void updateEmployeeStatus_Returns200() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        UpdateEmployeeStatusRequest req = new UpdateEmployeeStatusRequest(false);
        EmployeeResponse updated = new EmployeeResponse(
                2, "Thợ máy", LocalDate.now(), false, 4, "technician", "Tech Name", "tech@garage.com", "0900000004", List.of("ROLE_TECHNICIAN"), 1, "Chi Nhánh 1"
        );
        when(employeeService.updateEmployeeStatus(eq(2), any(UpdateEmployeeStatusRequest.class))).thenReturn(updated);

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(patch("/api/employees/2/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trangThai").value(false));
    }
}
