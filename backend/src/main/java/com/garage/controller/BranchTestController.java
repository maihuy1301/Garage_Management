package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.security.BranchAuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class BranchTestController {

    private final BranchAuthorizationService branchAuthorizationService;

    public BranchTestController(BranchAuthorizationService branchAuthorizationService) {
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Branch-scoped test endpoint.
     * SYSTEM_ADMIN → allowed for any branchId.
     * BRANCH_MANAGER / RECEPTIONIST / TECHNICIAN → allowed only for own branch.
     * CUSTOMER → denied (403 via RBAC).
     */
    @GetMapping("/branches/{branchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<String>> branchAccessTest(@PathVariable Integer branchId) {
        if (!branchAuthorizationService.isAllowedBranch(branchId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Forbidden: Bạn không có quyền truy cập chi nhánh " + branchId));
        }
        return ResponseEntity.ok(ApiResponse.success("Access granted for branch " + branchId, String.valueOf(branchId)));
    }
}
