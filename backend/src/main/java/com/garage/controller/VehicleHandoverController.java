package com.garage.controller;

import com.garage.dto.*;
import com.garage.service.VehicleHandoverService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reception/{id}/handover")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FRONT_DESK')")
public class VehicleHandoverController {
    private final VehicleHandoverService handovers;
    public VehicleHandoverController(VehicleHandoverService handovers) { this.handovers = handovers; }
    @GetMapping
    public ApiResponse<HandoverResponse> getStatus(@PathVariable Integer id) {
        return ApiResponse.success("Thông tin bàn giao xe", handovers.getStatus(id));
    }
    @PostMapping
    public ApiResponse<HandoverResponse> handover(@PathVariable Integer id,
            @Valid @RequestBody(required = false) HandoverRequest request) {
        return ApiResponse.success("Đã bàn giao xe", handovers.handover(id, request));
    }
}
