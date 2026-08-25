package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.CreateRepairPartRequest;
import com.garage.dto.RepairPartResponse;
import com.garage.dto.UpdateRepairPartRequest;
import com.garage.service.RepairPartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Repair Order Parts REST Controller — /api/repair-orders/{repairOrderId}/parts
 *
 * RBAC:
 *   SYSTEM_ADMIN   → Toàn quyền trên mọi phiếu sửa chữa
 *   BRANCH_MANAGER → Quản lý phụ tùng trong phiếu sửa chữa thuộc chi nhánh mình
 *   RECEPTIONIST   → Quản lý phụ tùng trong phiếu sửa chữa thuộc chi nhánh mình
 *   TECHNICIAN     → Quản lý phụ tùng trong phiếu sửa chữa mình được phân công
 *   CUSTOMER       → 403 Forbidden
 */
@RestController
@RequestMapping("/api/repair-orders/{repairOrderId}/parts")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN')")
public class RepairPartController {

    private final RepairPartService repairPartService;

    public RepairPartController(RepairPartService repairPartService) {
        this.repairPartService = repairPartService;
    }

    /** GET /api/repair-orders/{repairOrderId}/parts — Danh sách phụ tùng trong phiếu sửa chữa */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RepairPartResponse>>> getRepairOrderParts(
            @PathVariable Integer repairOrderId) {
        List<RepairPartResponse> list = repairPartService.getRepairOrderParts(repairOrderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phụ tùng sửa chữa thành công", list));
    }

    /** POST /api/repair-orders/{repairOrderId}/parts — Thêm phụ tùng vào phiếu sửa chữa (trừ tồn kho) */
    @PostMapping
    public ResponseEntity<ApiResponse<RepairPartResponse>> addPartToRepairOrder(
            @PathVariable Integer repairOrderId,
            @Valid @RequestBody CreateRepairPartRequest request) {
        RepairPartResponse response = repairPartService.addPartToRepairOrder(repairOrderId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm phụ tùng vào phiếu sửa chữa thành công", response));
    }

    /** PUT /api/repair-orders/{repairOrderId}/parts/{partDetailId} — Cập nhật số lượng phụ tùng (điều chỉnh tồn kho) */
    @PutMapping("/{partDetailId}")
    public ResponseEntity<ApiResponse<RepairPartResponse>> updatePartQuantity(
            @PathVariable Integer repairOrderId,
            @PathVariable Integer partDetailId,
            @Valid @RequestBody UpdateRepairPartRequest request) {
        RepairPartResponse response = repairPartService.updatePartQuantity(repairOrderId, partDetailId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật số lượng phụ tùng thành công", response));
    }

    /** DELETE /api/repair-orders/{repairOrderId}/parts/{partDetailId} — Xóa phụ tùng khỏi phiếu sửa chữa (hoàn trả tồn kho) */
    @DeleteMapping("/{partDetailId}")
    public ResponseEntity<ApiResponse<Void>> deletePartFromRepairOrder(
            @PathVariable Integer repairOrderId,
            @PathVariable Integer partDetailId) {
        repairPartService.deletePartFromRepairOrder(repairOrderId, partDetailId);
        return ResponseEntity.ok(ApiResponse.success("Xóa phụ tùng và hoàn trả tồn kho thành công", null));
    }
}
