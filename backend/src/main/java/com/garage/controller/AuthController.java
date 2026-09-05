package com.garage.controller;

import com.garage.dto.ApiResponse;
import com.garage.dto.CurrentUserResponse;
import com.garage.dto.LoginRequest;
import com.garage.dto.LoginResponse;
import com.garage.dto.RegisterRequest;
import com.garage.dto.RegisterResponse;
import com.garage.security.CustomUserDetails;
import com.garage.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = authService.registerCustomer(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký tài khoản thành công", registerResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getNguoiDung();
        boolean hasPin = user.getMaPinHash() != null && !user.getMaPinHash().isBlank();
        CurrentUserResponse userInfo = new CurrentUserResponse(
                user.getMaNguoiDung(),
                user.getTenDangNhap(),
                user.getHoTen(),
                user.getEmail(),
                hasPin
        );

        return ResponseEntity.ok(ApiResponse.success("Thông tin người dùng hiện tại", userInfo));
    }
}
