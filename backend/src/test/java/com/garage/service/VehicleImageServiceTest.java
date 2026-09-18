package com.garage.service;

import com.garage.dto.VehicleImageDownload;
import com.garage.dto.VehicleImageResponse;
import com.garage.entity.HinhAnhXe;
import com.garage.entity.Xe;
import com.garage.exception.BadRequestException;
import com.garage.repository.HinhAnhXeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VehicleImageServiceTest {

    @TempDir
    Path tempDir;

    private VehicleService vehicleService;
    private HinhAnhXeRepository imageRepository;
    private VehicleImageService imageService;
    private Xe vehicle;

    @BeforeEach
    void setUp() {
        vehicleService = mock(VehicleService.class);
        imageRepository = mock(HinhAnhXeRepository.class);
        imageService = new VehicleImageService(vehicleService, imageRepository, tempDir.toString());
        vehicle = new Xe();
        vehicle.setMaXe(42);
    }

    @Test
    void uploadOwnVehicleJpeg_savesMetadataAndFile() throws Exception {
        when(vehicleService.requireAccessibleVehicle(42)).thenReturn(vehicle);
        when(imageRepository.findByXeMaXe(42)).thenReturn(Optional.empty());
        when(imageRepository.save(any(HinhAnhXe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile file = new MockMultipartFile(
                "file", "garage.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
        );

        VehicleImageResponse response = imageService.upload(42, file);

        assertThat(response.getMaXe()).isEqualTo(42);
        assertThat(response.getLoaiNoiDung()).isEqualTo("image/jpeg");
        assertThat(Files.list(tempDir)).hasSize(1);
        ArgumentCaptor<HinhAnhXe> captor = ArgumentCaptor.forClass(HinhAnhXe.class);
        verify(imageRepository).save(captor.capture());
        assertThat(captor.getValue().getDuongDanAnh()).endsWith(".jpg");
    }

    @Test
    void uploadOtherCustomerVehicle_propagatesForbiddenBeforeWriting() {
        when(vehicleService.requireAccessibleVehicle(42))
                .thenThrow(new AccessDeniedException("Forbidden"));
        MockMultipartFile file = new MockMultipartFile(
                "file", "garage.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );

        assertThatThrownBy(() -> imageService.upload(42, file))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(imageRepository);
    }

    @Test
    void uploadFakeImage_rejectsByFileSignature() {
        when(vehicleService.requireAccessibleVehicle(42)).thenReturn(vehicle);
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.jpg", "image/jpeg", "not-an-image".getBytes()
        );

        assertThatThrownBy(() -> imageService.upload(42, file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("JPEG, PNG hoặc WebP");
        verify(imageRepository, never()).save(any());
    }

    @Test
    void downloadOwnVehicle_returnsStoredBytes() throws Exception {
        when(vehicleService.requireAccessibleVehicle(42)).thenReturn(vehicle);
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01};
        Path stored = tempDir.resolve("42-test.jpg");
        Files.write(stored, bytes);
        HinhAnhXe image = new HinhAnhXe();
        image.setXe(vehicle);
        image.setDuongDanAnh(stored.getFileName().toString());
        image.setTenTepGoc("garage.jpg");
        image.setLoaiNoiDung("image/jpeg");
        when(imageRepository.findByXeMaXe(42)).thenReturn(Optional.of(image));

        VehicleImageDownload result = imageService.download(42);

        assertThat(result.content()).isEqualTo(bytes);
        assertThat(result.contentType()).isEqualTo("image/jpeg");
    }
}
