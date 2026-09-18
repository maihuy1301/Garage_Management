package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.VehicleImageDownload;
import com.garage.dto.VehicleImageResponse;
import com.garage.service.VehicleImageService;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/vehicles/{vehicleId}/image")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CUSTOMER')")
public class VehicleImageController {

    private final VehicleImageService vehicleImageService;

    public VehicleImageController(VehicleImageService vehicleImageService) {
        this.vehicleImageService = vehicleImageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VehicleImageResponse>> upload(
            @PathVariable Integer vehicleId,
            @RequestPart("file") MultipartFile file) {
        VehicleImageResponse response = vehicleImageService.upload(vehicleId, file);
        return ResponseEntity.ok(ApiResponse.success("Tải ảnh xe thành công", response));
    }

    @GetMapping
    public ResponseEntity<byte[]> download(@PathVariable Integer vehicleId) {
        VehicleImageDownload image = vehicleImageService.download(vehicleId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.contentType()));
        headers.setContentDisposition(ContentDisposition.inline()
                .filename(image.fileName(), StandardCharsets.UTF_8)
                .build());
        headers.setCacheControl(CacheControl.noCache());
        return ResponseEntity.ok().headers(headers).body(image.content());
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer vehicleId) {
        vehicleImageService.delete(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Xóa ảnh xe thành công", null));
    }
}
