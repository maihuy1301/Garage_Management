package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.service.SupportBranchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support-chat/branch-suggestions")
@PreAuthorize("hasRole('CUSTOMER')")
public class SupportBranchController {
    public record Location(@NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
                           @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}
    private final SupportBranchService service;
    public SupportBranchController(SupportBranchService service) { this.service = service; }
    @GetMapping public ApiResponse<SupportBranchService.Result> suggestions() {
        return ApiResponse.success("Gợi ý chi nhánh", service.recommend(null, null));
    }
    // POST avoids putting precise GPS coordinates in URL/access logs. Coordinates are not persisted.
    @PostMapping public ApiResponse<SupportBranchService.Result> nearby(@Valid @RequestBody Location location) {
        return ApiResponse.success("Gợi ý theo vị trí", service.recommend(location.latitude(), location.longitude()));
    }
}
