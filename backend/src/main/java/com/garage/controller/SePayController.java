package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.SePayService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class SePayController {
    private final SePayService service;
    public SePayController(SePayService service) { this.service = service; }

    @GetMapping("/api/invoices/{invoiceId}/payment-options")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','FRONT_DESK','CUSTOMER')")
    public ApiResponse<PaymentOptionsResponse> options(@PathVariable Integer invoiceId) {
        return ApiResponse.success("Thông tin thanh toán", service.options(invoiceId));
    }

    @PostMapping("/api/invoices/{invoiceId}/payment-sessions")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','FRONT_DESK','CUSTOMER')")
    public ApiResponse<PaymentSessionResponse> create(@PathVariable Integer invoiceId) {
        return ApiResponse.success("Phiên thanh toán", service.createSession(invoiceId));
    }

    @GetMapping("/api/invoices/{invoiceId}/payment-sessions/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','FRONT_DESK','CUSTOMER')")
    public ApiResponse<PaymentSessionResponse> status(@PathVariable Integer invoiceId, @PathVariable String sessionId) {
        return ApiResponse.success("Trạng thái thanh toán", service.getSession(invoiceId, sessionId));
    }

    @PostMapping("/api/payments/sepay/webhook")
    public ApiResponse<Void> webhook(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @Valid @RequestBody SePayWebhookRequest request) {
        service.authenticateWebhook(authorization);
        service.receiveWebhook(request);
        return ApiResponse.success("Đã nhận giao dịch", null);
    }
}
