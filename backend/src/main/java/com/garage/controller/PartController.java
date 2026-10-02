package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.CreatePartRequest;
import com.garage.dto.PartResponse;
import com.garage.dto.UpdatePartRequest;
import com.garage.dto.UpdatePartStatusRequest;
import com.garage.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Parts Catalog REST Controller — /api/parts
 *
 * RBAC:
 *   - SYSTEM_ADMIN: Toàn quyền CRUD phụ tùng trong danh mục
 *   - BRANCH_MANAGER: Xem danh mục và bật/tắt trạng thái phụ tùng
 *   - RECEPTIONIST / TECHNICIAN: Xem danh mục phụ tùng
 *   - CUSTOMER: 403 Forbidden
 */
@RestController
@RequestMapping("/api/parts")
public class PartController {

    private final InventoryService inventoryService;

    public PartController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** GET /api/parts — Lấy danh mục phụ tùng */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<PartResponse>>> getAllParts(
            @RequestParam(required = false, defaultValue = "false") boolean onlyActive) {
        List<PartResponse> parts = inventoryService.getAllParts(onlyActive);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục phụ tùng thành công", parts));
    }

    /** GET /api/parts/{id} — Lấy chi tiết phụ tùng theo ID */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<PartResponse>> getPartById(@PathVariable Integer id) {
        PartResponse part = inventoryService.getPartById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phụ tùng thành công", part));
    }

    /** POST /api/parts — Tạo phụ tùng mới trong danh mục hệ thống (Admin only) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PartResponse>> createPart(
            @Valid @RequestBody CreatePartRequest request) {
        PartResponse response = inventoryService.createPart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phụ tùng mới thành công", response));
    }

    /** PUT /api/parts/{id} — Cập nhật thông tin phụ tùng (Admin only) */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PartResponse>> updatePart(
            @PathVariable Integer id,
            @Valid @RequestBody UpdatePartRequest request) {
        PartResponse response = inventoryService.updatePart(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin phụ tùng thành công", response));
    }

    /** PATCH /api/parts/{id}/status — Bật/Tắt trạng thái phụ tùng (Admin + Manager) */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PartResponse>> updatePartStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdatePartStatusRequest request) {
        PartResponse response = inventoryService.updatePartStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái phụ tùng thành công", response));
    }

    /** DELETE /api/parts/{id} — Xóa phụ tùng khỏi danh mục hệ thống (Admin only) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePart(@PathVariable Integer id) {
        inventoryService.deletePart(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa phụ tùng thành công", null));
    }
}
