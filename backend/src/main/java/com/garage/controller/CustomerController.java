package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentCustomerProfile() {
        CustomerResponse response = customerService.getCurrentCustomerProfile();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin cá nhân thành công", response));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCurrentCustomerProfile(@Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse response = customerService.updateCurrentCustomerProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        List<CustomerResponse> response = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khách hàng thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Integer id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo thông tin khách hàng thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(@PathVariable Integer id,
                                                                        @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin khách hàng thành công", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomerStatus(@PathVariable Integer id,
                                                                              @Valid @RequestBody UpdateCustomerStatusRequest request) {
        CustomerResponse response = customerService.updateCustomerStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái khách hàng thành công", response));
    }
}
