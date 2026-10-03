package com.garage.controller;

import com.garage.dto.*;
import com.garage.dto.SupportChatDtos.*;
import com.garage.service.SupportChatService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api/support-chat")
@PreAuthorize("hasAnyRole('CUSTOMER','FRONT_DESK','MANAGER','ADMIN')")
public class SupportChatController {
    private final SupportChatService service;
    public SupportChatController(SupportChatService service) { this.service = service; }
    @GetMapping("/capabilities")
    public ApiResponse<Map<String, Boolean>> capabilities() { return ApiResponse.success("Chat hỗ trợ", Map.of("enabled", service.isEnabled())); }
    @GetMapping public ApiResponse<List<Conversation>> list() { return ApiResponse.success("Danh sách trò chuyện", service.list()); }
    @PostMapping @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<Conversation> open(@Valid @RequestBody Open request) { return ApiResponse.success("Cuộc trò chuyện", service.open(request.branchId())); }
    @GetMapping("/{id}/messages")
    public ApiResponse<History> history(@PathVariable Integer id, @RequestParam(required = false) Long before) { return ApiResponse.success("Tin nhắn", service.history(id, before)); }
    @PostMapping("/{id}/messages")
    public ApiResponse<Message> send(@PathVariable Integer id, @Valid @RequestBody Send request) { return ApiResponse.success("Đã gửi", service.send(id, request)); }
    @DeleteMapping("/{id}/messages")
    public ApiResponse<Conversation> clear(@PathVariable Integer id) { return ApiResponse.success("Đã xóa lịch sử trò chuyện", service.clear(id)); }
    @PostMapping("/{id}/handoff") @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<Conversation> handoff(@PathVariable Integer id) { return ApiResponse.success("Chờ tiếp tân", service.handoff(id)); }
    @PostMapping("/{id}/claim") @PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
    public ApiResponse<Conversation> claim(@PathVariable Integer id) { return ApiResponse.success("Đã nhận hỗ trợ", service.claim(id)); }
    @PostMapping("/{id}/resolve") @PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
    public ApiResponse<Conversation> resolve(@PathVariable Integer id) { return ApiResponse.success("Đã kết thúc hỗ trợ", service.resolve(id)); }
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> read(@PathVariable Integer id, @Valid @RequestBody Read request) { service.read(id, request.lastReadId()); return ApiResponse.success("Đã đọc", null); }
    @PostMapping("/{id}/appointments") @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<AppointmentResponse> book(@PathVariable Integer id,
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody CreateAppointmentRequest request) {
        if (!key.matches("[a-zA-Z0-9-]{8,80}")) throw new com.garage.exception.BadRequestException("Mã yêu cầu không hợp lệ");
        return ApiResponse.success("Đã gửi yêu cầu đặt lịch", service.book(id, key, request));
    }
}
