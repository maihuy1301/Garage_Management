package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.*;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.QuotationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class QuotationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuotationService quotationService;

    @MockBean
    private NguoiDungRepository nguoiDungRepository;

    @MockBean
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    private NguoiDung mockUser(Integer id, String username) {
        NguoiDung u = new NguoiDung();
        u.setMaNguoiDung(id);
        u.setTenDangNhap(username);
        u.setHoTen(username + " FullName");
        u.setEmail(username + "@garage.com");
        u.setMatKhauHash("$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO");
        u.setMaPinHash("$2a$10$hashedPinValue");
        u.setTrangThai(true);
        return u;
    }

    private void stubUser(NguoiDung user, String roleName) {
        VaiTro role = new VaiTro();
        role.setMaVaiTro(user.getMaNguoiDung());
        role.setTenVaiTro(roleName);
        when(nguoiDungRepository.findByTenDangNhapOrEmail(user.getTenDangNhap(), user.getTenDangNhap()))
                .thenReturn(Optional.of(user));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()))
                .thenReturn(List.of(new NguoiDungVaiTro(user, role)));
    }

    private QuotationResponse sampleQuotation(Integer id, Integer orderId) {
        return new QuotationResponse(
                id, orderId, 1, "Chi Nhánh 1",
                "Phát sinh má phanh mòn", new BigDecimal("700000.00"),
                "CHO_KHACH_DUYET", LocalDateTime.now(), null,
                List.of(new QuotationServiceItemResponse(1, 10, "Bảo dưỡng phanh", new BigDecimal("200000.00"), new BigDecimal("200000.00"))),
                List.of(new QuotationPartItemResponse(2, 20, "PT002", "Má phanh", "Bộ", 1, new BigDecimal("500000.00"), new BigDecimal("500000.00")))
        );
    }

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_getQuotations_returns401() throws Exception {
        mockMvc.perform(get("/api/repair-orders/601/quotations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_createQuotation_returns401() throws Exception {
        mockMvc.perform(post("/api/repair-orders/601/quotations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. CUSTOMER (Read & Approve/Reject: 200, Create: 403)
    // ==========================================

    @Test
    void customer_createQuotation_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        CreateQuotationRequest req = new CreateQuotationRequest(
                "Khách tự tạo", List.of(new QuotationServiceItemRequest(10)), List.of()
        );

        mockMvc.perform(post("/api/repair-orders/601/quotations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void customer_getQuotations_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        when(quotationService.getQuotationsByRepairOrder(601)).thenReturn(List.of(sampleQuotation(801, 601)));

        mockMvc.perform(get("/api/repair-orders/601/quotations").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maBaoGia").value(801));
    }

    @Test
    void customer_approveQuotation_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        QuotationResponse approved = sampleQuotation(801, 601);
        approved.setTrangThai("DA_DUYET");
        when(quotationService.approveQuotation(801)).thenReturn(approved);

        mockMvc.perform(patch("/api/quotations/801/approve").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trangThai").value("DA_DUYET"));
    }

    @Test
    void customer_rejectQuotation_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        QuotationResponse rejected = sampleQuotation(801, 601);
        rejected.setTrangThai("TU_CHOI");
        when(quotationService.rejectQuotation(801)).thenReturn(rejected);

        mockMvc.perform(patch("/api/quotations/801/reject").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trangThai").value("TU_CHOI"));
    }

    // ==========================================
    // 3. BRANCH MANAGER (Create & Cancel: 200/201)
    // ==========================================

    @Test
    void branchManager_createQuotation_returns201() throws Exception {
        NguoiDung bm = mockUser(2, "manager");
        stubUser(bm, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        CreateQuotationRequest req = new CreateQuotationRequest(
                "Phát hiện mòn má phanh",
                List.of(new QuotationServiceItemRequest(10)),
                List.of(new QuotationPartItemRequest(20, 1))
        );

        when(quotationService.createQuotation(eq(601), any(CreateQuotationRequest.class)))
                .thenReturn(sampleQuotation(801, 601));

        mockMvc.perform(post("/api/repair-orders/601/quotations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.maBaoGia").value(801))
                .andExpect(jsonPath("$.data.tongTien").value(700000.00));
    }

    @Test
    void branchManager_cancelQuotation_returns200() throws Exception {
        NguoiDung bm = mockUser(2, "manager");
        stubUser(bm, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        QuotationResponse cancelled = sampleQuotation(801, 601);
        cancelled.setTrangThai("HUY");
        when(quotationService.cancelQuotation(801)).thenReturn(cancelled);

        mockMvc.perform(patch("/api/quotations/801/cancel").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trangThai").value("HUY"));
    }
}
