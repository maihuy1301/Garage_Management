package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Chat / Realtime Conversation REST Controller — /api/conversations
 *
 * RBAC / Membership:
 *   - CUSTOMER: chỉ chat và xem các cuộc hội thoại của chính mình.
 *   - BRANCH_MANAGER / RECEPTIONIST / TECHNICIAN: chat và xem các cuộc hội thoại thuộc chi nhánh mình hoặc được phân công.
 *   - SYSTEM_ADMIN: toàn quyền quản lý hệ thống chat.
 */
@RestController
@RequestMapping("/api/conversations")
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** GET /api/conversations — Lấy danh sách cuộc hội thoại của user hiện tại */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getUserConversations() {
        List<ConversationResponse> list = chatService.getUserConversations();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cuộc hội thoại thành công", list));
    }

    /** POST /api/conversations — Tạo cuộc hội thoại mới */
    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(@RequestBody CreateConversationRequest req) {
        ConversationResponse response = chatService.createConversation(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo cuộc hội thoại thành công", response));
    }

    /** GET /api/conversations/{conversationId} — Xem chi tiết cuộc hội thoại */
    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversationById(@PathVariable Integer conversationId) {
        ConversationResponse response = chatService.getConversationById(conversationId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết cuộc hội thoại thành công", response));
    }

    /** GET /api/conversations/{conversationId}/messages — Lấy lịch sử tin nhắn */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getConversationMessages(@PathVariable Integer conversationId) {
        List<ChatMessageResponse> messages = chatService.getConversationMessages(conversationId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử tin nhắn thành công", messages));
    }

    /** POST /api/conversations/{conversationId}/messages — Gửi tin nhắn mới */
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(@PathVariable Integer conversationId,
                                                                        @Valid @RequestBody SendMessageRequest req) {
        ChatMessageResponse response = chatService.sendMessage(conversationId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gửi tin nhắn thành công", response));
    }

    /** PATCH /api/conversations/{conversationId}/read — Đánh dấu tin nhắn đã đọc */
    @PatchMapping("/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markConversationAsRead(@PathVariable Integer conversationId) {
        chatService.markConversationAsRead(conversationId);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã đọc", null));
    }
}
