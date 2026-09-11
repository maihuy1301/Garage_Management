package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.AppointmentResponse;
import com.garage.dto.CreateAppointmentRequest;
import com.garage.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Appointment REST Controller — /api/appointments
 *
 * RBAC Matrix:
 *   SYSTEM_ADMIN     → Toàn quyền xem (tất cả chi nhánh/khách hàng), tạo, hủy
 *   BRANCH_MANAGER   → Xem & hủy lịch hẹn thuộc chi nhánh mình
 *   RECEPTIONIST     → Xem & hủy lịch hẹn thuộc chi nhánh mình
 *   CUSTOMER         → Xem, tạo (cho xe của mình), hủy lịch hẹn của chính mình
 *   TECHNICIAN       → 403 Forbidden
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** GET /api/appointments — Danh sách lịch hẹn (phân quyền tự động trong service) */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointments() {
        List<AppointmentResponse> list = appointmentService.getAppointments();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lịch hẹn thành công", list));
    }

    /** GET /api/appointments/{id} — Chi tiết lịch hẹn */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable Integer id) {
        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lịch hẹn thành công", appointment));
    }

    /** POST /api/appointments — Đặt lịch hẹn mới */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse appointment = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt lịch hẹn thành công", appointment));
    }

    /** PATCH /api/appointments/{id}/cancel — Hủy lịch hẹn */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(@PathVariable Integer id) {
        AppointmentResponse appointment = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Hủy lịch hẹn thành công", appointment));
    }

    /** PATCH /api/appointments/{id}/confirm — Xác nhận lịch hẹn (DA_XAC_NHAN) */
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(@PathVariable Integer id) {
        AppointmentResponse appointment = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận lịch hẹn thành công", appointment));
    }

    /** PATCH /api/appointments/{id}/receive — Tiếp nhận xe từ lịch hẹn (DA_TIEP_NHAN) */
    @PatchMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> receiveAppointment(@PathVariable Integer id) {
        AppointmentResponse appointment = appointmentService.receiveAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Tiếp nhận xe từ lịch hẹn thành công", appointment));
    }

    /** PATCH /api/appointments/{id}/status — Cập nhật trạng thái lịch hẹn */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody com.garage.dto.UpdateAppointmentStatusRequest request) {
        AppointmentResponse appointment = appointmentService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái lịch hẹn thành công", appointment));
    }
}
