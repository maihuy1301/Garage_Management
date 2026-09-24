package com.garage.service;

import com.garage.dto.HandoverRequest;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleHandoverServiceTest {
    final PhieuTiepNhanRepository receptions = mock(PhieuTiepNhanRepository.class);
    final PhieuSuaChuaRepository repairs = mock(PhieuSuaChuaRepository.class);
    final HoaDonRepository invoices = mock(HoaDonRepository.class);
    final BanGiaoXeRepository handovers = mock(BanGiaoXeRepository.class);
    final NguoiDungRepository users = mock(NguoiDungRepository.class);
    final BranchAuthorizationService branches = mock(BranchAuthorizationService.class);
    final NotificationService notifications = mock(NotificationService.class);
    final VehicleHandoverService service = new VehicleHandoverService(receptions, repairs, invoices, handovers, users, branches, notifications);
    PhieuTiepNhan reception;
    PhieuSuaChua repair;
    HoaDon invoice;
    NguoiDung customer;
    NguoiDung staff;

    @BeforeEach void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "staff", null, List.of(new SimpleGrantedAuthority("ROLE_FRONT_DESK"))));
        staff = new NguoiDung(); staff.setHoTen("Receptionist"); staff.setTrangThai(true);
        customer = new NguoiDung(); customer.setTrangThai(true);
        var owner = new KhachHang(); owner.setNguoiDung(customer);
        var vehicle = new Xe(); vehicle.setKhachHang(owner); vehicle.setBienSo("51A-12345");
        var branch = new ChiNhanh(); branch.setMaChiNhanh(1); branch.setTenChiNhanh("Central");
        reception = new PhieuTiepNhan(); reception.setMaTiepNhan(10); reception.setChiNhanh(branch);
        reception.setXe(vehicle); reception.setDatLich(new DatLich());
        repair = new PhieuSuaChua(); repair.setMaPhieuSuaChua(20); repair.setTrangThai("HOAN_TAT");
        invoice = new HoaDon(); invoice.setTrangThai("DA_THANH_TOAN");
        when(receptions.findForHandover(10)).thenReturn(Optional.of(reception));
        when(receptions.findById(10)).thenReturn(Optional.of(reception));
        when(branches.isAllowedBranch(1)).thenReturn(true);
        when(repairs.findAllByPhieuTiepNhanMaTiepNhan(10)).thenReturn(List.of(repair));
        when(invoices.findByPhieuSuaChuaMaPhieuSuaChua(20)).thenReturn(Optional.of(invoice));
        when(users.findByTenDangNhapOrEmail("staff", "staff")).thenReturn(Optional.of(staff));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void recordsAuditUpdatesStagesAndNotifiesOwner() {
        var result = service.handover(10, new HandoverRequest("  Checked  "));
        assertThat(result.thoiGianBanGiao()).isNotNull();
        assertThat(result.ghiChu()).isEqualTo("Checked");
        assertThat(reception.getTrangThai()).isEqualTo("DA_BAN_GIAO");
        assertThat(reception.getDatLich().getTrangThai()).isEqualTo("HOAN_TAT");
        verify(handovers).save(argThat(h -> h.getNguoiBanGiao() == staff && h.getMaTiepNhan() == 10));
        verify(notifications).sendNotification(same(customer), anyString(), contains("51A-12345"), eq("VEHICLE_HANDED_OVER"), eq(10));
    }
    @Test void repeatedConfirmationReturnsExistingAuditWithoutAnotherNotification() {
        var existing = new BanGiaoXe(); existing.setNguoiBanGiao(staff); existing.setThoiGianBanGiao(LocalDateTime.now());
        when(handovers.findById(10)).thenReturn(Optional.of(existing));
        assertThat(service.handover(10, null).thoiGianBanGiao()).isEqualTo(existing.getThoiGianBanGiao());
        verifyNoInteractions(notifications);
        verify(handovers, never()).save(any());
    }
    @Test void unpaidInvoiceBlocksHandover() {
        invoice.setTrangThai("THANH_TOAN_MOT_PHAN");
        assertThatThrownBy(() -> service.handover(10, null)).isInstanceOf(BadRequestException.class);
        verifyNoInteractions(notifications);
        verify(handovers, never()).save(any());
    }
    @Test void unfinishedChildRepairBlocksHandover() {
        var child = new PhieuSuaChua(); child.setTrangThai("DANG_SUA"); child.setPhieuCha(repair);
        when(repairs.findAllByPhieuTiepNhanMaTiepNhan(10)).thenReturn(List.of(repair, child));
        assertThatThrownBy(() -> service.handover(10, null)).isInstanceOf(BadRequestException.class);
        verifyNoInteractions(notifications);
    }
    @Test void missingInvoiceBlocksHandover() {
        when(invoices.findByPhieuSuaChuaMaPhieuSuaChua(20)).thenReturn(Optional.empty());
        assertThat(service.getStatus(10).eligible()).isFalse();
        assertThatThrownBy(() -> service.handover(10, null)).isInstanceOf(BadRequestException.class);
    }
    @Test void crossBranchDeniedEvenForExistingHandover() {
        when(branches.isAllowedBranch(1)).thenReturn(false);
        assertThatThrownBy(() -> service.handover(10, null)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(handovers, notifications);
    }
    @Test void customerCannotConfirmHandover() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "customer", null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));
        assertThatThrownBy(() -> service.handover(10, null)).isInstanceOf(AccessDeniedException.class);
    }
    @Test void completedWalkInHandoverDoesNotPushToInactiveAccount() {
        customer.setTrangThai(false);
        service.handover(10, null);
        verifyNoInteractions(notifications);
        verify(handovers).save(any());
    }
}
