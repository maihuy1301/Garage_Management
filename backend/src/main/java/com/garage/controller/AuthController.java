package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.LoginRequest;
import com.garage.dto.LoginResponse;
import com.garage.security.CustomUserDetails;
import com.garage.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", loginResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("maNguoiDung", userDetails.getNguoiDung().getMaNguoiDung());
        userInfo.put("tenDangNhap", userDetails.getNguoiDung().getTenDangNhap());
        userInfo.put("hoTen", userDetails.getNguoiDung().getHoTen());
        userInfo.put("email", userDetails.getNguoiDung().getEmail());

        return ResponseEntity.ok(ApiResponse.success("Thông tin người dùng hiện tại", userInfo));
    }
}
