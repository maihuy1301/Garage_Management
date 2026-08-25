package com.garage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import com.garage.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

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

    private ConversationResponse sampleConversation(Integer id) {
        return new ConversationResponse(
                id, 1, "Nguyễn Văn Khách", 10, "Trần Quản Lý", null,
                null, "DANG_MO", LocalDateTime.now(), LocalDateTime.now(), 0, null
        );
    }

    private ChatMessageResponse sampleMessage(Integer id, Integer convId) {
        return new ChatMessageResponse(
                id, convId, 5, "customer", "Nguyễn Văn Khách", "Xin chào!",
                null, false, LocalDateTime.now()
        );
    }

    // ==========================================
    // 1. Unauthenticated (401)
    // ==========================================

    @Test
    void unauthenticated_getConversations_returns401() throws Exception {
        mockMvc.perform(get("/api/conversations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticated_sendMessage_returns401() throws Exception {
        SendMessageRequest req = new SendMessageRequest("Tin nhắn");
        mockMvc.perform(post("/api/conversations/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. Authenticated Customer (200 / 201)
    // ==========================================

    @Test
    void customer_getConversations_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        when(chatService.getUserConversations()).thenReturn(List.of(sampleConversation(101)));

        mockMvc.perform(get("/api/conversations").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maCuocHoiThoai").value(101));
    }

    @Test
    void customer_createConversation_returns201() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        CreateConversationRequest req = new CreateConversationRequest();
        req.setInitialMessage("Xin chào garage!");

        when(chatService.createConversation(any())).thenReturn(sampleConversation(101));

        mockMvc.perform(post("/api/conversations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.maCuocHoiThoai").value(101));
    }

    @Test
    void customer_sendMessage_returns201() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        SendMessageRequest req = new SendMessageRequest("Xe tôi đã xong chưa?");

        when(chatService.sendMessage(eq(101), any())).thenReturn(sampleMessage(501, 101));

        mockMvc.perform(post("/api/conversations/101/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.maTinNhan").value(501));
    }

    @Test
    void customer_sendMessage_blankContent_returns400() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        SendMessageRequest req = new SendMessageRequest("   "); // blank

        mockMvc.perform(post("/api/conversations/101/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void customer_markAsRead_returns200() throws Exception {
        NguoiDung cust = mockUser(5, "customer");
        stubUser(cust, "CUSTOMER");
        String token = jwtService.generateToken("customer", List.of("CUSTOMER"));

        mockMvc.perform(patch("/api/conversations/101/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(chatService).markConversationAsRead(101);
    }
}
