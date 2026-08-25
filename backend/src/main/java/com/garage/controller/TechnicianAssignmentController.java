package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.AssignmentResponse;
import com.garage.dto.CreateAssignmentRequest;
import com.garage.service.TechnicianAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Technician Assignment REST Controller — /api/repair-orders/{repairOrderId}/assignments
 *
 * RBAC Matrix:
 *   SYSTEM_ADMIN     → Toàn quyền xem, phân công, hủy phân công kỹ thuật viên toàn hệ thống
 *   BRANCH_MANAGER   → Toàn quyền xem, phân công, hủy phân công kỹ thuật viên thuộc chi nhánh mình
 *   RECEPTIONIST     → 403 Forbidden
 *   TECHNICIAN       → 403 Forbidden
 *   CUSTOMER         → 403 Forbidden
 */
@RestController
@RequestMapping("/api/repair-orders/{repairOrderId}/assignments")
public class TechnicianAssignmentController {

    private final TechnicianAssignmentService technicianAssignmentService;

    public TechnicianAssignmentController(TechnicianAssignmentService technicianAssignmentService) {
        this.technicianAssignmentService = technicianAssignmentService;
    }

    /** GET /api/repair-orders/{repairOrderId}/assignments — Danh sách phân công kỹ thuật viên của phiếu sửa chữa */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAssignments(
            @PathVariable Integer repairOrderId) {
        List<AssignmentResponse> list = technicianAssignmentService.getAssignments(repairOrderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phân công thành công", list));
    }

    /** POST /api/repair-orders/{repairOrderId}/assignments — Phân công kỹ thuật viên cho phiếu sửa chữa */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @PathVariable Integer repairOrderId,
            @Valid @RequestBody CreateAssignmentRequest request) {
        AssignmentResponse response = technicianAssignmentService.createAssignment(repairOrderId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Phân công kỹ thuật viên thành công", response));
    }

    /** DELETE /api/repair-orders/{repairOrderId}/assignments/{assignmentId} — Hủy/xóa phân công kỹ thuật viên */
    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @PathVariable Integer repairOrderId,
            @PathVariable Integer assignmentId) {
        technicianAssignmentService.deleteAssignment(repairOrderId, assignmentId);
        return ResponseEntity.ok(ApiResponse.success("Hủy phân công kỹ thuật viên thành công", null));
    }
}
