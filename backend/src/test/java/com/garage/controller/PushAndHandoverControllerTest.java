package com.garage.controller;

import com.garage.dto.HandoverResponse;
import com.garage.service.PushDeviceService;
import com.garage.service.VehicleHandoverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {PushDeviceController.class, VehicleHandoverController.class,
        PushAndHandoverControllerTest.Security.class})
class PushAndHandoverControllerTest {
    @Autowired MockMvc mvc;
    @MockBean PushDeviceService devices;
    @MockBean VehicleHandoverService handovers;

    // Isolated MVC security tests: no datasource, Firebase credentials or real JWT required.
    @TestConfiguration
    @EnableMethodSecurity
    static class Security {
        @Bean SecurityFilterChain chain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(errors -> errors.authenticationEntryPoint(
                            (request, response, exception) -> response.sendError(401)))
                    .build();
        }
    }

    @Test void anonymousRequestsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/reception/1/handover")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/notifications/devices").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"device:token\"}")).andExpect(status().isUnauthorized());
        verifyNoInteractions(devices, handovers);
    }

    @Test void customerCanRegisterAndRemoveDevice() throws Exception {
        mvc.perform(post("/api/notifications/devices").with(user("customer").roles("CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"device:token\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mvc.perform(delete("/api/notifications/devices").with(user("customer").roles("CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"device:token\"}"))
                .andExpect(status().isOk());
        verify(devices).register("device:token");
        verify(devices).unregister("device:token");
    }

    @Test void staffCannotRegisterCustomerDevice() throws Exception {
        mvc.perform(post("/api/notifications/devices").with(user("manager").roles("MANAGER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"device:token\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(devices);
    }

    @Test void invalidTokenIsRejectedBeforeService() throws Exception {
        for (String token : new String[]{"", "has spaces", "x".repeat(2049)}) {
            mvc.perform(post("/api/notifications/devices").with(user("customer").roles("CUSTOMER"))
                    .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"" + token + "\"}"))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(devices);
    }

    @Test void customerAndTechnicianCannotHandover() throws Exception {
        for (String role : new String[]{"CUSTOMER", "TECHNICIAN"}) {
            mvc.perform(get("/api/reception/1/handover").with(user("user").roles(role)))
                    .andExpect(status().isForbidden());
            mvc.perform(post("/api/reception/1/handover").with(user("user").roles(role)))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(handovers);
    }

    @Test void staffCanReadStatusAndConfirmWithOptionalBody() throws Exception {
        when(handovers.getStatus(1)).thenReturn(new HandoverResponse(true, null, null, null, null));
        for (String role : new String[]{"ADMIN", "MANAGER", "FRONT_DESK"}) {
            mvc.perform(get("/api/reception/1/handover").with(user("staff").roles(role)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data.eligible").value(true));
        }
        mvc.perform(post("/api/reception/1/handover").with(user("staff").roles("FRONT_DESK")))
                .andExpect(status().isOk());
        verify(handovers).handover(1, null);
    }

    @Test void oversizedNoteIsRejectedBeforeService() throws Exception {
        mvc.perform(post("/api/reception/1/handover").with(user("staff").roles("FRONT_DESK"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"ghiChu\":\"" + "x".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(handovers);
    }
}
