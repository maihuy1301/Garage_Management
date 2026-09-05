package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.KhachHang;
import com.garage.entity.NguoiDung;
import com.garage.entity.VaiTro;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.KhachHangRepository;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customerService = new CustomerService(
                khachHangRepository,
                nguoiDungRepository,
                nguoiDungVaiTroRepository
        );
    }

    private NguoiDung createMockUser(Integer id, String username) {
        NguoiDung u = new NguoiDung();
        u.setMaNguoiDung(id);
        u.setTenDangNhap(username);
        u.setHoTen(username + " FullName");
        u.setEmail(username + "@test.com");
        u.setTrangThai(true);
        return u;
    }

    private KhachHang createMockCustomer(Integer id, String code, NguoiDung user) {
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang(id);
        kh.setNguoiDung(user);
        kh.setDiaChi("123 Test Street");
        kh.setNgaySinh(LocalDate.of(1995, 5, 10));
        return kh;
    }

    private void setSecurityContextUser(NguoiDung user, String role) {
        SecurityContext ctx = mock(SecurityContext.class);
        CustomUserDetails userDetails = new CustomUserDetails(user, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void getCurrentCustomerProfile_Success() {
        NguoiDung user = createMockUser(5, "customer");
        setSecurityContextUser(user, "CUSTOMER");

        KhachHang customer = createMockCustomer(1, "KH001", user);
        when(khachHangRepository.findByNguoiDungMaNguoiDung(5)).thenReturn(Optional.of(customer));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(5)).thenReturn(Collections.emptyList());

        CustomerResponse res = customerService.getCurrentCustomerProfile();
        assertNotNull(res);
        assertEquals(1, res.getMaKhachHang());
        assertEquals("customer", res.getTenDangNhap());
    }

    @Test
    void getCustomerById_AsOwner_Success() {
        NguoiDung user = createMockUser(5, "customer");
        setSecurityContextUser(user, "CUSTOMER");

        KhachHang customer = createMockCustomer(1, "KH001", user);
        when(khachHangRepository.findById(1)).thenReturn(Optional.of(customer));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(5)).thenReturn(Collections.emptyList());

        CustomerResponse res = customerService.getCustomerById(1);
        assertNotNull(res);
        assertEquals(1, res.getMaKhachHang());
    }

    @Test
    void getCustomerById_AsOtherCustomer_ThrowsAccessDenied() {
        NguoiDung userA = createMockUser(5, "customerA");
        setSecurityContextUser(userA, "CUSTOMER");

        NguoiDung userB = createMockUser(6, "customerB");
        KhachHang customerB = createMockCustomer(2, "KH002", userB);

        when(khachHangRepository.findById(2)).thenReturn(Optional.of(customerB));

        assertThrows(AccessDeniedException.class, () -> customerService.getCustomerById(2));
    }

    @Test
    void getCustomerById_AsSystemAdmin_Success() {
        NguoiDung admin = createMockUser(1, "admin");
        setSecurityContextUser(admin, "ADMIN");

        NguoiDung userB = createMockUser(6, "customerB");
        KhachHang customerB = createMockCustomer(2, "KH002", userB);

        when(khachHangRepository.findById(2)).thenReturn(Optional.of(customerB));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(6)).thenReturn(Collections.emptyList());

        CustomerResponse res = customerService.getCustomerById(2);
        assertNotNull(res);
        assertEquals(2, res.getMaKhachHang());
    }

    @Test
    void createCustomer_Success() {
        CreateCustomerRequest req = new CreateCustomerRequest(10, "456 New Road", LocalDate.of(1990, 1, 1));
        NguoiDung u10 = createMockUser(10, "newuser");

        when(khachHangRepository.existsByNguoiDungMaNguoiDung(10)).thenReturn(false);
        when(nguoiDungRepository.findById(10)).thenReturn(Optional.of(u10));

        KhachHang saved = createMockCustomer(99, "KH099", u10);
        when(khachHangRepository.save(any(KhachHang.class))).thenReturn(saved);

        CustomerResponse res = customerService.createCustomer(req);
        assertNotNull(res);
        assertEquals(99, res.getMaKhachHang());
    }

    @Test
    void createCustomer_DuplicateLinkedUser_ThrowsConflict() {
        CreateCustomerRequest req = new CreateCustomerRequest(10, "456 New Road", LocalDate.of(1990, 1, 1));
        when(khachHangRepository.existsByNguoiDungMaNguoiDung(10)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.createCustomer(req));
    }
}
