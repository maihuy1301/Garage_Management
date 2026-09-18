package com.garage.service;

import com.garage.dto.VehicleImageDownload;
import com.garage.dto.VehicleImageResponse;
import com.garage.entity.HinhAnhXe;
import com.garage.entity.Xe;
import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.HinhAnhXeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class VehicleImageService {

    static final long MAX_IMAGE_SIZE = 8L * 1024 * 1024;

    private final VehicleService vehicleService;
    private final HinhAnhXeRepository hinhAnhXeRepository;
    private final Path storageRoot;

    public VehicleImageService(VehicleService vehicleService,
                               HinhAnhXeRepository hinhAnhXeRepository,
                               @Value("${storage.vehicle-images-dir:uploads/vehicles}") String storageDir) {
        this.vehicleService = vehicleService;
        this.hinhAnhXeRepository = hinhAnhXeRepository;
        this.storageRoot = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    @Transactional
    public VehicleImageResponse upload(Integer vehicleId, MultipartFile file) {
        Xe vehicle = vehicleService.requireAccessibleVehicle(vehicleId);
        byte[] content = readAndValidate(file);
        String contentType = detectContentType(content);
        String extension = extensionFor(contentType);
        String originalName = sanitizeFileName(file.getOriginalFilename(), extension);
        String storedName = vehicleId + "-" + UUID.randomUUID() + "." + extension;
        Path target = storageRoot.resolve(storedName).normalize();

        if (!target.startsWith(storageRoot)) {
            throw new BadRequestException("Đường dẫn ảnh xe không hợp lệ");
        }

        Optional<HinhAnhXe> current = hinhAnhXeRepository.findByXeMaXe(vehicleId);
        String previousPath = current.map(HinhAnhXe::getDuongDanAnh).orElse(null);

        try {
            Files.createDirectories(storageRoot);
            Files.write(target, content, StandardOpenOption.CREATE_NEW);

            HinhAnhXe image = current.orElseGet(HinhAnhXe::new);
            image.setXe(vehicle);
            image.setDuongDanAnh(storedName);
            image.setTenTepGoc(originalName);
            image.setLoaiNoiDung(contentType);
            image.setKichThuoc((long) content.length);
            image.setNgayCapNhat(LocalDateTime.now());
            HinhAnhXe saved = hinhAnhXeRepository.save(image);

            if (previousPath != null && !previousPath.equals(storedName)) {
                deleteStoredFile(previousPath);
            }
            return toResponse(saved);
        } catch (IOException ex) {
            deleteStoredFile(storedName);
            throw new BadRequestException("Không thể lưu ảnh xe. Vui lòng thử lại.");
        } catch (RuntimeException ex) {
            deleteStoredFile(storedName);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public VehicleImageDownload download(Integer vehicleId) {
        vehicleService.requireAccessibleVehicle(vehicleId);
        HinhAnhXe image = hinhAnhXeRepository.findByXeMaXe(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Xe chưa có ảnh hồ sơ"));
        Path imagePath = storageRoot.resolve(image.getDuongDanAnh()).normalize();
        if (!imagePath.startsWith(storageRoot) || !Files.isRegularFile(imagePath)) {
            throw new ResourceNotFoundException("Không tìm thấy tệp ảnh xe");
        }
        try {
            return new VehicleImageDownload(
                    Files.readAllBytes(imagePath),
                    image.getLoaiNoiDung(),
                    image.getTenTepGoc()
            );
        } catch (IOException ex) {
            throw new ResourceNotFoundException("Không thể đọc tệp ảnh xe");
        }
    }

    @Transactional
    public void delete(Integer vehicleId) {
        vehicleService.requireAccessibleVehicle(vehicleId);
        HinhAnhXe image = hinhAnhXeRepository.findByXeMaXe(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Xe chưa có ảnh hồ sơ"));
        hinhAnhXeRepository.delete(image);
        deleteStoredFile(image.getDuongDanAnh());
    }

    private byte[] readAndValidate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ảnh cần tải lên");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Ảnh xe không được vượt quá 8 MB");
        }
        try {
            byte[] content = file.getBytes();
            detectContentType(content);
            return content;
        } catch (IOException ex) {
            throw new BadRequestException("Không thể đọc tệp ảnh đã chọn");
        }
    }

    private String detectContentType(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A) {
            return "image/png";
        }
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "image/webp";
        }
        throw new BadRequestException("Chỉ hỗ trợ ảnh JPEG, PNG hoặc WebP");
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private String sanitizeFileName(String originalName, String extension) {
        if (originalName == null || originalName.isBlank()) {
            return "anh-xe." + extension;
        }
        String name = Paths.get(originalName).getFileName().toString().trim();
        name = name.replaceAll("[\\r\\n]", "_");
        if (name.length() > 255) {
            name = name.substring(name.length() - 255);
        }
        return name.toLowerCase(Locale.ROOT).endsWith("." + extension)
                ? name
                : name + "." + extension;
    }

    private void deleteStoredFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        Path path = storageRoot.resolve(relativePath).normalize();
        if (!path.startsWith(storageRoot)) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Metadata remains authoritative; orphan cleanup can run separately.
        }
    }

    private VehicleImageResponse toResponse(HinhAnhXe image) {
        return new VehicleImageResponse(
                image.getXe().getMaXe(),
                image.getTenTepGoc(),
                image.getLoaiNoiDung(),
                image.getKichThuoc(),
                image.getNgayCapNhat()
        );
    }
}
