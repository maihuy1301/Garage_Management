package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.RepairOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Repair Order REST Controller — /api/repair-orders
 *
 * RBAC Matrix:
 *   SYSTEM_ADMIN     → Toàn quyền tạo, xem, cập nhật thông tin và trạng thái toàn hệ thống
 *   BRANCH_MANAGER   → Tạo, xem, cập nhật thông tin và trạng thái thuộc chi nhánh mình
 *   RECEPTIONIST     → Tạo và xem phiếu sửa chữa thuộc chi nhánh mình
 *   TECHNICIAN       → 403 Forbidden
 *   CUSTOMER         → 403 Forbidden
 */
@RestController
@RequestMapping("/api/repair-orders")
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    public RepairOrderController(RepairOrderService repairOrderService) {
        this.repairOrderService = repairOrderService;
    }

    /** POST /api/repair-orders — Tạo phiếu sửa chữa từ phiếu tiếp nhận */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<RepairOrderResponse>> createRepairOrder(
            @Valid @RequestBody CreateRepairOrderRequest request) {
        RepairOrderResponse response = repairOrderService.createRepairOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phiếu sửa chữa thành công", response));
    }

    /** GET /api/repair-orders — Danh sách phiếu sửa chữa */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<List<RepairOrderResponse>>> getRepairOrders() {
        List<RepairOrderResponse> list = repairOrderService.getRepairOrders();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu sửa chữa thành công", list));
    }

    /** GET /api/repair-orders/{id} — Chi tiết phiếu sửa chữa */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<RepairOrderResponse>> getRepairOrderById(@PathVariable Integer id) {
        RepairOrderResponse response = repairOrderService.getRepairOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phiếu sửa chữa thành công", response));
    }

    /** PUT /api/repair-orders/{id} — Cập nhật thông tin phiếu sửa chữa */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<RepairOrderResponse>> updateRepairOrder(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateRepairOrderRequest request) {
        RepairOrderResponse response = repairOrderService.updateRepairOrder(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phiếu sửa chữa thành công", response));
    }

    /** PATCH /api/repair-orders/{id}/status — Cập nhật trạng thái phiếu sửa chữa */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<RepairOrderResponse>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateRepairOrderStatusRequest request) {
        RepairOrderResponse response = repairOrderService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái phiếu sửa chữa thành công", response));
    }
}
