package com.garage.service;

import com.garage.dto.NotificationResponse;
import com.garage.dto.UnreadCountResponse;
import com.garage.entity.NguoiDung;
import com.garage.entity.ThongBao;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.ThongBaoRepository;
import com.garage.dto.RealtimeEvent;
import com.garage.websocket.WebSocketEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final ThongBaoRepository thongBaoRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final WebSocketEventPublisher webSocketEventPublisher;
    private final PushNotificationDispatcher pushDispatcher;

    public NotificationService(ThongBaoRepository thongBaoRepository,
                               NguoiDungRepository nguoiDungRepository,
                               WebSocketEventPublisher webSocketEventPublisher,
                               PushNotificationDispatcher pushDispatcher) {
        this.thongBaoRepository = thongBaoRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.webSocketEventPublisher = webSocketEventPublisher;
        this.pushDispatcher = pushDispatcher;
    }

    /**
     * Tạo thông báo mới cho người dùng và push realtime qua WebSocket
     */
    @Transactional
    public NotificationResponse sendNotification(NguoiDung recipient, String title, String content,
                                                 String type, Integer referenceId) {
        if (recipient == null) {
            return null;
        }

        ThongBao tb = new ThongBao();
        tb.setNguoiDung(recipient);
        tb.setTieuDe(title);
        tb.setNoiDung(content);
        tb.setLoaiThongBao(type);
        tb.setMaThamChieu(referenceId);
        tb.setDaDoc(false);

        ThongBao saved = thongBaoRepository.save(tb);
        NotificationResponse response = mapToResponse(saved);
        String account = recipient.getTenDangNhap();

        // Clients reload REST on this event, so publish only after the row commits.
        Runnable publish = () -> {
            try {
                RealtimeEvent event = RealtimeEvent.of(
                        type != null ? type : "NOTIFICATION_NEW",
                        "THONG_BAO",
                        saved.getMaThongBao(),
                        title,
                        response
                );
                webSocketEventPublisher.sendToUser(recipient.getTenDangNhap(), event);
            } catch (Exception ignored) {
                // Safe offline fallback: notification is already persisted in DB
            }
            try {
                pushDispatcher.send(account, response);
            } catch (Exception ignored) {
                // A full push queue must not fail a committed business operation.
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        } else {
            publish.run();
        }

        return response;
    }

    /**
     * Lấy danh sách thông báo của người dùng hiện tại (sắp xếp mới nhất trước)
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications() {
        NguoiDung currentUser = getCurrentAuthenticatedUser();
        return thongBaoRepository.findByNguoiDungMaNguoiDungOrderByNgayTaoDesc(currentUser.getMaNguoiDung())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết thông báo theo ID (kèm kiểm tra quyền sở hữu)
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Integer notificationId) {
        ThongBao tb = thongBaoRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo ID: " + notificationId));

        validateOwnership(tb);

        return mapToResponse(tb);
    }

    /**
     * Đánh dấu thông báo đã đọc (idempotent)
     */
    @Transactional
    public NotificationResponse markAsRead(Integer notificationId) {
        ThongBao tb = thongBaoRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo ID: " + notificationId));

        validateOwnership(tb);

        if (Boolean.FALSE.equals(tb.getDaDoc())) {
            tb.setDaDoc(true);
            tb = thongBaoRepository.save(tb);
        }

        return mapToResponse(tb);
    }

    /**
     * Đánh dấu tất cả thông báo chưa đọc của người dùng hiện tại thành đã đọc
     */
    @Transactional
    public void markAllAsRead() {
        NguoiDung currentUser = getCurrentAuthenticatedUser();
        List<ThongBao> unreadList = thongBaoRepository.findByNguoiDungMaNguoiDungAndDaDocFalse(currentUser.getMaNguoiDung());
        for (ThongBao tb : unreadList) {
            tb.setDaDoc(true);
        }
        thongBaoRepository.saveAll(unreadList);
    }

    /**
     * Đếm số lượng thông báo chưa đọc của người dùng hiện tại
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        NguoiDung currentUser = getCurrentAuthenticatedUser();
        long count = thongBaoRepository.countByNguoiDungMaNguoiDungAndDaDocFalse(currentUser.getMaNguoiDung());
        return new UnreadCountResponse(count);
    }

    // --- Helpers ---

    private void validateOwnership(ThongBao tb) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return; // ROLE_ADMIN can view
        }

        NguoiDung currentUser = getCurrentAuthenticatedUser();
        if (!tb.getNguoiDung().getMaNguoiDung().equals(currentUser.getMaNguoiDung())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập thông báo của người dùng khác");
        }
    }

    private NguoiDung getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Forbidden: Yêu cầu đăng nhập để truy cập thông báo");
        }

        return nguoiDungRepository.findByTenDangNhapOrEmail(auth.getName(), auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Forbidden: Không tìm thấy thông tin tài khoản người dùng"));
    }

    private NotificationResponse mapToResponse(ThongBao tb) {
        return new NotificationResponse(
                tb.getMaThongBao(),
                tb.getNguoiDung() != null ? tb.getNguoiDung().getMaNguoiDung() : null,
                tb.getTieuDe(),
                tb.getNoiDung(),
                tb.getLoaiThongBao(),
                tb.getMaThamChieu(),
                tb.getDaDoc(),
                tb.getNgayTao()
        );
    }
}
