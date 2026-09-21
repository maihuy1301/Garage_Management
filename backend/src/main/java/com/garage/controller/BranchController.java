package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        List<BranchResponse> response = branchService.getAllBranches(includeInactive);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chi nhánh thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Integer id) {
        BranchResponse response = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi nhánh thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        BranchResponse created = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo chi nhánh thành công", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(@PathVariable Integer id,
                                                                    @Valid @RequestBody UpdateBranchRequest request) {
        BranchResponse updated = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin chi nhánh thành công", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranchStatus(@PathVariable Integer id,
                                                                          @Valid @RequestBody UpdateBranchStatusRequest request) {
        BranchResponse updated = branchService.updateBranchStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái chi nhánh thành công", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Integer id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa chi nhánh thành công", null));
    }
}
