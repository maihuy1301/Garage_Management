package com.garage.service;

import com.garage.entity.NguoiDung;
import com.garage.entity.ThietBiPush;
import com.garage.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class PushDeviceServiceTest {
    final ThietBiPushRepository devices = mock(ThietBiPushRepository.class);
    final NguoiDungRepository users = mock(NguoiDungRepository.class);
    final PushDeviceService service = new PushDeviceService(devices, users);

    @BeforeEach void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "customer", null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));
        NguoiDung user = new NguoiDung(); user.setMaNguoiDung(5); user.setTrangThai(true);
        when(users.findByTenDangNhapOrEmail("customer", "customer")).thenReturn(Optional.of(user));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void storesTokenForAuthenticatedUserOnly() {
        service.register("test-token");
        var captor = ArgumentCaptor.forClass(ThietBiPush.class);
        verify(devices).save(captor.capture());
        assertThat(captor.getValue().getMaNguoiDung()).isEqualTo(5);
        assertThat(captor.getValue().getTokenHash()).hasSize(64);
        assertThat(captor.getValue().getCapNhatLuc()).isNotNull();
    }
    @Test void reassignsSharedDeviceWithoutDuplicatingToken() {
        var device = new ThietBiPush(); device.setMaNguoiDung(9);
        when(devices.findForUpdate(PushDeviceService.hash("test-token"))).thenReturn(Optional.of(device));
        service.register("test-token");
        assertThat(device.getMaNguoiDung()).isEqualTo(5);
        verify(devices).save(device);
    }
    @Test void logoutOnlyDeletesTokenBelongingToCurrentUser() {
        service.unregister("test-token");
        verify(devices).deleteByTokenHashAndMaNguoiDung(PushDeviceService.hash("test-token"), 5);
    }
    @Test void rejectsStaffTokenRegistration() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "staff", null, List.of(new SimpleGrantedAuthority("ROLE_FRONT_DESK"))));
        assertThatThrownBy(() -> service.register("test-token")).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(devices);
    }
    @Test void invalidTokenCleanupIsOwnerScoped() {
        service.removeInvalid("test-token", 9);
        verify(devices).deleteByTokenHashAndMaNguoiDung(PushDeviceService.hash("test-token"), 9);
    }
}
