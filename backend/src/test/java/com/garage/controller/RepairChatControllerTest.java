package com.garage.controller;

import com.garage.security.*;
import com.garage.service.RepairChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(RepairChatController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
class RepairChatControllerTest {
    @Autowired MockMvc mvc;
    @MockBean RepairChatService service;
    @MockBean JwtService jwt;
    @MockBean CustomUserDetailsService users;
    @Test void anonymousDenied() throws Exception {
        mvc.perform(get("/api/repair-chat/orders/1/messages")).andExpect(status().isUnauthorized());
    }
    @Test @WithMockUser(roles = "CUSTOMER") void customerDeniedForAllChatResources() throws Exception {
        mvc.perform(get("/api/repair-chat/orders/1/messages")).andExpect(status().isForbidden());
        mvc.perform(get("/api/repair-chat/orders/1/vehicle-image")).andExpect(status().isForbidden());
        mvc.perform(get("/api/repair-chat/capabilities")).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "TECHNICIAN") void technicianCannotDelete() throws Exception {
        mvc.perform(delete("/api/repair-chat/orders/1")).andExpect(status().isForbidden()); verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "MANAGER") void managerCannotDelete() throws Exception {
        mvc.perform(delete("/api/repair-chat/orders/1")).andExpect(status().isForbidden()); verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "FRONT_DESK") void frontDeskCanRequestDelete() throws Exception {
        mvc.perform(delete("/api/repair-chat/orders/1")).andExpect(status().isOk()); verify(service).delete(1);
    }
    @Test @WithMockUser(roles = "TECHNICIAN") void validatesTextAndRetryKey() throws Exception {
        mvc.perform(post("/api/repair-chat/orders/1/messages").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"   \",\"clientId\":\"request_123\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/repair-chat/orders/1/messages").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Chào\",\"clientId\":\"?\"}")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "TECHNICIAN") void capabilitiesDoesNotNeedNewTables() throws Exception {
        mvc.perform(get("/api/repair-chat/capabilities")).andExpect(status().isOk()).andExpect(jsonPath("$.data.enabled").value(false));
        verify(service).isEnabled(); verifyNoMoreInteractions(service);
    }
}
