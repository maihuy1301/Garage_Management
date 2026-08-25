package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.CheckInRequest;
import com.garage.dto.ReceptionResponse;
import com.garage.service.ReceptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reception REST Controller — /api/reception
 *
 * RBAC Matrix:
 *   SYSTEM_ADMIN     → Toàn quyền check-in, xem danh sách và chi tiết phiếu tiếp nhận toàn hệ thống
 *   RECEPTIONIST     → Check-in, xem danh sách và chi tiết phiếu tiếp nhận thuộc chi nhánh mình
 *   BRANCH_MANAGER   → Xem danh sách và chi tiết phiếu tiếp nhận thuộc chi nhánh mình (và check-in nếu có phân công)
 *   TECHNICIAN       → 403 Forbidden
 *   CUSTOMER         → 403 Forbidden
 */
@RestController
@RequestMapping("/api/reception")
public class ReceptionController {

    private final ReceptionService receptionService;

    public ReceptionController(ReceptionService receptionService) {
        this.receptionService = receptionService;
    }

    /** POST /api/reception/check-in/{appointmentId} — Check-in tiếp nhận xe từ lịch hẹn */
    @PostMapping("/check-in/{appointmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<ReceptionResponse>> checkIn(
            @PathVariable Integer appointmentId,
            @Valid @RequestBody(required = false) CheckInRequest request) {
        ReceptionResponse response = receptionService.checkIn(appointmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tiếp nhận xe thành công", response));
    }

    /** GET /api/reception — Danh sách phiếu tiếp nhận */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<List<ReceptionResponse>>> getReceptionSlips() {
        List<ReceptionResponse> list = receptionService.getReceptionSlips();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu tiếp nhận thành công", list));
    }

    /** GET /api/reception/{id} — Chi tiết phiếu tiếp nhận */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<ReceptionResponse>> getReceptionSlipById(@PathVariable Integer id) {
        ReceptionResponse response = receptionService.getReceptionSlipById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phiếu tiếp nhận thành công", response));
    }
}
