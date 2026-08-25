package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhân viên thành công", employees));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Integer id) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhân viên thành công", employee));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse createdEmployee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo nhân viên thành công", createdEmployee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(@PathVariable Integer id,
                                                                        @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse updatedEmployee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin nhân viên thành công", updatedEmployee));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployeeStatus(@PathVariable Integer id,
                                                                              @Valid @RequestBody UpdateEmployeeStatusRequest request) {
        EmployeeResponse updatedEmployee = employeeService.updateEmployeeStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái nhân viên thành công", updatedEmployee));
    }
}
