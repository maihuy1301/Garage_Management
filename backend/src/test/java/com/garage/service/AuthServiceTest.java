package com.garage.service;

import com.garage.dto.LoginRequest;
import com.garage.dto.LoginResponse;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(nguoiDungRepository, nguoiDungVaiTroRepository, passwordEncoder, jwtService);
    }

    @Test
    void testLogin_Success() {
        NguoiDung user = new NguoiDung();
        user.setMaNguoiDung(1);
        user.setTenDangNhap("admin");
        user.setMatKhauHash("$2a$10$hashedpassword");
        user.setHoTen("Administrator");
        user.setTrangThai(true);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("admin", "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(1)).thenReturn(Collections.emptyList());
        when(jwtService.generateToken(anyString(), anyList())).thenReturn("mocked.jwt.token");

        LoginRequest request = new LoginRequest("admin", "password123");
        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getAccessToken());
        assertEquals("admin", response.getTenDangNhap());
        assertEquals("Administrator", response.getHoTen());
    }

    @Test
    void testLogin_UserNotFound_ThrowsBadCredentials() {
        when(nguoiDungRepository.findByTenDangNhapOrEmail(anyString(), anyString())).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown_user", "password123");
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_WrongPassword_ThrowsBadCredentials() {
        NguoiDung user = new NguoiDung();
        user.setTenDangNhap("admin");
        user.setMatKhauHash("$2a$10$hashedpassword");
        user.setTrangThai(true);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("admin", "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "$2a$10$hashedpassword")).thenReturn(false);

        LoginRequest request = new LoginRequest("admin", "wrongpass");
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_DisabledUser_ThrowsDisabledException() {
        NguoiDung user = new NguoiDung();
        user.setMaNguoiDung(99);
        user.setTenDangNhap("disabled_user");
        user.setMatKhauHash("$2a$10$hashedpassword");
        user.setTrangThai(false);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("disabled_user", "disabled_user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);

        LoginRequest request = new LoginRequest("disabled_user", "password123");
        assertThrows(DisabledException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_WithRole_IncludesRoleInToken() {
        NguoiDung user = new NguoiDung();
        user.setMaNguoiDung(1);
        user.setTenDangNhap("admin");
        user.setMatKhauHash("$2a$10$hashedpassword");
        user.setHoTen("Administrator");
        user.setTrangThai(true);

        VaiTro adminRole = new VaiTro();
        adminRole.setMaVaiTro(1);
        adminRole.setTenVaiTro("ROLE_ADMIN");

        NguoiDung adminEntity = new NguoiDung();
        adminEntity.setMaNguoiDung(1);
        NguoiDungVaiTro userRole = new NguoiDungVaiTro(adminEntity, adminRole);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("admin", "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(1)).thenReturn(List.of(userRole));
        when(jwtService.generateToken("admin", List.of("ROLE_ADMIN"))).thenReturn("jwt.with.role.token");

        LoginRequest request = new LoginRequest("admin", "password123");
        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt.with.role.token", response.getAccessToken());
    }
}
