package com.garage.controller;

import com.garage.dto.*;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private ReportService reportService;

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

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_getDashboard_returns401() throws Exception {
        mockMvc.perform(get("/api/reports/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. Customer Access (403)
    // ==========================================

    @Test
    void customer_getDashboard_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        mockMvc.perform(get("/api/reports/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 3. Admin Access (200)
    // ==========================================

    @Test
    void admin_getDashboard_returns200() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        DashboardResponse mockRes = new DashboardResponse(
                new BigDecimal("5000000.00"), 5, 4, 10, 8, 3, 4, 15, 20
        );
        when(reportService.getDashboardReport(any(), any(), any())).thenReturn(mockRes);

        mockMvc.perform(get("/api/reports/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRevenue").value(5000000.00))
                .andExpect(jsonPath("$.data.totalInvoices").value(5));
    }

    @Test
    void admin_getRevenue_returns200() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        RevenueReportResponse mockRes = new RevenueReportResponse(
                new BigDecimal("5000000.00"), 5, new BigDecimal("4500000.00"),
                new BigDecimal("500000.00"), List.of()
        );
        when(reportService.getRevenueReport(any(), any(), any())).thenReturn(mockRes);

        mockMvc.perform(get("/api/reports/revenue")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRevenue").value(5000000.00));
    }

    @Test
    void admin_getAppointments_returns200() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        AppointmentReportResponse mockRes = new AppointmentReportResponse(10, 2, 5, 2, 1);
        when(reportService.getAppointmentReport(any(), any(), any())).thenReturn(mockRes);

        mockMvc.perform(get("/api/reports/appointments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(10));
    }

    @Test
    void admin_getRepairOrders_returns200() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        RepairOrderReportResponse mockRes = new RepairOrderReportResponse(20, 2, 3, 5, 1, 1, 7, 1);
        when(reportService.getRepairOrderReport(any(), any(), any())).thenReturn(mockRes);

        mockMvc.perform(get("/api/reports/repair-orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(20));
    }

    @Test
    void admin_getInventory_returns200() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        InventoryReportResponse mockRes = new InventoryReportResponse(
                1, "PT001", "Lọc nhớt", new BigDecimal("100000"), 10, 2, new BigDecimal("1000000"), false
        );
        when(reportService.getInventoryReport(any())).thenReturn(List.of(mockRes));

        mockMvc.perform(get("/api/reports/inventory")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tenPhuTung").value("Lọc nhớt"));
    }

    @Test
    void admin_getBranches_returns200() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        BranchReportResponse mockRes = new BranchReportResponse(
                1, "Chi nhánh 1", new BigDecimal("2000000"), 5, 4, 3
        );
        when(reportService.getBranchReport(any(), any())).thenReturn(List.of(mockRes));

        mockMvc.perform(get("/api/reports/branches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].maChiNhanh").value(1));
    }
}
