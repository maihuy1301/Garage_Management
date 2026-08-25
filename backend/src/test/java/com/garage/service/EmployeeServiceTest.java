package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.ChiNhanh;
import com.garage.entity.NguoiDung;
import com.garage.entity.NhanVien;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.ChiNhanhRepository;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.NhanVienRepository;
import com.garage.security.BranchAuthorizationService;
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

class EmployeeServiceTest {

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private ChiNhanhRepository chiNhanhRepository;

    @Mock
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeService = new EmployeeService(
                nhanVienRepository,
                nguoiDungRepository,
                chiNhanhRepository,
                nguoiDungVaiTroRepository,
                branchAuthorizationService
        );
    }

    private ChiNhanh createMockBranch(Integer id, String code) {
        ChiNhanh b = new ChiNhanh();
        b.setMaChiNhanh(id);
        b.setMaChiNhanhCode(code);
        b.setTenChiNhanh("Branch " + code);
        return b;
    }

    private NguoiDung createMockUser(Integer id, String username) {
        NguoiDung u = new NguoiDung();
        u.setMaNguoiDung(id);
        u.setTenDangNhap(username);
        u.setHoTen(username + " Name");
        u.setEmail(username + "@garage.com");
        return u;
    }

    private NhanVien createMockEmployee(Integer id, String code, NguoiDung user, ChiNhanh branch) {
        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(id);
        nv.setMaNhanVienCode(code);
        nv.setNguoiDung(user);
        nv.setChiNhanh(branch);
        nv.setChucVu("Thợ sửa chữa");
        nv.setNgayVaoLam(LocalDate.of(2024, 1, 1));
        nv.setTrangThai(true);
        return nv;
    }

    @Test
    void getAllEmployees_SystemAdmin_ReturnsAll() {
        SecurityContext ctx = mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        ChiNhanh cn1 = createMockBranch(1, "CN001");
        ChiNhanh cn2 = createMockBranch(2, "CN002");
        NhanVien nv1 = createMockEmployee(1, "NV01", createMockUser(1, "user1"), cn1);
        NhanVien nv2 = createMockEmployee(2, "NV02", createMockUser(2, "user2"), cn2);

        when(nhanVienRepository.findAll()).thenReturn(List.of(nv1, nv2));

        List<EmployeeResponse> results = employeeService.getAllEmployees();
        assertEquals(2, results.size());
    }

    @Test
    void getAllEmployees_BranchManager_ReturnsOnlyOwnBranch() {
        SecurityContext ctx = mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "manager", null, List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
        );
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(branchAuthorizationService.resolveUserBranchId(auth)).thenReturn(Optional.of(1));

        ChiNhanh cn1 = createMockBranch(1, "CN001");
        NhanVien nv1 = createMockEmployee(1, "NV01", createMockUser(1, "user1"), cn1);

        when(nhanVienRepository.findByChiNhanhMaChiNhanh(1)).thenReturn(List.of(nv1));

        List<EmployeeResponse> results = employeeService.getAllEmployees();
        assertEquals(1, results.size());
        assertEquals("CN001", results.get(0).getMaChiNhanhCode());
    }

    @Test
    void getEmployeeById_OwnBranch_ReturnsSuccess() {
        ChiNhanh cn1 = createMockBranch(1, "CN001");
        NhanVien nv1 = createMockEmployee(1, "NV01", createMockUser(1, "user1"), cn1);

        when(nhanVienRepository.findById(1)).thenReturn(Optional.of(nv1));
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        EmployeeResponse res = employeeService.getEmployeeById(1);
        assertNotNull(res);
        assertEquals("NV01", res.getMaNhanVienCode());
    }

    @Test
    void getEmployeeById_OtherBranch_ThrowsAccessDenied() {
        ChiNhanh cn2 = createMockBranch(2, "CN002");
        NhanVien nv2 = createMockEmployee(2, "NV02", createMockUser(2, "user2"), cn2);

        when(nhanVienRepository.findById(2)).thenReturn(Optional.of(nv2));
        when(branchAuthorizationService.isAllowedBranch(2)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> employeeService.getEmployeeById(2));
    }

    @Test
    void createEmployee_Success() {
        CreateEmployeeRequest req = new CreateEmployeeRequest("NV99", 10, 1, "Thợ chính", LocalDate.now());
        ChiNhanh cn1 = createMockBranch(1, "CN001");
        NguoiDung u10 = createMockUser(10, "user10");

        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(nhanVienRepository.existsByMaNhanVienCode("NV99")).thenReturn(false);
        when(nhanVienRepository.existsByNguoiDungMaNguoiDung(10)).thenReturn(false);
        when(nguoiDungRepository.findById(10)).thenReturn(Optional.of(u10));
        when(chiNhanhRepository.findById(1)).thenReturn(Optional.of(cn1));

        NhanVien saved = createMockEmployee(99, "NV99", u10, cn1);
        when(nhanVienRepository.save(any(NhanVien.class))).thenReturn(saved);

        EmployeeResponse res = employeeService.createEmployee(req);
        assertNotNull(res);
        assertEquals("NV99", res.getMaNhanVienCode());
    }

    @Test
    void createEmployee_DuplicateCode_ThrowsConflict() {
        CreateEmployeeRequest req = new CreateEmployeeRequest("NV01", 10, 1, "Thợ chính", LocalDate.now());
        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);
        when(nhanVienRepository.existsByMaNhanVienCode("NV01")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> employeeService.createEmployee(req));
    }

    @Test
    void createEmployee_OtherBranch_ThrowsAccessDenied() {
        CreateEmployeeRequest req = new CreateEmployeeRequest("NV99", 10, 2, "Thợ chính", LocalDate.now());
        when(branchAuthorizationService.isAllowedBranch(2)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> employeeService.createEmployee(req));
    }
}
