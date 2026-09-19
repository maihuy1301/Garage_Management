package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.AssignmentResponse;
import com.garage.dto.CreateAssignmentRequest;
import com.garage.dto.RejectAssignmentRequest;
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
 *   SYSTEM_ADMIN     → Toàn quyền xem, phân công, duyệt, từ chối, hủy phân công kỹ thuật viên toàn hệ thống
 *   BRANCH_MANAGER   → Toàn quyền xem, phân công, duyệt, từ chối, hủy phân công kỹ thuật viên thuộc chi nhánh mình
 *   RECEPTIONIST     → Tạo phân công (chờ duyệt) và xem phân công thuộc chi nhánh mình
 *   TECHNICIAN       → 403 Forbidden (sử dụng /api/technician/repair-orders)
 *   CUSTOMER         → 403 Forbidden
 */
@RestController
@RequestMapping("/api/repair-orders")
public class TechnicianAssignmentController {

    private final TechnicianAssignmentService technicianAssignmentService;

    public TechnicianAssignmentController(TechnicianAssignmentService technicianAssignmentService) {
        this.technicianAssignmentService = technicianAssignmentService;
    }

    /** GET /api/repair-orders/{repairOrderId}/assignments — Danh sách phân công kỹ thuật viên của phiếu sửa chữa */
    @GetMapping("/{repairOrderId}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAssignments(
            @PathVariable Integer repairOrderId) {
        List<AssignmentResponse> list = technicianAssignmentService.getAssignments(repairOrderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phân công thành công", list));
    }

    /** GET /api/repair-orders/assignments/pending — Danh sách phân công chờ duyệt (CHO_DUYET) cho MANAGER / ADMIN */
    @GetMapping("/assignments/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getPendingAssignments() {
        List<AssignmentResponse> list = technicianAssignmentService.getPendingAssignments();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phân công chờ duyệt thành công", list));
    }

    /** POST /api/repair-orders/{repairOrderId}/assignments — Phân công kỹ thuật viên cho phiếu sửa chữa */
    @PostMapping("/{repairOrderId}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @PathVariable Integer repairOrderId,
            @Valid @RequestBody CreateAssignmentRequest request) {
        AssignmentResponse response = technicianAssignmentService.createAssignment(repairOrderId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Phân công kỹ thuật viên thành công", response));
    }

    /** PUT /api/repair-orders/{repairOrderId}/assignments/{assignmentId}/approve — Manager duyệt phân công */
    @PutMapping("/{repairOrderId}/assignments/{assignmentId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> approveAssignment(
            @PathVariable Integer repairOrderId,
            @PathVariable Integer assignmentId) {
        AssignmentResponse response = technicianAssignmentService.approveAssignment(repairOrderId, assignmentId);
        return ResponseEntity.ok(ApiResponse.success("Duyệt phân công kỹ thuật viên thành công", response));
    }

    /** PUT /api/repair-orders/{repairOrderId}/assignments/{assignmentId}/reject — Manager từ chối phân công */
    @PutMapping("/{repairOrderId}/assignments/{assignmentId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> rejectAssignment(
            @PathVariable Integer repairOrderId,
            @PathVariable Integer assignmentId,
            @Valid @RequestBody(required = false) RejectAssignmentRequest request) {
        AssignmentResponse response = technicianAssignmentService.rejectAssignment(repairOrderId, assignmentId, request);
        return ResponseEntity.ok(ApiResponse.success("Từ chối phân công kỹ thuật viên thành công", response));
    }

    /** DELETE /api/repair-orders/{repairOrderId}/assignments/{assignmentId} — Hủy/xóa phân công kỹ thuật viên */
    @DeleteMapping("/{repairOrderId}/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @PathVariable Integer repairOrderId,
            @PathVariable Integer assignmentId) {
        technicianAssignmentService.deleteAssignment(repairOrderId, assignmentId);
        return ResponseEntity.ok(ApiResponse.success("Hủy phân công kỹ thuật viên thành công", null));
    }
}
