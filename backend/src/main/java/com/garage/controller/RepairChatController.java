package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.RepairChatDtos.*;
import com.garage.service.RepairChatService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.Map;

@RestController
@RequestMapping("/api/repair-chat")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER','FRONT_DESK','TECHNICIAN')")
public class RepairChatController {
    private final RepairChatService service;
    public RepairChatController(RepairChatService service) { this.service = service; }

    @GetMapping("/capabilities")
    public ApiResponse<Map<String, Boolean>> capabilities() {
        return ApiResponse.success("Chat sửa chữa", Map.of("enabled", service.isEnabled()));
    }
    @GetMapping("/orders/{orderId}/messages")
    public ApiResponse<History> history(@PathVariable Integer orderId,
            @RequestParam(required = false) Long before, @RequestParam(required = false) Long after) {
        return ApiResponse.success("Trao đổi sửa chữa", service.history(orderId, before, after));
    }
    @GetMapping("/orders/{orderId}/vehicle-image")
    public ResponseEntity<byte[]> image(@PathVariable Integer orderId) {
        var image = service.image(orderId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(image.contentType())).body(image.content());
    }
    @PostMapping("/orders/{orderId}/messages")
    public ApiResponse<Message> send(@PathVariable Integer orderId, @Valid @RequestBody Send request) {
        return ApiResponse.success("Đã gửi tin nhắn", service.send(orderId, request));
    }
    @DeleteMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','FRONT_DESK')")
    public ApiResponse<Void> delete(@PathVariable Integer orderId) {
        service.delete(orderId); return ApiResponse.success("Đã xóa vĩnh viễn nội dung chat", null);
    }
}
