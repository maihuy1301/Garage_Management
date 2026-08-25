package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.CreateQuotationRequest;
import com.garage.dto.QuotationResponse;
import com.garage.service.QuotationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Additional Quotation REST Controller — /api/quotations & /api/repair-orders/{repairOrderId}/quotations
 *
 * RBAC:
 *   SYSTEM_ADMIN   → Toàn quyền trên mọi báo giá
 *   BRANCH_MANAGER → Quản lý báo giá thuộc chi nhánh mình
 *   RECEPTIONIST   → Quản lý báo giá thuộc chi nhánh mình
 *   TECHNICIAN     → Tạo & xem báo giá thuộc phiếu sửa chữa được phân công
 *   CUSTOMER       → Xem & Duyệt/Từ chối báo giá thuộc xe/phiếu sửa chữa của mình
 */
@RestController
public class QuotationController {

    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    /** GET /api/repair-orders/{repairOrderId}/quotations — Xem danh sách báo giá của phiếu sửa chữa */
    @GetMapping("/api/repair-orders/{repairOrderId}/quotations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<QuotationResponse>>> getQuotationsByRepairOrder(
            @PathVariable Integer repairOrderId) {
        List<QuotationResponse> list = quotationService.getQuotationsByRepairOrder(repairOrderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách báo giá phát sinh thành công", list));
    }

    /** POST /api/repair-orders/{repairOrderId}/quotations — Tạo báo giá phát sinh mới */
    @PostMapping("/api/repair-orders/{repairOrderId}/quotations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<QuotationResponse>> createQuotation(
            @PathVariable Integer repairOrderId,
            @Valid @RequestBody CreateQuotationRequest request) {
        QuotationResponse response = quotationService.createQuotation(repairOrderId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo báo giá phát sinh thành công", response));
    }

    /** GET /api/quotations/{quotationId} — Xem chi tiết báo giá */
    @GetMapping("/api/quotations/{quotationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<QuotationResponse>> getQuotationById(
            @PathVariable Integer quotationId) {
        QuotationResponse response = quotationService.getQuotationById(quotationId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết báo giá thành công", response));
    }

    /** PATCH /api/quotations/{quotationId}/approve — Khách hàng duyệt báo giá phát sinh */
    @PatchMapping("/api/quotations/{quotationId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<QuotationResponse>> approveQuotation(
            @PathVariable Integer quotationId) {
        QuotationResponse response = quotationService.approveQuotation(quotationId);
        return ResponseEntity.ok(ApiResponse.success("Duyệt báo giá phát sinh thành công", response));
    }

    /** PATCH /api/quotations/{quotationId}/reject — Khách hàng từ chối báo giá phát sinh */
    @PatchMapping("/api/quotations/{quotationId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<QuotationResponse>> rejectQuotation(
            @PathVariable Integer quotationId) {
        QuotationResponse response = quotationService.rejectQuotation(quotationId);
        return ResponseEntity.ok(ApiResponse.success("Từ chối báo giá phát sinh thành công", response));
    }

    /** PATCH /api/quotations/{quotationId}/cancel — Quản lý/nhân viên hủy báo giá phát sinh */
    @PatchMapping("/api/quotations/{quotationId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<QuotationResponse>> cancelQuotation(
            @PathVariable Integer quotationId) {
        QuotationResponse response = quotationService.cancelQuotation(quotationId);
        return ResponseEntity.ok(ApiResponse.success("Hủy báo giá phát sinh thành công", response));
    }
}
