package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.VaiTroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @Mock
    private VaiTroRepository vaiTroRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(nguoiDungRepository, nguoiDungVaiTroRepository, vaiTroRepository, passwordEncoder);
    }

    private NguoiDung createMockUser(Integer id, String username, String email) {
        NguoiDung user = new NguoiDung();
        user.setMaNguoiDung(id);
        user.setTenDangNhap(username);
        user.setEmail(email);
        user.setHoTen(username + " FullName");
        user.setMatKhauHash("$2a$10$hashed");
        user.setTrangThai(true);
        return user;
    }

    private VaiTro createMockRole(Integer id, String roleName) {
        VaiTro r = new VaiTro();
        r.setMaVaiTro(id);
        r.setTenVaiTro(roleName);
        return r;
    }

    @Test
    void testGetAllUsers() {
        NguoiDung u1 = createMockUser(1, "admin", "admin@test.com");
        NguoiDung u2 = createMockUser(2, "manager", "manager@test.com");

        when(nguoiDungRepository.findAll()).thenReturn(List.of(u1, u2));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(1)).thenReturn(Collections.emptyList());
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(2)).thenReturn(Collections.emptyList());

        List<UserResponse> responses = userService.getAllUsers();
        assertEquals(2, responses.size());
        assertEquals("admin", responses.get(0).getTenDangNhap());
        assertEquals("manager", responses.get(1).getTenDangNhap());
    }

    @Test
    void testGetUserById_Found() {
        NguoiDung user = createMockUser(1, "admin", "admin@test.com");
        VaiTro role = createMockRole(1, "ROLE_ADMIN");
        NguoiDungVaiTro link = new NguoiDungVaiTro(user, role);

        when(nguoiDungRepository.findById(1)).thenReturn(Optional.of(user));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(1)).thenReturn(List.of(link));

        UserResponse response = userService.getUserById(1);
        assertNotNull(response);
        assertEquals("admin", response.getTenDangNhap());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void testGetUserById_NotFound_ThrowsException() {
        when(nguoiDungRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999));
    }

    @Test
    void testCreateUser_Success() {
        CreateUserRequest req = new CreateUserRequest("newuser", "Password123@", "123456", "New User", "new@test.com", "0900000099", null, List.of("ROLE_MANAGER"));
        VaiTro role = createMockRole(2, "ROLE_MANAGER");

        when(nguoiDungRepository.existsByTenDangNhap("newuser")).thenReturn(false);
        when(nguoiDungRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(vaiTroRepository.findByTenVaiTro("ROLE_MANAGER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Password123@")).thenReturn("$2a$10$encodedPassword");
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encodedPin");
        
        NguoiDung savedUser = createMockUser(10, "newuser", "new@test.com");
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenReturn(savedUser);
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(10)).thenReturn(List.of(new NguoiDungVaiTro(savedUser, role)));

        UserResponse response = userService.createUser(req);
        assertNotNull(response);
        assertEquals("newuser", response.getTenDangNhap());
        verify(passwordEncoder).encode("Password123@");
        verify(passwordEncoder).encode("123456");
        verify(nguoiDungVaiTroRepository).save(any(NguoiDungVaiTro.class));
    }

    @Test
    void testCreateUser_DuplicateUsername_ThrowsConflict() {
        CreateUserRequest req = new CreateUserRequest("existinguser", "Password123@", "123456", "User", "user@test.com", null, null, null);
        when(nguoiDungRepository.existsByTenDangNhap("existinguser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(req));
    }

    @Test
    void testCreateUser_InvalidRole_ThrowsBadRequest() {
        CreateUserRequest req = new CreateUserRequest("newuser", "Password123@", "123456", "User", "user@test.com", null, null, List.of("NON_EXISTENT_ROLE"));
        when(nguoiDungRepository.existsByTenDangNhap("newuser")).thenReturn(false);
        when(nguoiDungRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(vaiTroRepository.findByTenVaiTro("NON_EXISTENT_ROLE")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userService.createUser(req));
    }

    @Test
    void testUpdateUser_Success() {
        NguoiDung existing = createMockUser(1, "user1", "old@test.com");
        UpdateUserRequest req = new UpdateUserRequest("Updated Name", "new@test.com", "0999999999", null, null);

        when(nguoiDungRepository.findById(1)).thenReturn(Optional.of(existing));
        when(nguoiDungRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenReturn(existing);
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(1)).thenReturn(Collections.emptyList());

        UserResponse response = userService.updateUser(1, req);
        assertNotNull(response);
        verify(nguoiDungRepository).save(existing);
    }

    @Test
    void testUpdateUserStatus_DisableLastAdmin_ThrowsBadRequest() {
        NguoiDung admin = createMockUser(1, "admin", "admin@test.com");
        VaiTro adminRole = createMockRole(1, "ROLE_ADMIN");
        NguoiDungVaiTro link = new NguoiDungVaiTro(admin, adminRole);

        when(nguoiDungRepository.findById(1)).thenReturn(Optional.of(admin));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(1)).thenReturn(List.of(link));
        when(nguoiDungVaiTroRepository.countActiveUsersByRoleName("ROLE_ADMIN")).thenReturn(1L);

        UpdateUserStatusRequest req = new UpdateUserStatusRequest(false);
        assertThrows(BadRequestException.class, () -> userService.updateUserStatus(1, req));
    }
}
