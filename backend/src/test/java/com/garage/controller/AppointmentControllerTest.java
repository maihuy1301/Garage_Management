package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.AppointmentResponse;
import com.garage.dto.CreateAppointmentRequest;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

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
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

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

    private AppointmentResponse sampleAppointment(Integer id, Integer customerId, Integer branchId, String status) {
        return new AppointmentResponse(
                id, customerId, "Khách Hàng " + customerId, "090000000" + customerId,
                100, "51A-11111", "Toyota", "Camry",
                branchId, "CN00" + branchId, "Chi Nhánh " + branchId,
                LocalDateTime.now().plusDays(1), status, "Ghi chú test", LocalDateTime.now()
        );
    }

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_getAppointments_returns401() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_getAppointmentById_returns401() throws Exception {
        mockMvc.perform(get("/api/appointments/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_createAppointment_returns401() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. TECHNICIAN Role Denied (403)
    // ==========================================

    @Test
    void technician_getAppointments_returns403() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        mockMvc.perform(get("/api/appointments").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void technician_createAppointment_returns403() throws Exception {
        NguoiDung tech = mockUser(4, "technician");
        stubUser(tech, "ROLE_TECHNICIAN");
        String token = jwtService.generateToken("technician", List.of("ROLE_TECHNICIAN"));

        CreateAppointmentRequest req = new CreateAppointmentRequest(100, 1, LocalDateTime.now().plusDays(1), "Test");

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 3. ROLE_CUSTOMER: List, View Own, View Other (403), Create, Cancel
    // ==========================================

    @Test
    void customer_getAppointments_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        when(appointmentService.getAppointments()).thenReturn(List.of(sampleAppointment(1, 1, 1, "CHO_XAC_NHAN")));

        mockMvc.perform(get("/api/appointments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void customer_getOwnAppointment_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        when(appointmentService.getAppointmentById(1)).thenReturn(sampleAppointment(1, 1, 1, "CHO_XAC_NHAN"));

        mockMvc.perform(get("/api/appointments/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maDatLich").value(1));
    }

    @Test
    void customer_getOtherCustomerAppointment_returns403() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        when(appointmentService.getAppointmentById(2))
                .thenThrow(new AccessDeniedException("Forbidden: Bạn không có quyền xem lịch hẹn của khách hàng khác"));

        mockMvc.perform(get("/api/appointments/2").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void customer_createAppointment_returns201() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        CreateAppointmentRequest req = new CreateAppointmentRequest(100, 1, LocalDateTime.now().plusDays(1), "Đặt lịch");
        when(appointmentService.createAppointment(any(CreateAppointmentRequest.class)))
                .thenReturn(sampleAppointment(10, 1, 1, "CHO_XAC_NHAN"));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maDatLich").value(10));
    }

    @Test
    void customer_cancelOwnAppointment_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "ROLE_CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("ROLE_CUSTOMER"));

        when(appointmentService.cancelAppointment(1)).thenReturn(sampleAppointment(1, 1, 1, "HUY"));

        mockMvc.perform(patch("/api/appointments/1/cancel").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trangThai").value("HUY"));
    }

    // ==========================================
    // 4. ROLE_MANAGER & ROLE_FRONT_DESK
    // ==========================================

    @Test
    void branchManager_getAppointments_returns200() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(appointmentService.getAppointments()).thenReturn(List.of(sampleAppointment(1, 1, 1, "CHO_XAC_NHAN")));

        mockMvc.perform(get("/api/appointments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void receptionist_getAppointments_returns200() throws Exception {
        NguoiDung rec = mockUser(3, "receptionist");
        stubUser(rec, "ROLE_FRONT_DESK");
        String token = jwtService.generateToken("receptionist", List.of("ROLE_FRONT_DESK"));

        when(appointmentService.getAppointments()).thenReturn(List.of(sampleAppointment(1, 1, 1, "CHO_XAC_NHAN")));

        mockMvc.perform(get("/api/appointments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void branchManager_getOtherBranchAppointment_returns403() throws Exception {
        NguoiDung mgr = mockUser(2, "manager");
        stubUser(mgr, "ROLE_MANAGER");
        String token = jwtService.generateToken("manager", List.of("ROLE_MANAGER"));

        when(appointmentService.getAppointmentById(2))
                .thenThrow(new AccessDeniedException("Forbidden: Bạn không có quyền truy cập lịch hẹn của chi nhánh khác"));

        mockMvc.perform(get("/api/appointments/2").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 5. ROLE_ADMIN: Global Access
    // ==========================================

    @Test
    void admin_getAppointments_returns200() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        when(appointmentService.getAppointments()).thenReturn(List.of(
                sampleAppointment(1, 1, 1, "CHO_XAC_NHAN"),
                sampleAppointment(2, 2, 2, "CHO_XAC_NHAN")
        ));

        mockMvc.perform(get("/api/appointments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void appointmentNotFound_returns404() throws Exception {
        NguoiDung admin = mockUser(1, "admin");
        stubUser(admin, "ROLE_ADMIN");
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"));

        when(appointmentService.getAppointmentById(999))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: 999"));

        mockMvc.perform(get("/api/appointments/999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
