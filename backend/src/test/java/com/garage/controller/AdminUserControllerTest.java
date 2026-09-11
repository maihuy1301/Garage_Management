package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.*;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

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
    void getAllUsers_SystemAdmin_Returns200() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        UserResponse userResponse = new UserResponse(
                1, "admin", "Admin FullName", "admin@garage.com", "0900000001", null, true, LocalDateTime.now(), List.of("ROLE_ADMIN")
        );
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].tenDangNhap").value("admin"))
                .andExpect(jsonPath("$.data[0].matKhauHash").doesNotExist())
                .andExpect(jsonPath("$.data[0].matKhau").doesNotExist());
    }

    @Test
    void getAllUsers_BranchManager_Returns200() throws Exception {
        NguoiDung manager = createMockUser(2, "manager");
        VaiTro mgrRole = createMockRole(2, "ROLE_MANAGER");
        stubAuthenticatedUser(manager, mgrRole);

        UserResponse userResponse = new UserResponse(
                2, "manager", "Manager FullName", "manager@garage.com", "0900000002", null, true, LocalDateTime.now(), List.of("ROLE_MANAGER")
        );
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getAllUsers_Customer_Returns403() throws Exception {
        NguoiDung customer = createMockUser(5, "customer");
        VaiTro cusRole = createMockRole(5, "ROLE_CUSTOMER");
        stubAuthenticatedUser(customer, cusRole);

        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createUser_SystemAdmin_Returns201() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        UserResponse created = new UserResponse(
                10, "newtech", "Kỹ Thuật Mới", "newtech@garage.com", "0900000099", null, true, LocalDateTime.now(), List.of("ROLE_TECHNICIAN")
        );
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(created);

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        CreateUserRequest req = new CreateUserRequest("newtech", "Password123@", "Kỹ Thuật Mới", "newtech@garage.com", "0900000099", null, List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tenDangNhap").value("newtech"))
                .andExpect(jsonPath("$.data.matKhau").doesNotExist())
                .andExpect(jsonPath("$.data.matKhauHash").doesNotExist());
    }

    @Test
    void createUser_DuplicateUsername_Returns409() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new DuplicateResourceException("Tên đăng nhập 'admin' đã tồn tại"));

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        CreateUserRequest req = new CreateUserRequest("admin", "Password123@", "Admin", "admin@garage.com", null, null, null);

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tên đăng nhập 'admin' đã tồn tại"));
    }

    @Test
    void getUserById_Found_Returns200() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        UserResponse userResponse = new UserResponse(
                2, "manager", "Manager FullName", "manager@garage.com", "0900000002", null, true, LocalDateTime.now(), List.of("ROLE_MANAGER")
        );
        when(userService.getUserById(2)).thenReturn(userResponse);

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/admin/users/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenDangNhap").value("manager"));
    }

    @Test
    void getUserById_NotFound_Returns404() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        when(userService.getUserById(999)).thenThrow(new ResourceNotFoundException("Không tìm thấy người dùng với ID: 999"));

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/admin/users/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy người dùng với ID: 999"));
    }

    @Test
    void updateUserStatus_Returns200() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        stubAuthenticatedUser(admin, adminRole);

        UserResponse updated = new UserResponse(
                2, "manager", "Manager FullName", "manager@garage.com", "0900000002", null, false, LocalDateTime.now(), List.of("ROLE_MANAGER")
        );
        when(userService.updateUserStatus(eq(2), any(UpdateUserStatusRequest.class))).thenReturn(updated);

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        UpdateUserStatusRequest req = new UpdateUserStatusRequest(false);

        mockMvc.perform(patch("/api/admin/users/2/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trangThai").value(false));
    }
}
