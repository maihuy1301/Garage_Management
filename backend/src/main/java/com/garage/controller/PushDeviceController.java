package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.PushTokenRequest;
import com.garage.service.PushDeviceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/devices")
@PreAuthorize("hasRole('CUSTOMER')")
public class PushDeviceController {
    private final PushDeviceService devices;
    public PushDeviceController(PushDeviceService devices) { this.devices = devices; }

    @PostMapping
    public ApiResponse<Void> register(@Valid @RequestBody PushTokenRequest request) {
        devices.register(request.token());
        return ApiResponse.success("Đã đăng ký thiết bị", null);
    }

    @DeleteMapping
    public ApiResponse<Void> unregister(@Valid @RequestBody PushTokenRequest request) {
        devices.unregister(request.token());
        return ApiResponse.success("Đã hủy đăng ký thiết bị", null);
    }
}
