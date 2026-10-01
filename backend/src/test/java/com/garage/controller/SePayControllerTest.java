package com.garage.controller;

import com.garage.service.SePayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SePayControllerTest {
    @Autowired MockMvc mvc;
    @MockBean SePayService service;
    static final String PAYLOAD = """
        {"id":99,"gateway":"TestBank","accountNumber":"000000","code":"GARABC",
        "content":"GARABC","transferType":"in","transferAmount":100,"referenceCode":"REF"}
        """;
    @Test void anonymousCannotCreateSession() throws Exception {
        mvc.perform(post("/api/invoices/1/payment-sessions")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "TECHNICIAN") void technicianCannotCreateOrRead() throws Exception {
        mvc.perform(post("/api/invoices/1/payment-sessions")).andExpect(status().isForbidden());
        mvc.perform(get("/api/invoices/1/payment-options")).andExpect(status().isForbidden());
        mvc.perform(get("/api/invoices/1/payment-sessions/a")).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test @WithMockUser(roles = "CUSTOMER") void customerReachesOwnershipCheckedService() throws Exception {
        mvc.perform(post("/api/invoices/1/payment-sessions")).andExpect(status().isOk());
        verify(service).createSession(1);
    }
    @Test @WithMockUser(roles = "CUSTOMER") void customerCannotUseStaffPaymentEndpoint() throws Exception {
        mvc.perform(post("/api/invoices/1/payments").contentType("application/json")
                .content("{\"soTien\":100,\"phuongThuc\":\"TIEN_MAT\"}")).andExpect(status().isForbidden());
    }
    @Test void webhookHasSeparateAuthentication() throws Exception {
        doThrow(new AccessDeniedException("Denied")).when(service).authenticateWebhook(null);
        mvc.perform(post("/api/payments/sepay/webhook").contentType("application/json").content(PAYLOAD))
                .andExpect(status().isForbidden());
        verify(service, never()).receiveWebhook(any());
    }
    @Test void authenticatedProviderReceivesSuccessEnvelope() throws Exception {
        mvc.perform(post("/api/payments/sepay/webhook").header("Authorization","Apikey test-only")
                .contentType("application/json").content(PAYLOAD)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        var order = inOrder(service); order.verify(service).authenticateWebhook("Apikey test-only"); order.verify(service).receiveWebhook(any());
    }
    @Test void invalidAmountDoesNotReachService() throws Exception {
        mvc.perform(post("/api/payments/sepay/webhook").contentType("application/json").content(PAYLOAD.replace(":100", ":-1")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
