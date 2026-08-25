package com.garage.service;

import com.garage.dto.NotificationResponse;
import com.garage.dto.UnreadCountResponse;
import com.garage.entity.NguoiDung;
import com.garage.entity.ThongBao;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.ThongBaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private ThongBaoRepository thongBaoRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private com.garage.websocket.WebSocketEventPublisher webSocketEventPublisher;

    @InjectMocks
    private NotificationService notificationService;

    private NguoiDung user1;
    private NguoiDung user2;
    private ThongBao notif1;

    @BeforeEach
    void setUp() {
        user1 = new NguoiDung();
        user1.setMaNguoiDung(10);
        user1.setTenDangNhap("customer1");
        user1.setHoTen("Nguyễn Văn Khách");

        user2 = new NguoiDung();
        user2.setMaNguoiDung(20);
        user2.setTenDangNhap("technician1");
        user2.setHoTen("Trần Kỹ Thuật");

        notif1 = new ThongBao();
        notif1.setMaThongBao(101);
        notif1.setNguoiDung(user1);
        notif1.setTieuDe("Lịch hẹn đã được xác nhận");
        notif1.setNoiDung("Lịch hẹn lúc 09:00 ngày 20/08 đã được duyệt.");
        notif1.setLoaiThongBao("APPOINTMENT_CONFIRMED");
        notif1.setMaThamChieu(501);
        notif1.setDaDoc(false);
        notif1.setNgayTao(LocalDateTime.now());
    }

    private void stubUserAuth(NguoiDung user) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                user.getTenDangNhap(), "password", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        ));
        SecurityContextHolder.setContext(ctx);
        when(nguoiDungRepository.findByTenDangNhapOrEmail(user.getTenDangNhap(), user.getTenDangNhap()))
                .thenReturn(Optional.of(user));
    }

    @Test
    void sendNotification_success() {
        when(thongBaoRepository.save(any(ThongBao.class))).thenReturn(notif1);

        NotificationResponse res = notificationService.sendNotification(
                user1, "Lịch hẹn đã được xác nhận", "Nội dung", "APPOINTMENT_CONFIRMED", 501
        );

        assertThat(res).isNotNull();
        assertThat(res.getMaThongBao()).isEqualTo(101);
        assertThat(res.getLoaiThongBao()).isEqualTo("APPOINTMENT_CONFIRMED");
        assertThat(res.getDaDoc()).isFalse();
        verify(thongBaoRepository).save(any(ThongBao.class));
    }

    @Test
    void getUserNotifications_returnsList() {
        stubUserAuth(user1);
        when(thongBaoRepository.findByNguoiDungMaNguoiDungOrderByNgayTaoDesc(10)).thenReturn(List.of(notif1));

        List<NotificationResponse> list = notificationService.getUserNotifications();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getMaThongBao()).isEqualTo(101);
    }

    @Test
    void getNotificationById_owner_success() {
        stubUserAuth(user1);
        when(thongBaoRepository.findById(101)).thenReturn(Optional.of(notif1));

        NotificationResponse res = notificationService.getNotificationById(101);

        assertThat(res).isNotNull();
        assertThat(res.getMaThongBao()).isEqualTo(101);
    }

    @Test
    void getNotificationById_otherUser_throws403() {
        stubUserAuth(user2);
        when(thongBaoRepository.findById(101)).thenReturn(Optional.of(notif1));

        assertThatThrownBy(() -> notificationService.getNotificationById(101))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("người dùng khác");
    }

    @Test
    void markAsRead_owner_success_idempotent() {
        stubUserAuth(user1);
        when(thongBaoRepository.findById(101)).thenReturn(Optional.of(notif1));
        when(thongBaoRepository.save(any(ThongBao.class))).thenReturn(notif1);

        NotificationResponse res = notificationService.markAsRead(101);

        assertThat(res).isNotNull();
        assertThat(notif1.getDaDoc()).isTrue();
        verify(thongBaoRepository).save(notif1);

        // Mark again (idempotent)
        notificationService.markAsRead(101);
        verify(thongBaoRepository, times(1)).save(notif1); // save only called once
    }

    @Test
    void markAsRead_otherUser_throws403() {
        stubUserAuth(user2);
        when(thongBaoRepository.findById(101)).thenReturn(Optional.of(notif1));

        assertThatThrownBy(() -> notificationService.markAsRead(101))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("người dùng khác");
    }

    @Test
    void markAllAsRead_success() {
        stubUserAuth(user1);
        when(thongBaoRepository.findByNguoiDungMaNguoiDungAndDaDocFalse(10)).thenReturn(List.of(notif1));

        notificationService.markAllAsRead();

        assertThat(notif1.getDaDoc()).isTrue();
        verify(thongBaoRepository).saveAll(List.of(notif1));
    }

    @Test
    void getUnreadCount_returnsCount() {
        stubUserAuth(user1);
        when(thongBaoRepository.countByNguoiDungMaNguoiDungAndDaDocFalse(10)).thenReturn(3L);

        UnreadCountResponse res = notificationService.getUnreadCount();

        assertThat(res).isNotNull();
        assertThat(res.getUnreadCount()).isEqualTo(3L);
    }
}
