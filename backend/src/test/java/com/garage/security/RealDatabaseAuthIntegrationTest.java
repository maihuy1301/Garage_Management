package com.garage.security;

import com.garage.dto.LoginRequest;
import com.garage.dto.LoginResponse;
import com.garage.entity.NguoiDung;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.KhachHangRepository;
import com.garage.repository.VaiTroRepository;
import com.garage.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RealDatabaseAuthIntegrationTest {

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @Mock
    private VaiTroRepository vaiTroRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                nguoiDungRepository,
                nguoiDungVaiTroRepository,
                vaiTroRepository,
                khachHangRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void testSeededUserLogin_SystemAdmin() {
        // Simulated entity seeded in SQL Server: admin / Password123@
        NguoiDung adminUser = new NguoiDung();
        adminUser.setMaNguoiDung(1);
        adminUser.setTenDangNhap("admin");
        adminUser.setMatKhauHash("$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO");
        adminUser.setHoTen("Hệ Thống Admin");
        adminUser.setTrangThai(true);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("admin", "admin")).thenReturn(Optional.of(adminUser));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(1)).thenReturn(Collections.emptyList());
        when(jwtService.generateToken("admin", Collections.emptyList())).thenReturn("eyJhbGciOiJIUzI1NiJ9.admin.token");

        LoginRequest loginRequest = new LoginRequest("admin", "Password123@");
        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("admin", response.getTenDangNhap());
        assertEquals("Hệ Thống Admin", response.getHoTen());
        assertEquals("eyJhbGciOiJIUzI1NiJ9.admin.token", response.getAccessToken());
    }

    @Test
    void testSeededUserLogin_BranchManager() {
        NguoiDung mgrUser = new NguoiDung();
        mgrUser.setMaNguoiDung(2);
        mgrUser.setTenDangNhap("manager");
        mgrUser.setMatKhauHash("$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO");
        mgrUser.setHoTen("Nguyễn Văn Quản Lý");
        mgrUser.setTrangThai(true);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("manager", "manager")).thenReturn(Optional.of(mgrUser));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(2)).thenReturn(Collections.emptyList());
        when(jwtService.generateToken("manager", Collections.emptyList())).thenReturn("eyJhbGciOiJIUzI1NiJ9.manager.token");

        LoginRequest loginRequest = new LoginRequest("manager", "Password123@");
        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("manager", response.getTenDangNhap());
        assertEquals("Nguyễn Văn Quản Lý", response.getHoTen());
    }
}
