package com.garage.service;

import com.garage.entity.*;
import com.garage.repository.*;
import com.garage.exception.BadRequestException;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SupportBranchServiceTest {
    ChiNhanhRepository branches = mock(ChiNhanhRepository.class);
    DatLichRepository appointments = mock(DatLichRepository.class);
    KhachHangRepository customers = mock(KhachHangRepository.class);
    NguoiDungRepository users = mock(NguoiDungRepository.class);
    ChiNhanh a = branch(1), b = branch(2);
    SupportBranchService service(String coords) { return new SupportBranchService(branches, appointments, customers, users, true, coords); }
    @BeforeEach void setup() {
        var user = new NguoiDung(); user.setMaNguoiDung(7); user.setTrangThai(true);
        var customer = new KhachHang(); customer.setMaKhachHang(10);
        when(users.findByTenDangNhapOrEmail("me", "me")).thenReturn(Optional.of(user));
        when(customers.findByNguoiDungMaNguoiDung(7)).thenReturn(Optional.of(customer));
        when(branches.findByTrangThaiTrue()).thenReturn(List.of(a,b));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("me", "", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    static ChiNhanh branch(int id) { var b = new ChiNhanh(); b.setMaChiNhanh(id); b.setTenChiNhanh("Gara " + id); b.setDiaChi("Địa chỉ " + id); return b; }
    DatLich booking(ChiNhanh branch, String status) { var d = new DatLich(); d.setChiNhanh(branch); d.setTrangThai(status); return d; }
    @Test void historyUsesAuthenticatedCustomerAndExcludesCancelledPendingNoShow() {
        when(appointments.findByKhachHangMaKhachHang(10)).thenReturn(List.of(booking(a,"HUY"),booking(a,"CHO_XAC_NHAN"),booking(a,"KHONG_DEN"),booking(b,"HOAN_TAT")));
        var result = service("").recommend(null,null);
        assertEquals(2,result.branches().get(0).id()); assertEquals(1,result.branches().get(0).bookingCount());
        assertEquals(0,result.branches().get(1).bookingCount());
        verify(appointments).findByKhachHangMaKhachHang(10);
    }
    @Test void gpsOverridesFrequentBranchAndUsesStraightLineDistance() {
        when(appointments.findByKhachHangMaKhachHang(10)).thenReturn(List.of(booking(b,"HOAN_TAT")));
        var result = service("1:10:106;2:11:106").recommend(10.0,106.0);
        assertEquals(1,result.branches().get(0).id()); assertEquals(0,result.branches().get(0).distanceKm(),0.0001);
        assertEquals(111.195,result.branches().get(1).distanceKm(),0.01);
    }
    @Test void missingCoordinatesNeverInventDistanceOrGlobalNearest() {
        var result = service("1:10:106").recommend(10.0,106.0);
        assertNull(result.branches().get(1).distanceKm());
        assertTrue(result.explanation().contains("chưa thể xác định gần nhất"));
        assertTrue(service("").recommend(10.0,106.0).explanation().contains("chưa tính được"));
    }
    @Test void rejectsInvalidGps() {
        assertThrows(BadRequestException.class,()->service("").recommend(Double.NaN,106.0));
        assertThrows(BadRequestException.class,()->service("").recommend(91.0,106.0));
        assertThrows(BadRequestException.class,()->service("").recommend(10.0,null));
    }
    @Test void staffCannotReadCustomerSuggestions() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("me","", List.of(new SimpleGrantedAuthority("ROLE_FRONT_DESK"))));
        assertThrows(AccessDeniedException.class,()->service("").recommend(null,null));
        verifyNoInteractions(appointments);
    }
    @Test void disabledFeatureDeniesSuggestions() {
        assertThrows(BadRequestException.class,()->new SupportBranchService(branches,appointments,customers,users,false,"").recommend(null,null));
    }
}
