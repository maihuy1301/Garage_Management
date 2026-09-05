package com.garage.service;

import com.garage.dto.LoginRequest;
import com.garage.dto.LoginResponse;
import com.garage.dto.RegisterRequest;
import com.garage.dto.RegisterResponse;
import com.garage.entity.KhachHang;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.exception.DuplicateResourceException;
import com.garage.repository.KhachHangRepository;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.VaiTroRepository;
import com.garage.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @Mock
    private VaiTroRepository vaiTroRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        assertFalse(response.isHasPin());
    }

    @Test
    void testLogin_WithConfiguredPin_ReturnsHasPinTrue() {
        NguoiDung user = new NguoiDung();
        user.setMaNguoiDung(1);
        user.setTenDangNhap("customer");
        user.setMatKhauHash("$2a$10$hashedpassword");
        user.setMaPinHash("$2a$10$hashedpin");
        user.setHoTen("Customer");
        user.setTrangThai(true);

        when(nguoiDungRepository.findByTenDangNhapOrEmail("customer", "customer"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(1)).thenReturn(Collections.emptyList());
        when(jwtService.generateToken(anyString(), anyList())).thenReturn("mocked.jwt.token");

        LoginResponse response = authService.login(new LoginRequest("customer", "password123"));

        assertTrue(response.isHasPin());
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

    @Test
    void registerCustomer_Success_HashesPasswordAndCreatesCustomerRole() {
        VaiTro customerRole = new VaiTro();
        customerRole.setMaVaiTro(5);
        customerRole.setTenVaiTro("ROLE_CUSTOMER");

        when(nguoiDungRepository.existsByTenDangNhap("0901234567")).thenReturn(false);
        when(nguoiDungRepository.existsByEmail("customer@example.com")).thenReturn(false);
        when(vaiTroRepository.findByTenVaiTro("ROLE_CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("SecurePass123")).thenReturn("$2a$10$encoded");
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(invocation -> {
            NguoiDung user = invocation.getArgument(0);
            user.setMaNguoiDung(20);
            return user;
        });
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(invocation -> {
            KhachHang customer = invocation.getArgument(0);
            customer.setMaKhachHang(12);
            return customer;
        });

        RegisterResponse response = authService.registerCustomer(new RegisterRequest(
                " Nguyễn Văn Khách ",
                "0901234567",
                " Customer@Example.com ",
                "SecurePass123",
                true
        ));

        assertEquals(20, response.getMaNguoiDung());
        assertEquals(12, response.getMaKhachHang());
        assertEquals("0901234567", response.getTenDangNhap());
        assertEquals("Nguyễn Văn Khách", response.getHoTen());
        assertEquals("customer@example.com", response.getEmail());

        ArgumentCaptor<NguoiDung> userCaptor = ArgumentCaptor.forClass(NguoiDung.class);
        verify(nguoiDungRepository).save(userCaptor.capture());
        assertEquals("$2a$10$encoded", userCaptor.getValue().getMatKhauHash());
        assertNotEquals("SecurePass123", userCaptor.getValue().getMatKhauHash());
        assertNull(userCaptor.getValue().getMaPinHash());

        ArgumentCaptor<NguoiDungVaiTro> roleCaptor = ArgumentCaptor.forClass(NguoiDungVaiTro.class);
        verify(nguoiDungVaiTroRepository).save(roleCaptor.capture());
        assertEquals("ROLE_CUSTOMER", roleCaptor.getValue().getVaiTro().getTenVaiTro());

        ArgumentCaptor<KhachHang> customerCaptor = ArgumentCaptor.forClass(KhachHang.class);
        verify(khachHangRepository).save(customerCaptor.capture());
        assertSame(userCaptor.getValue(), customerCaptor.getValue().getNguoiDung());
    }

    @Test
    void registerCustomer_DuplicatePhone_DoesNotPersistAnything() {
        when(nguoiDungRepository.existsByTenDangNhap("0901234567")).thenReturn(true);

        RegisterRequest request = new RegisterRequest(
                "Nguyễn Văn Khách", "0901234567", null, "SecurePass123", true
        );

        assertThrows(DuplicateResourceException.class, () -> authService.registerCustomer(request));
        verify(passwordEncoder, never()).encode(anyString());
        verify(nguoiDungRepository, never()).save(any());
        verify(nguoiDungVaiTroRepository, never()).save(any());
        verify(khachHangRepository, never()).save(any());
    }
}
