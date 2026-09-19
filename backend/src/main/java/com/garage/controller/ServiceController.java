package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.ServiceResponse;
import com.garage.entity.DichVu;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.DichVuRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Catalog REST Controller — /api/services
 *
 * Cho phép xem danh mục dịch vụ đang hoạt động.
 * RBAC:
 *   ADMIN, MANAGER, FRONT_DESK, TECHNICIAN, CUSTOMER đều có thể xem danh mục dịch vụ.
 */
@RestController
@RequestMapping("/api/services")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
public class ServiceController {

    private final DichVuRepository dichVuRepository;

    public ServiceController(DichVuRepository dichVuRepository) {
        this.dichVuRepository = dichVuRepository;
    }

    /** GET /api/services — Lấy danh mục dịch vụ đang hoạt động */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getAllServices() {
        List<ServiceResponse> services = dichVuRepository.findByTrangThaiTrue().stream()
                .map(this::mapToServiceResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục dịch vụ thành công", services));
    }

    /** GET /api/services/{id} — Lấy chi tiết dịch vụ theo ID */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> getServiceById(@PathVariable Integer id) {
        DichVu dichVu = dichVuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + id));
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết dịch vụ thành công", mapToServiceResponse(dichVu)));
    }

    private ServiceResponse mapToServiceResponse(DichVu dv) {
        Integer maLoai = dv.getLoaiDichVu() != null ? dv.getLoaiDichVu().getMaLoaiDichVu() : null;
        String tenLoai = dv.getLoaiDichVu() != null ? dv.getLoaiDichVu().getTenLoai() : null;

        return new ServiceResponse(
                dv.getMaDichVu(),
                maLoai,
                tenLoai,
                dv.getTenDichVu(),
                dv.getMoTa(),
                dv.getDonGia(),
                dv.getThoiGianDuKien(),
                dv.getTrangThai()
        );
    }
}
