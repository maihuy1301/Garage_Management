package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.BranchResponse;
import com.garage.service.BranchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches() {
        List<BranchResponse> response = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chi nhánh thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Integer id) {
        BranchResponse response = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi nhánh thành công", response));
    }
}
