package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.InvoiceService;
import com.garage.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Invoice & Payment REST Controller — /api/invoices & /api/repair-orders/{repairOrderId}/invoice
 *
 * RBAC:
 *   SYSTEM_ADMIN   → Toàn quyền trên mọi hóa đơn và thanh toán
 *   BRANCH_MANAGER → Quản lý hóa đơn & thanh toán thuộc chi nhánh mình
 *   RECEPTIONIST   → Tạo hóa đơn & thu ngân thanh toán thuộc chi nhánh mình
 *   TECHNICIAN     → Xem hóa đơn thuộc phiếu sửa chữa của mình
 *   CUSTOMER       → Xem hóa đơn & lịch sử thanh toán của chính mình
 */
@RestController
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public InvoiceController(InvoiceService invoiceService, PaymentService paymentService) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }

    /** GET /api/repair-orders/{repairOrderId}/invoice — Xem hóa đơn của phiếu sửa chữa */
    @GetMapping("/api/repair-orders/{repairOrderId}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByRepairOrder(
            @PathVariable Integer repairOrderId) {
        InvoiceResponse response = invoiceService.getInvoiceByRepairOrder(repairOrderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin hóa đơn thành công", response));
    }

    /** POST /api/repair-orders/{repairOrderId}/invoice — Tạo hóa đơn mới từ phiếu sửa chữa */
    @PostMapping("/api/repair-orders/{repairOrderId}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @PathVariable Integer repairOrderId,
            @Valid @RequestBody(required = false) CreateInvoiceRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(repairOrderId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo hóa đơn thành công", response));
    }

    /** GET /api/invoices/{invoiceId} — Xem chi tiết hóa đơn */
    @GetMapping("/api/invoices/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(
            @PathVariable Integer invoiceId) {
        InvoiceResponse response = invoiceService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết hóa đơn thành công", response));
    }

    /** GET /api/invoices — Danh sách hóa đơn (có thể lọc theo branchId) */
    @GetMapping("/api/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices(
            @RequestParam(required = false) Integer branchId) {
        List<InvoiceResponse> list = invoiceService.getInvoices(branchId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hóa đơn thành công", list));
    }

    /** POST /api/invoices/{invoiceId}/payments — Thực hiện thanh toán hóa đơn */
    @PostMapping("/api/invoices/{invoiceId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @PathVariable Integer invoiceId,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(invoiceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thực hiện thanh toán thành công", response));
    }

    /** GET /api/invoices/{invoiceId}/payments — Xem lịch sử thanh toán của hóa đơn */
    @GetMapping("/api/invoices/{invoiceId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByInvoice(
            @PathVariable Integer invoiceId) {
        List<PaymentResponse> list = paymentService.getPaymentsByInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử thanh toán thành công", list));
    }
}
