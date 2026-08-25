package com.garage.security;

import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RbacSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private NguoiDungRepository nguoiDungRepository;

    @MockBean
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    /**
     * Creates a mock NguoiDung with trangThai=true.
     */
    private NguoiDung createMockUser(Integer id, String username) {
        NguoiDung user = new NguoiDung();
        user.setMaNguoiDung(id);
        user.setTenDangNhap(username);
        user.setHoTen(username + " FullName");
        user.setMatKhauHash("$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO");
        user.setTrangThai(true);
        return user;
    }

    /**
     * Creates a mock VaiTro with both maVaiTro and tenVaiTro set,
     * preventing NPE in NguoiDungVaiTro constructor (which calls vaiTro.getMaVaiTro()).
     */
    private VaiTro createMockRole(Integer id, String roleName) {
        VaiTro role = new VaiTro();
        role.setMaVaiTro(id);
        role.setTenVaiTro(roleName);
        return role;
    }

    /**
     * Links a user-role pair. Uses direct field injection to avoid NPE
     * from NguoiDungVaiTroId if IDs are not yet set on the entity.
     */
    private NguoiDungVaiTro createLink(NguoiDung user, VaiTro role) {
        return new NguoiDungVaiTro(user, role);
    }

    @Test
    void testAdminRole_AccessSuccessAndDenied() throws Exception {
        NguoiDung admin = createMockUser(1, "admin");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        NguoiDungVaiTro link = createLink(admin, adminRole);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("admin", "admin")).thenReturn(Optional.of(admin));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(1)).thenReturn(List.of(link));

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        // Admin endpoint -> 200 OK
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("ADMIN_RESOURCE"));

        // Manager endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/test/manager")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Forbidden: Bạn không có quyền truy cập tài nguyên này."));
    }

    @Test
    void testManagerRole_AccessSuccessAndDenied() throws Exception {
        NguoiDung manager = createMockUser(2, "manager");
        VaiTro mgrRole = createMockRole(2, "ROLE_MANAGER");
        NguoiDungVaiTro link = createLink(manager, mgrRole);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("manager", "manager")).thenReturn(Optional.of(manager));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(2)).thenReturn(List.of(link));

        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/api/test/manager")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("MANAGER_RESOURCE"));

        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testFrontDeskRole_AccessSuccessAndDenied() throws Exception {
        NguoiDung rec = createMockUser(3, "frontdesk");
        VaiTro recRole = createMockRole(3, "ROLE_FRONT_DESK");
        NguoiDungVaiTro link = createLink(rec, recRole);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("frontdesk", "frontdesk")).thenReturn(Optional.of(rec));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(3)).thenReturn(List.of(link));

        String token = jwtService.generateToken("frontdesk", List.of("ROLE_FRONT_DESK"));

        mockMvc.perform(get("/api/test/receptionist")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("FRONT_DESK_RESOURCE"));

        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testTechnicianRole_AccessSuccessAndDenied() throws Exception {
        NguoiDung tech = createMockUser(4, "technician");
        VaiTro techRole = createMockRole(4, "ROLE_TECHNICIAN");
        NguoiDungVaiTro link = createLink(tech, techRole);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("technician", "technician")).thenReturn(Optional.of(tech));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(4)).thenReturn(List.of(link));

        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(get("/api/test/technician")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("TECHNICIAN_RESOURCE"));

        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCustomerRole_AccessSuccessAndDenied() throws Exception {
        NguoiDung cus = createMockUser(5, "customer");
        VaiTro cusRole = createMockRole(5, "ROLE_CUSTOMER");
        NguoiDungVaiTro link = createLink(cus, cusRole);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("customer", "customer")).thenReturn(Optional.of(cus));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(5)).thenReturn(List.of(link));

        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/test/customer")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("CUSTOMER_RESOURCE"));

        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testMultiRoleUser_AccessMultipleEndpoints() throws Exception {
        NguoiDung multiUser = createMockUser(6, "multi_user");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        VaiTro cusRole = createMockRole(5, "ROLE_CUSTOMER");
        NguoiDungVaiTro link1 = createLink(multiUser, adminRole);
        NguoiDungVaiTro link2 = createLink(multiUser, cusRole);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("multi_user", "multi_user")).thenReturn(Optional.of(multiUser));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(6)).thenReturn(List.of(link1, link2));

        String token = jwtService.generateToken("multi_user", List.of("ROLE_ADMIN", "ROLE_CUSTOMER"));

        // Can access admin endpoint
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Can access customer endpoint
        mockMvc.perform(get("/api/test/customer")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Cannot access manager endpoint (not in roles)
        mockMvc.perform(get("/api/test/manager")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedRequest_Returns401() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized: Bạn cần đăng nhập để truy cập tài nguyên này."));
    }
}
