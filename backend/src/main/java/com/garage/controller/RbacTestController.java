package com.garage.controller;

import com.garage.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class RbacTestController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminTest() {
        return ResponseEntity.ok(ApiResponse.success("Access granted for ADMIN", "ADMIN_RESOURCE"));
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> managerTest() {
        return ResponseEntity.ok(ApiResponse.success("Access granted for MANAGER", "MANAGER_RESOURCE"));
    }

    @GetMapping("/receptionist")
    @PreAuthorize("hasRole('FRONT_DESK')")
    public ResponseEntity<ApiResponse<String>> receptionistTest() {
        return ResponseEntity.ok(ApiResponse.success("Access granted for FRONT_DESK", "FRONT_DESK_RESOURCE"));
    }

    @GetMapping("/technician")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<String>> technicianTest() {
        return ResponseEntity.ok(ApiResponse.success("Access granted for TECHNICIAN", "TECHNICIAN_RESOURCE"));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<String>> customerTest() {
        return ResponseEntity.ok(ApiResponse.success("Access granted for CUSTOMER", "CUSTOMER_RESOURCE"));
    }
}
