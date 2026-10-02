package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.ServiceCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Service Catalog REST Controller — /api/services
 *
 * RBAC:
 *   - SYSTEM_ADMIN: Toàn quyền CRUD dịch vụ
 *   - BRANCH_MANAGER: Xem danh mục và cập nhật trạng thái (Bật/Tắt) dịch vụ
 *   - RECEPTIONIST, TECHNICIAN, CUSTOMER: Xem danh mục dịch vụ đang hoạt động
 */
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    /** GET /api/services — Lấy danh mục dịch vụ */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getAllServices(
            @RequestParam(required = false, defaultValue = "false") boolean onlyActive,
            @RequestParam(required = false) Integer categoryId) {
        List<ServiceResponse> services = serviceCatalogService.getAllServices(onlyActive, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục dịch vụ thành công", services));
    }

    /** GET /api/services/{id} — Lấy chi tiết dịch vụ theo ID */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<ServiceResponse>> getServiceById(@PathVariable Integer id) {
        ServiceResponse response = serviceCatalogService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết dịch vụ thành công", response));
    }

    /** POST /api/services — Tạo mới dịch vụ (Admin only) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(
            @Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = serviceCatalogService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo dịch vụ thành công", response));
    }

    /** PUT /api/services/{id} — Cập nhật thông tin dịch vụ (Admin only) */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateServiceRequest request) {
        ServiceResponse response = serviceCatalogService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin dịch vụ thành công", response));
    }

    /** PATCH /api/services/{id}/status — Bật/Tắt trạng thái dịch vụ (Admin + Manager) */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateServiceStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateServiceStatusRequest request) {
        ServiceResponse response = serviceCatalogService.updateServiceStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái dịch vụ thành công", response));
    }

    /** DELETE /api/services/{id} — Xóa dịch vụ (Admin only) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Integer id) {
        serviceCatalogService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa dịch vụ thành công", null));
    }
}
