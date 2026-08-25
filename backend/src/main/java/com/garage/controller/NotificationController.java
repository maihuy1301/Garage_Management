package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.NotificationResponse;
import com.garage.dto.UnreadCountResponse;
import com.garage.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Notification REST Controller — /api/notifications
 *
 * RBAC / Ownership:
 *   Mọi người dùng đã đăng nhập (SYSTEM_ADMIN, BRANCH_MANAGER, RECEPTIONIST, TECHNICIAN, CUSTOMER)
 *   đều có thể xem và quản lý thông báo của chính mình.
 */
@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** GET /api/notifications — Lấy danh sách thông báo của user hiện tại */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications() {
        List<NotificationResponse> list = notificationService.getUserNotifications();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", list));
    }

    /** GET /api/notifications/{id} — Xem chi tiết thông báo */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(@PathVariable Integer id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thông báo thành công", response));
    }

    /** PATCH /api/notifications/{id}/read — Đánh dấu thông báo là đã đọc */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Integer id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu đã đọc thành công", response));
    }

    /** PATCH /api/notifications/read-all — Đánh dấu tất cả thông báo là đã đọc */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả thông báo là đã đọc", null));
    }

    /** GET /api/notifications/unread-count — Đếm số thông báo chưa đọc */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        UnreadCountResponse response = notificationService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success("Lấy số thông báo chưa đọc thành công", response));
    }
}
