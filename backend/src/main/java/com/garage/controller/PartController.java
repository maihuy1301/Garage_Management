package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.PartResponse;
import com.garage.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Parts Catalog REST Controller — /api/parts
 *
 * RBAC:
 *   SYSTEM_ADMIN / BRANCH_MANAGER / RECEPTIONIST / TECHNICIAN → Xem danh mục phụ tùng
 *   CUSTOMER → 403 Forbidden
 */
@RestController
@RequestMapping("/api/parts")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN')")
public class PartController {

    private final InventoryService inventoryService;

    public PartController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** GET /api/parts — Lấy danh mục phụ tùng đang hoạt động */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PartResponse>>> getAllParts() {
        List<PartResponse> parts = inventoryService.getAllParts();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục phụ tùng thành công", parts));
    }

    /** GET /api/parts/{id} — Lấy chi tiết phụ tùng theo ID */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartResponse>> getPartById(@PathVariable Integer id) {
        PartResponse part = inventoryService.getPartById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phụ tùng thành công", part));
    }
}
