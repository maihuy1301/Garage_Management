package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.TechnicianExecutionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Technician Execution REST Controller — /api/technician/repair-orders
 *
 * RBAC:
 *   TECHNICIAN     → Chỉ truy cập & cập nhật các phiếu sửa chữa mà mình được phân công (PhanCong).
 *   SYSTEM_ADMIN   → Phân hệ riêng cho Kỹ thuật viên (hoặc dùng admin controller).
 *   BRANCH_MANAGER → Phân hệ riêng cho Kỹ thuật viên (hoặc dùng manager controller).
 *   RECEPTIONIST   → 403 Forbidden
 *   CUSTOMER       → 403 Forbidden
 */
@RestController
@RequestMapping("/api/technician/repair-orders")
@PreAuthorize("hasRole('TECHNICIAN')")
public class TechnicianExecutionController {

    private final TechnicianExecutionService technicianExecutionService;

    public TechnicianExecutionController(TechnicianExecutionService technicianExecutionService) {
        this.technicianExecutionService = technicianExecutionService;
    }

    /** GET /api/technician/repair-orders — Danh sách phiếu sửa chữa được phân công cho kỹ thuật viên */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RepairOrderResponse>>> getMyRepairOrders() {
        List<RepairOrderResponse> list = technicianExecutionService.getMyRepairOrders();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu sửa chữa được phân công thành công", list));
    }

    /** GET /api/technician/repair-orders/{repairOrderId} — Chi tiết phiếu sửa chữa được phân công */
    @GetMapping("/{repairOrderId}")
    public ResponseEntity<ApiResponse<RepairOrderResponse>> getRepairOrderDetail(
            @PathVariable Integer repairOrderId) {
        RepairOrderResponse response = technicianExecutionService.getRepairOrderDetail(repairOrderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phiếu sửa chữa thành công", response));
    }

    /** GET /api/technician/repair-orders/{repairOrderId}/items — Danh sách dịch vụ trong phiếu sửa chữa */
    @GetMapping("/{repairOrderId}/items")
    public ResponseEntity<ApiResponse<List<RepairItemResponse>>> getRepairOrderItems(
            @PathVariable Integer repairOrderId) {
        List<RepairItemResponse> list = technicianExecutionService.getRepairOrderItems(repairOrderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách dịch vụ thành công", list));
    }

    /** PATCH /api/technician/repair-orders/{repairOrderId}/progress — Cập nhật tiến độ thực hiện */
    @PatchMapping("/{repairOrderId}/progress")
    public ResponseEntity<ApiResponse<RepairProgressResponse>> updateProgress(
            @PathVariable Integer repairOrderId,
            @Valid @RequestBody UpdateRepairProgressRequest request) {
        RepairProgressResponse response = technicianExecutionService.updateProgress(repairOrderId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tiến độ sửa chữa thành công", response));
    }

    /** PATCH /api/technician/repair-orders/{repairOrderId}/items/{itemId} — Cập nhật trạng thái hạng mục dịch vụ */
    @PatchMapping("/{repairOrderId}/items/{itemId}")
    public ResponseEntity<ApiResponse<RepairItemResponse>> updateItemStatus(
            @PathVariable Integer repairOrderId,
            @PathVariable Integer itemId,
            @Valid @RequestBody UpdateServiceItemStatusRequest request) {
        RepairItemResponse response = technicianExecutionService.updateItemStatus(repairOrderId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái dịch vụ thành công", response));
    }

    /** GET /api/technician/repair-orders/{repairOrderId}/progress-history — Lịch sử tiến độ sửa chữa */
    @GetMapping("/{repairOrderId}/progress-history")
    public ResponseEntity<ApiResponse<List<RepairProgressResponse>>> getProgressHistory(
            @PathVariable Integer repairOrderId) {
        List<RepairProgressResponse> history = technicianExecutionService.getProgressHistory(repairOrderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử tiến độ thành công", history));
    }
}
