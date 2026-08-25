package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.CreateRepairItemRequest;
import com.garage.dto.RepairItemResponse;
import com.garage.dto.UpdateRepairItemRequest;
import com.garage.service.RepairItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Repair Items REST Controller — /api/repair-orders/{repairOrderId}/items
 *
 * RBAC Matrix:
 *   SYSTEM_ADMIN     → Toàn quyền xem, thêm, sửa, xóa hạng mục dịch vụ toàn hệ thống
 *   BRANCH_MANAGER   → Toàn quyền xem, thêm, sửa, xóa hạng mục dịch vụ thuộc chi nhánh mình
 *   RECEPTIONIST     → Xem và thêm hạng mục dịch vụ thuộc chi nhánh mình
 *   TECHNICIAN       → 403 Forbidden
 *   CUSTOMER         → 403 Forbidden
 */
@RestController
@RequestMapping("/api/repair-orders/{repairOrderId}/items")
public class RepairItemController {

    private final RepairItemService repairItemService;

    public RepairItemController(RepairItemService repairItemService) {
        this.repairItemService = repairItemService;
    }

    /** GET /api/repair-orders/{repairOrderId}/items — Danh sách dịch vụ trong phiếu sửa chữa */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<List<RepairItemResponse>>> getRepairItems(
            @PathVariable Integer repairOrderId) {
        List<RepairItemResponse> list = repairItemService.getRepairItems(repairOrderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách dịch vụ thành công", list));
    }

    /** POST /api/repair-orders/{repairOrderId}/items — Thêm dịch vụ vào phiếu sửa chữa */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<RepairItemResponse>> addRepairItem(
            @PathVariable Integer repairOrderId,
            @Valid @RequestBody CreateRepairItemRequest request) {
        RepairItemResponse response = repairItemService.addRepairItem(repairOrderId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm dịch vụ vào phiếu sửa chữa thành công", response));
    }

    /** PUT /api/repair-orders/{repairOrderId}/items/{itemId} — Cập nhật hạng mục dịch vụ */
    @PutMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<RepairItemResponse>> updateRepairItem(
            @PathVariable Integer repairOrderId,
            @PathVariable Integer itemId,
            @Valid @RequestBody UpdateRepairItemRequest request) {
        RepairItemResponse response = repairItemService.updateRepairItem(repairOrderId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật dịch vụ thành công", response));
    }

    /** DELETE /api/repair-orders/{repairOrderId}/items/{itemId} — Xóa dịch vụ khỏi phiếu sửa chữa */
    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteRepairItem(
            @PathVariable Integer repairOrderId,
            @PathVariable Integer itemId) {
        repairItemService.deleteRepairItem(repairOrderId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Xóa dịch vụ khỏi phiếu sửa chữa thành công", null));
    }
}
