package com.garage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.ServiceResponse;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupportBotServiceTest {
    ServiceCatalogService catalog = mock(ServiceCatalogService.class);
    SupportBotService bot = new SupportBotService(catalog,new ObjectMapper(),false,"","");
    @Test void unknownOrTechnicalQuestionHandsOffWithoutApiKey() { assertTrue(bot.answer("Xe kêu khi đánh lái là lỗi gì?").handoff()); }
    @Test void explicitHumanRequestOverridesServiceQuestion() { assertTrue(bot.answer("Tôi muốn gặp tiếp tân hỏi dịch vụ").handoff()); }
    @Test void bookingDoesNotClaimAnAppointmentWasCreated() { var a = bot.answer("Tôi muốn đặt lịch"); assertFalse(a.handoff()); assertTrue(a.text().contains("tiếp tân sẽ xác nhận")); }
    @Test void pricesComeFromCurrentCatalog() {
        var s = new ServiceResponse(); s.setTenDichVu("Bảo dưỡng"); s.setTongGiaDuKien(new BigDecimal("123456"));
        when(catalog.getAllServices(true,null)).thenReturn(List.of(s));
        var a = bot.answer("Bảng giá dịch vụ"); assertTrue(a.text().contains("123456")); assertFalse(a.handoff());
    }
    @Test void emptyCatalogDoesNotInventServices() { when(catalog.getAllServices(true,null)).thenReturn(List.of()); assertTrue(bot.answer("dịch vụ").handoff()); }
    @Test void enabledAiHandlesBookingInsteadOfCannedFaqAndReturnsDraft() throws Exception {
        var http = mock(java.net.http.HttpClient.class);
        @SuppressWarnings("unchecked") var response = (java.net.http.HttpResponse<String>) mock(java.net.http.HttpResponse.class);
        var mapper = new ObjectMapper().findAndRegisterModules();
        when(catalog.getAllServices(true,null)).thenReturn(List.of());
        when(response.statusCode()).thenReturn(200);
        String json = "{\"text\":\"Bạn muốn đặt lúc mấy giờ?\",\"handoff\":false,\"booking\":{\"vehicleId\":5,\"branchId\":1,\"appointmentAt\":null,\"serviceIds\":[],\"note\":\"Bảo dưỡng\"}}";
        when(response.body()).thenReturn(mapper.writeValueAsString(java.util.Map.of("candidates",List.of(java.util.Map.of("content",java.util.Map.of("parts",List.of(java.util.Map.of("text",json))))))));
        when(http.send(any(java.net.http.HttpRequest.class), org.mockito.ArgumentMatchers.<java.net.http.HttpResponse.BodyHandler<String>>any())).thenReturn(response);
        var ai = new SupportBotService(catalog,mapper,true,"test-key","test-model",http);
        var answer = ai.answer("Đặt lịch bảo dưỡng", "vehicleId=5 branchId=1", "");
        assertNotNull(answer.booking()); assertEquals(5,answer.booking().vehicleId());
        assertNull(answer.booking().appointmentAt()); assertFalse(answer.text().contains("bấm Đặt lịch"));
        verify(http).send(any(java.net.http.HttpRequest.class), org.mockito.ArgumentMatchers.<java.net.http.HttpResponse.BodyHandler<String>>any());
    }
}
