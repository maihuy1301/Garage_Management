package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.RegisterRequest;
import com.garage.dto.RegisterResponse;
import com.garage.dto.LoginRequest;
import com.garage.dto.LoginResponse;
import com.garage.security.JwtAccessDeniedHandler;
import com.garage.security.JwtAuthenticationEntryPoint;
import com.garage.security.CustomUserDetailsService;
import com.garage.security.JwtService;
import com.garage.security.SecurityConfig;
import com.garage.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class AuthRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    void login_UnauthenticatedValidRequest_ReturnsPinStateWithoutHashes() throws Exception {
        LoginRequest request = new LoginRequest("0901234567", "SecurePass123");
        LoginResponse response = new LoginResponse(
                "signed.jwt.token", 20, "0901234567", "Nguyễn Văn Khách", true
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasPin").value(true))
                .andExpect(jsonPath("$.data.matKhau").doesNotExist())
                .andExpect(jsonPath("$.data.matKhauHash").doesNotExist())
                .andExpect(jsonPath("$.data.maPinHash").doesNotExist());
    }

    @Test
    void register_UnauthenticatedValidRequest_Returns201() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Nguyễn Văn Khách", "0901234567", "customer@example.com", "SecurePass123", true
        );
        RegisterResponse response = new RegisterResponse(
                20, 12, "0901234567", "Nguyễn Văn Khách",
                "customer@example.com", "0901234567"
        );
        when(authService.registerCustomer(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tenDangNhap").value("0901234567"))
                .andExpect(jsonPath("$.data.maKhachHangCode").doesNotExist())
                .andExpect(jsonPath("$.data.matKhau").doesNotExist())
                .andExpect(jsonPath("$.data.matKhauHash").doesNotExist());
    }

    @Test
    void register_InvalidRequest_Returns400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "", "123", "not-an-email", "short", false
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.hoTen").exists())
                .andExpect(jsonPath("$.data.soDienThoai").exists())
                .andExpect(jsonPath("$.data.matKhau").exists())
                .andExpect(jsonPath("$.data.dongYDieuKhoan").exists());
    }
}
