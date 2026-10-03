package com.garage.controller;

import com.garage.security.*;
import com.garage.service.SupportChatService;
import com.garage.service.SupportBranchService;
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

@WebMvcTest({SupportChatController.class, SupportBranchController.class})
@Import({SecurityConfig.class,JwtAuthenticationFilter.class,JwtAuthenticationEntryPoint.class,JwtAccessDeniedHandler.class})
class SupportChatControllerTest {
    @Autowired MockMvc mvc;
    @MockBean SupportChatService service;
    @MockBean SupportBranchService suggestions;
    @MockBean JwtService jwt;
    @MockBean CustomUserDetailsService userDetails;

    @Test void anonymousCannotRead() throws Exception { mvc.perform(get("/api/support-chat")).andExpect(status().isUnauthorized()); }
    @Test @WithMockUser(roles = "FRONT_DESK") void staffCannotReadPersonalSuggestions() throws Exception {
        mvc.perform(get("/api/support-chat/branch-suggestions")).andExpect(status().isForbidden());
        verifyNoInteractions(suggestions);
    }
    @Test @WithMockUser(roles = "CUSTOMER") void invalidLocationDoesNotReachService() throws Exception {
        mvc.perform(post("/api/support-chat/branch-suggestions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":91,\"longitude\":106}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/support-chat/branch-suggestions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":10}")).andExpect(status().isBadRequest());
        verifyNoInteractions(suggestions);
    }
    @Test @WithMockUser(roles = "CUSTOMER") void gpsRequestUsesValidatedCoordinates() throws Exception {
        when(suggestions.recommend(10.0,106.0)).thenReturn(new SupportBranchService.Result(java.util.List.of(),"Chưa có tọa độ"));
        mvc.perform(post("/api/support-chat/branch-suggestions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\":10,\"longitude\":106}")).andExpect(status().isOk());
        verify(suggestions).recommend(10.0,106.0);
    }
    @Test @WithMockUser(roles = "TECHNICIAN") void technicianCannotRead() throws Exception {
        mvc.perform(get("/api/support-chat")).andExpect(status().isForbidden()); verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "CUSTOMER") void customerCannotClaimOrResolve() throws Exception {
        mvc.perform(post("/api/support-chat/1/claim")).andExpect(status().isForbidden());
        mvc.perform(post("/api/support-chat/1/resolve")).andExpect(status().isForbidden()); verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "FRONT_DESK") void staffCannotOpenOrBookAsCustomer() throws Exception {
        mvc.perform(post("/api/support-chat").contentType(MediaType.APPLICATION_JSON).content("{\"branchId\":1}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/support-chat/1/handoff")).andExpect(status().isForbidden()); verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "CUSTOMER") void emptyMessageAndInvalidRetryKeyRejected() throws Exception {
        mvc.perform(post("/api/support-chat/1/messages").contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"   \",\"clientId\":\"request-1\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/support-chat/1/messages").contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"hello\",\"clientId\":\"?\"}")).andExpect(status().isBadRequest()); verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "CUSTOMER") void negativeReadCursorRejected() throws Exception {
        mvc.perform(patch("/api/support-chat/1/read").contentType(MediaType.APPLICATION_JSON).content("{\"lastReadId\":-1}")).andExpect(status().isBadRequest()); verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "CUSTOMER") void capabilitiesWorksBeforeMigration() throws Exception {
        when(service.isEnabled()).thenReturn(false);
        mvc.perform(get("/api/support-chat/capabilities")).andExpect(status().isOk()).andExpect(jsonPath("$.data.enabled").value(false));
    }
}
