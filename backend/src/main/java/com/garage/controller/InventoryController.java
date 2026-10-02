package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.InventoryResponse;
import com.garage.dto.StockImportRequest;
import com.garage.dto.StockTransactionResponse;
import com.garage.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Branch Inventory REST Controller — /api/inventory
 *
 * RBAC:
 *   - SYSTEM_ADMIN: Xem tồn kho mọi chi nhánh, nhập kho mọi chi nhánh, xem toàn bộ giao dịch
 *   - BRANCH_MANAGER: Xem tồn kho chi nhánh mình, nhập kho chi nhánh mình, xem giao dịch chi nhánh mình
 *   - RECEPTIONIST / TECHNICIAN: Xem tồn kho chi nhánh mình
 *   - CUSTOMER: 403 Forbidden
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** GET /api/inventory — Xem tồn kho theo chi nhánh */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventory(
            @RequestParam(required = false) Integer branchId) {
        List<InventoryResponse> list = inventoryService.getInventoryByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tồn kho thành công", list));
    }

    /** GET /api/inventory/{partId} — Xem chi tiết tồn kho phụ tùng tại chi nhánh */
    @GetMapping("/{partId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryDetail(
            @PathVariable Integer partId,
            @RequestParam(required = false, defaultValue = "1") Integer branchId) {
        InventoryResponse response = inventoryService.getInventoryDetail(branchId, partId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết tồn kho thành công", response));
    }

    /** POST /api/inventory/import — Nhập kho phụ tùng (Manager & Admin) */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> importStock(
            @Valid @RequestBody StockImportRequest request) {
        InventoryResponse response = inventoryService.importStock(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Nhập kho phụ tùng thành công", response));
    }

    /** GET /api/inventory/transactions — Lịch sử giao dịch kho (Manager & Admin) */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getStockTransactions(
            @RequestParam(required = false) Integer branchId) {
        List<StockTransactionResponse> list = inventoryService.getStockTransactions(branchId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử giao dịch kho thành công", list));
    }
}
