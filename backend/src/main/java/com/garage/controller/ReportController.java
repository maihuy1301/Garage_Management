package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Report & Statistics REST Controller — /api/reports
 *
 * RBAC:
 *   - SYSTEM_ADMIN: Toàn quyền truy cập tất cả báo cáo hệ thống và mọi chi nhánh.
 *   - BRANCH_MANAGER: Xem toàn bộ báo cáo thuộc chi nhánh của mình.
 *   - RECEPTIONIST: Xem báo cáo nghiệp vụ (dashboard, lịch hẹn, phiếu sửa chữa, dịch vụ).
 *   - TECHNICIAN: Xem báo cáo kỹ thuật viên.
 *   - CUSTOMER: Bị chặn (403 Forbidden).
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** GET /api/reports/dashboard — Dashboard tổng quan */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        DashboardResponse res = reportService.getDashboardReport(branchId, from, to);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu dashboard thành công", res));
    }

    /** GET /api/reports/revenue — Báo cáo doanh thu */
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenue(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        RevenueReportResponse res = reportService.getRevenueReport(branchId, from, to);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo doanh thu thành công", res));
    }

    /** GET /api/reports/appointments — Báo cáo lịch hẹn */
    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<AppointmentReportResponse>> getAppointments(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        AppointmentReportResponse res = reportService.getAppointmentReport(branchId, from, to);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo lịch hẹn thành công", res));
    }

    /** GET /api/reports/repair-orders — Báo cáo phiếu sửa chữa */
    @GetMapping("/repair-orders")
    public ResponseEntity<ApiResponse<RepairOrderReportResponse>> getRepairOrders(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        RepairOrderReportResponse res = reportService.getRepairOrderReport(branchId, from, to);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo phiếu sửa chữa thành công", res));
    }

    /** GET /api/reports/services — Báo cáo dịch vụ sử dụng */
    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<ServiceReportResponse>>> getServices(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<ServiceReportResponse> res = reportService.getServiceReport(branchId, from, to, limit);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo dịch vụ thành công", res));
    }

    /** GET /api/reports/parts — Báo cáo phụ tùng sử dụng */
    @GetMapping("/parts")
    public ResponseEntity<ApiResponse<List<PartReportResponse>>> getParts(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<PartReportResponse> res = reportService.getPartReport(branchId, from, to, limit);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo phụ tùng thành công", res));
    }

    /** GET /api/reports/inventory — Báo cáo tồn kho */
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<InventoryReportResponse>>> getInventory(
            @RequestParam(required = false) Integer branchId) {
        List<InventoryReportResponse> res = reportService.getInventoryReport(branchId);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo tồn kho thành công", res));
    }

    /** GET /api/reports/technicians — Báo cáo hiệu suất kỹ thuật viên */
    @GetMapping("/technicians")
    public ResponseEntity<ApiResponse<List<TechnicianReportResponse>>> getTechnicians(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<TechnicianReportResponse> res = reportService.getTechnicianReport(branchId, from, to);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo kỹ thuật viên thành công", res));
    }

    /** GET /api/reports/branches — Báo cáo so sánh chi nhánh */
    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<BranchReportResponse>>> getBranches(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<BranchReportResponse> res = reportService.getBranchReport(from, to);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo chi nhánh thành công", res));
    }
}
