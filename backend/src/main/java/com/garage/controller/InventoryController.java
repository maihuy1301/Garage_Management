package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.InventoryResponse;
import com.garage.security.BranchAuthorizationService;
import com.garage.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Branch Inventory REST Controller — /api/inventory
 *
 * RBAC:
 *   SYSTEM_ADMIN   → Xem tồn kho toàn bộ chi nhánh hoặc theo filter
 *   BRANCH_MANAGER → Xem tồn kho chi nhánh của mình (khác branch → 403)
 *   RECEPTIONIST   → Xem tồn kho chi nhánh của mình
 *   TECHNICIAN     → Xem tồn kho chi nhánh của mình
 *   CUSTOMER       → 403 Forbidden
 */
@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN')")
public class InventoryController {

    private final InventoryService inventoryService;
    private final BranchAuthorizationService branchAuthorizationService;

    public InventoryController(InventoryService inventoryService,
                               BranchAuthorizationService branchAuthorizationService) {
        this.inventoryService = inventoryService;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /** GET /api/inventory — Xem tồn kho theo chi nhánh */
    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventory(
            @RequestParam(required = false) Integer branchId) {
        List<InventoryResponse> list = inventoryService.getInventoryByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tồn kho thành công", list));
    }

    /** GET /api/inventory/{partId} — Xem chi tiết tồn kho phụ tùng tại chi nhánh */
    @GetMapping("/{partId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryDetail(
            @PathVariable Integer partId,
            @RequestParam(required = false, defaultValue = "1") Integer branchId) {
        InventoryResponse response = inventoryService.getInventoryDetail(branchId, partId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết tồn kho thành công", response));
    }
}
