package com.garage.controller;

import com.garage.dto.VehicleImageDownload;
import com.garage.dto.VehicleImageResponse;
import com.garage.exception.GlobalExceptionHandler;
import com.garage.service.VehicleImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VehicleImageControllerTest {

    private VehicleImageService imageService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        imageService = mock(VehicleImageService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VehicleImageController(imageService))
                .setMessageConverters(
                        new org.springframework.http.converter.ByteArrayHttpMessageConverter(),
                        new org.springframework.http.converter.StringHttpMessageConverter(),
                        new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter()
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void uploadImage_returnsMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "car.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );
        when(imageService.upload(eq(42), any()))
                .thenReturn(new VehicleImageResponse(
                        42, "car.jpg", "image/jpeg", 3L, LocalDateTime.now()
                ));

        mockMvc.perform(multipart("/api/vehicles/42/image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maXe").value(42))
                .andExpect(jsonPath("$.data.loaiNoiDung").value("image/jpeg"));
    }

    @Test
    void downloadImage_returnsInlineBytes() throws Exception {
        byte[] content = new byte[]{1, 2, 3};
        when(imageService.download(42))
                .thenReturn(new VehicleImageDownload(content, "image/jpeg", "car.jpg"));

        mockMvc.perform(get("/api/vehicles/42/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(content))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")));
    }
}
