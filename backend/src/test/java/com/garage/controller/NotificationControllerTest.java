package com.garage.controller;

import com.garage.dto.NotificationResponse;
import com.garage.dto.UnreadCountResponse;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NguoiDungRepository nguoiDungRepository;

    @MockBean
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    private NguoiDung mockUser(Integer id, String username) {
        NguoiDung u = new NguoiDung();
        u.setMaNguoiDung(id);
        u.setTenDangNhap(username);
        u.setHoTen(username + " FullName");
        u.setEmail(username + "@garage.com");
        u.setMatKhauHash("$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO");
        u.setTrangThai(true);
        return u;
    }

    private void stubUser(NguoiDung user, String roleName) {
        VaiTro role = new VaiTro();
        role.setMaVaiTro(user.getMaNguoiDung());
        role.setTenVaiTro(roleName);
        when(nguoiDungRepository.findByTenDangNhapOrEmail(user.getTenDangNhap(), user.getTenDangNhap()))
                .thenReturn(Optional.of(user));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()))
                .thenReturn(List.of(new NguoiDungVaiTro(user, role)));
    }

    private NotificationResponse sampleNotification(Integer id, Integer userId) {
        return new NotificationResponse(
                id, userId, "Lịch hẹn được duyệt", "Lịch hẹn 09:00 ngày 20/08 đã được duyệt",
                "APPOINTMENT_CONFIRMED", 501, false, LocalDateTime.now()
        );
    }

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_getNotifications_returns401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_markRead_returns401() throws Exception {
        mockMvc.perform(patch("/api/notifications/101/read"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. Authenticated User (Customer)
    // ==========================================

    @Test
    void customer_getNotifications_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        when(notificationService.getUserNotifications()).thenReturn(List.of(sampleNotification(101, 5)));

        mockMvc.perform(get("/api/notifications").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maThongBao").value(101));
    }

    @Test
    void customer_getNotificationById_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        when(notificationService.getNotificationById(101)).thenReturn(sampleNotification(101, 5));

        mockMvc.perform(get("/api/notifications/101").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maThongBao").value(101));
    }

    @Test
    void customer_markAsRead_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        NotificationResponse readNotif = sampleNotification(101, 5);
        readNotif.setDaDoc(true);
        when(notificationService.markAsRead(101)).thenReturn(readNotif);

        mockMvc.perform(patch("/api/notifications/101/read").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.daDoc").value(true));
    }

    @Test
    void customer_markAllAsRead_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        mockMvc.perform(patch("/api/notifications/read-all").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).markAllAsRead();
    }

    @Test
    void customer_getUnreadCount_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        when(notificationService.getUnreadCount()).thenReturn(new UnreadCountResponse(4L));

        mockMvc.perform(get("/api/notifications/unread-count").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(4));
    }
}
