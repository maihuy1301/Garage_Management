package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.ServiceCategoryRequest;
import com.garage.dto.ServiceCategoryResponse;
import com.garage.service.ServiceCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Service Category REST Controller — /api/service-categories
 *
 * RBAC:
 *   - SYSTEM_ADMIN: Toàn quyền CRUD loại dịch vụ
 *   - BRANCH_MANAGER / RECEPTIONIST / TECHNICIAN / CUSTOMER: Xem danh mục loại dịch vụ
 */
@RestController
@RequestMapping("/api/service-categories")
public class ServiceCategoryController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceCategoryController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    /** GET /api/service-categories — Danh sách loại dịch vụ */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<ServiceCategoryResponse>>> getAllCategories(
            @RequestParam(required = false, defaultValue = "false") boolean onlyActive) {
        List<ServiceCategoryResponse> list = serviceCatalogService.getAllCategories(onlyActive);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách loại dịch vụ thành công", list));
    }

    /** GET /api/service-categories/{id} — Chi tiết loại dịch vụ */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<ServiceCategoryResponse>> getCategoryById(@PathVariable Integer id) {
        ServiceCategoryResponse response = serviceCatalogService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết loại dịch vụ thành công", response));
    }

    /** POST /api/service-categories — Tạo loại dịch vụ mới (Admin only) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceCategoryResponse>> createCategory(
            @Valid @RequestBody ServiceCategoryRequest request) {
        ServiceCategoryResponse response = serviceCatalogService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo loại dịch vụ thành công", response));
    }

    /** PUT /api/service-categories/{id} — Cập nhật loại dịch vụ (Admin only) */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceCategoryResponse>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody ServiceCategoryRequest request) {
        ServiceCategoryResponse response = serviceCatalogService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật loại dịch vụ thành công", response));
    }

    /** PATCH /api/service-categories/{id}/status — Bật/Tắt trạng thái loại dịch vụ (Admin only) */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceCategoryResponse>> updateCategoryStatus(
            @PathVariable Integer id,
            @RequestBody(required = false) com.garage.dto.UpdateCategoryStatusRequest request) {
        Boolean status = (request != null && request.getTrangThai() != null) ? request.getTrangThai() : true;
        ServiceCategoryResponse response = serviceCatalogService.updateCategoryStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái loại dịch vụ thành công", response));
    }

    /** DELETE /api/service-categories/{id} — Xóa loại dịch vụ (Admin only) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        serviceCatalogService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa loại dịch vụ thành công", null));
    }
}

