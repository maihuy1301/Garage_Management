package com.garage.controller;

import com.garage.dto.RoleResponse;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private RoleService roleService;

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

    private List<RoleResponse> sampleRoles() {
        return List.of(
                new RoleResponse(1, "ROLE_ADMIN", "Quản trị viên toàn hệ thống"),
                new RoleResponse(2, "ROLE_MANAGER", "Quản lý chi nhánh"),
                new RoleResponse(3, "ROLE_FRONT_DESK", "Nhân viên tiếp nhận / lễ tân"),
                new RoleResponse(4, "ROLE_TECHNICIAN", "Kỹ thuật viên sửa chữa"),
                new RoleResponse(5, "ROLE_CUSTOMER", "Khách hàng")
        );
    }

    @Test
    void getAllRoles_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllRoles_Customer_Returns403() throws Exception {
        NguoiDung customer = createMockUser(5, "customer");
        VaiTro cusRole = createMockRole(5, "ROLE_CUSTOMER");
        stubAuthenticatedUser(customer, cusRole);

        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllRoles_Technician_Returns403() throws Exception {
        NguoiDung tech = createMockUser(4, "technician");
        VaiTro techRole = createMockRole(4, "ROLE_TECHNICIAN");
        stubAuthenticatedUser(tech, techRole);

        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllRoles_FrontDesk_Returns403() throws Exception {
        NguoiDung fd = createMockUser(3, "frontdesk");
        VaiTro fdRole = createMockRole(3, "ROLE_FRONT_DESK");
        stubAuthenticatedUser(fd, fdRole);

        String token = jwtService.generateToken("frontdesk", List.of("ROLE_FRONT_DESK"));

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllRoles_Admin_Returns200With5Roles() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        when(roleService.getAllRoles()).thenReturn(sampleRoles());

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy danh sách vai trò thành công"))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].tenVaiTro").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.data[1].tenVaiTro").value("ROLE_MANAGER"))
                .andExpect(jsonPath("$.data[2].tenVaiTro").value("ROLE_FRONT_DESK"))
                .andExpect(jsonPath("$.data[3].tenVaiTro").value("ROLE_TECHNICIAN"))
                .andExpect(jsonPath("$.data[4].tenVaiTro").value("ROLE_CUSTOMER"));
    }

    @Test
    void getAllRoles_Manager_Returns200With5Roles() throws Exception {
        NguoiDung manager = createMockUser(2, "manager");
        VaiTro mgrRole = createMockRole(2, "ROLE_MANAGER");
        stubAuthenticatedUser(manager, mgrRole);

        when(roleService.getAllRoles()).thenReturn(sampleRoles());

        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].tenVaiTro").value("ROLE_ADMIN"));
    }
}
