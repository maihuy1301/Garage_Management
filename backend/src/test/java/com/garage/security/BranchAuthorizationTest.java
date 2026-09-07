package com.garage.security;

import com.garage.entity.ChiNhanh;
import com.garage.entity.NguoiDung;
import com.garage.entity.NhanVien;
import com.garage.entity.VaiTro;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.NhanVienRepository;
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
class BranchAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private NguoiDungRepository nguoiDungRepository;

    @MockBean
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @MockBean
    private NhanVienRepository nhanVienRepository;

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private NguoiDung mockUser(Integer id, String username) {
        NguoiDung u = new NguoiDung();
        u.setMaNguoiDung(id);
        u.setTenDangNhap(username);
        u.setHoTen(username);
        u.setMatKhauHash("$2a$10$...");
        u.setTrangThai(true);
        return u;
    }

    private VaiTro mockRole(Integer id, String name) {
        VaiTro r = new VaiTro();
        r.setMaVaiTro(id);
        r.setTenVaiTro(name);
        return r;
    }

    private ChiNhanh mockBranch(Integer id, String name) {
        ChiNhanh b = new ChiNhanh();
        b.setMaChiNhanh(id);
        b.setTenChiNhanh("Chi Nhánh " + name);
        b.setDiaChi("Địa chỉ " + name);
        b.setTrangThai(true);
        return b;
    }

    private NhanVien mockEmployee(NguoiDung user, ChiNhanh branch) {
        NhanVien nv = new NhanVien();
        nv.setNguoiDung(user);
        nv.setChiNhanh(branch);
        nv.setTrangThai(true);
        return nv;
    }

    private void stubUser(NguoiDung user, VaiTro role, NhanVien employee) {
        NguoiDungVaiTro link = new NguoiDungVaiTro(user, role);
        when(nguoiDungRepository.findByTenDangNhapOrEmail(user.getTenDangNhap(), user.getTenDangNhap()))
                .thenReturn(Optional.of(user));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()))
                .thenReturn(List.of(link));
        if (employee != null) {
            when(nhanVienRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()))
                    .thenReturn(Optional.of(employee));
        } else {
            when(nhanVienRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()))
                    .thenReturn(Optional.empty());
        }
    }

    // ---------------------------------------------------------------
    // ROLE_ADMIN — global access
    // ---------------------------------------------------------------

    @Test
    void admin_CanAccessCN001() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        VaiTro adminRole = mockRole(1, "ROLE_ADMIN");
        stubUser(admin, adminRole, null); // admin has no NhanVien record

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/test/branches/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("1"));
    }

    @Test
    void admin_CanAccessCN002() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        VaiTro adminRole = mockRole(1, "ROLE_ADMIN");
        stubUser(admin, adminRole, null);

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/test/branches/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("2"));
    }

    // ---------------------------------------------------------------
    // ROLE_MANAGER — own branch 200, other branch 403
    // ---------------------------------------------------------------

    @Test
    void branchManager_CN001_CanAccessCN001() throws Exception {
        NguoiDung manager = mockUser(2, "manager");
        VaiTro mgrRole = mockRole(2, "ROLE_MANAGER");
        ChiNhanh cn001 = mockBranch(1, "CN001");
        NhanVien emp = mockEmployee(manager, cn001);
        stubUser(manager, mgrRole, emp);

        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/api/test/branches/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("1"));
    }

    @Test
    void branchManager_CN001_DeniedCN002() throws Exception {
        NguoiDung manager = mockUser(2, "manager");
        VaiTro mgrRole = mockRole(2, "ROLE_MANAGER");
        ChiNhanh cn001 = mockBranch(1, "CN001");
        NhanVien emp = mockEmployee(manager, cn001);
        stubUser(manager, mgrRole, emp);

        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/api/test/branches/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------
    // ROLE_FRONT_DESK — own branch 200, other branch 403
    // ---------------------------------------------------------------

    @Test
    void frontDesk_CN001_CanAccessCN001() throws Exception {
        NguoiDung rec = mockUser(3, "frontdesk");
        VaiTro recRole = mockRole(3, "ROLE_FRONT_DESK");
        ChiNhanh cn001 = mockBranch(1, "CN001");
        NhanVien emp = mockEmployee(rec, cn001);
        stubUser(rec, recRole, emp);

        String token = jwtService.generateToken("frontdesk", List.of("ROLE_FRONT_DESK"));

        mockMvc.perform(get("/api/test/branches/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void frontDesk_CN001_DeniedCN002() throws Exception {
        NguoiDung rec = mockUser(3, "frontdesk");
        VaiTro recRole = mockRole(3, "ROLE_FRONT_DESK");
        ChiNhanh cn001 = mockBranch(1, "CN001");
        NhanVien emp = mockEmployee(rec, cn001);
        stubUser(rec, recRole, emp);

        String token = jwtService.generateToken("frontdesk", List.of("ROLE_FRONT_DESK"));

        mockMvc.perform(get("/api/test/branches/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------
    // ROLE_TECHNICIAN — own branch 200, other branch 403
    // ---------------------------------------------------------------

    @Test
    void technician_CN001_CanAccessCN001() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        VaiTro techRole = mockRole(4, "ROLE_TECHNICIAN");
        ChiNhanh cn001 = mockBranch(1, "CN001");
        NhanVien emp = mockEmployee(tech, cn001);
        stubUser(tech, techRole, emp);

        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(get("/api/test/branches/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void technician_CN001_DeniedCN002() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        VaiTro techRole = mockRole(4, "ROLE_TECHNICIAN");
        ChiNhanh cn001 = mockBranch(1, "CN001");
        NhanVien emp = mockEmployee(tech, cn001);
        stubUser(tech, techRole, emp);

        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(get("/api/test/branches/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------
    // Cross-branch: CN002 manager → CN001 denied
    // ---------------------------------------------------------------

    @Test
    void branchManager_CN002_DeniedCN001() throws Exception {
        NguoiDung mgr2 = mockUser(6, "manager2");
        VaiTro mgrRole = mockRole(2, "ROLE_MANAGER");
        ChiNhanh cn002 = mockBranch(2, "CN002");
        NhanVien emp = mockEmployee(mgr2, cn002);
        stubUser(mgr2, mgrRole, emp);

        String token = jwtService.generateToken("manager2", List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/api/test/branches/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void branchManager_CN002_CanAccessCN002() throws Exception {
        NguoiDung mgr2 = mockUser(6, "manager2");
        VaiTro mgrRole = mockRole(2, "ROLE_MANAGER");
        ChiNhanh cn002 = mockBranch(2, "CN002");
        NhanVien emp = mockEmployee(mgr2, cn002);
        stubUser(mgr2, mgrRole, emp);

        String token = jwtService.generateToken("manager2", List.of("ROLE_MANAGER"));

        mockMvc.perform(get("/api/test/branches/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("2"));
    }

    // ---------------------------------------------------------------
    // ROLE_CUSTOMER — denied from branch employee endpoints (RBAC 403)
    // ---------------------------------------------------------------

    @Test
    void customer_DeniedBranchEndpoint() throws Exception {
        NguoiDung cus = mockUser(5, "customer");
        VaiTro cusRole = mockRole(5, "ROLE_CUSTOMER");
        stubUser(cus, cusRole, null);

        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/test/branches/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------
    // No JWT → 401
    // ---------------------------------------------------------------

    @Test
    void noJwt_Returns401() throws Exception {
        mockMvc.perform(get("/api/test/branches/1"))
                .andExpect(status().isUnauthorized());
    }
}
