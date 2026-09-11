package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.KhachHang;
import com.garage.entity.ModelXe;
import com.garage.entity.NguoiDung;
import com.garage.entity.Xe;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.HangXeRepository;
import com.garage.repository.KhachHangRepository;
import com.garage.repository.ModelXeRepository;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.XeRepository;
import com.garage.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final XeRepository xeRepository;
    private final KhachHangRepository khachHangRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final HangXeRepository hangXeRepository;
    private final ModelXeRepository modelXeRepository;

    public VehicleService(XeRepository xeRepository,
                          KhachHangRepository khachHangRepository,
                          NguoiDungRepository nguoiDungRepository,
                          HangXeRepository hangXeRepository,
                          ModelXeRepository modelXeRepository) {
        this.xeRepository = xeRepository;
        this.khachHangRepository = khachHangRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.hangXeRepository = hangXeRepository;
        this.modelXeRepository = modelXeRepository;
    }

    // =========================================================
    // CUSTOMER — chỉ thấy xe của chính mình
    // =========================================================

    /**
     * Lấy danh sách xe thuộc customer đang đăng nhập.
     * SYSTEM_ADMIN → toàn bộ xe.
     * CUSTOMER → chỉ xe của chính mình.
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehicles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isSystemAdmin(auth)) {
            return xeRepository.findAll().stream()
                    .map(this::mapToVehicleResponse)
                    .collect(Collectors.toList());
        }

        // CUSTOMER — filter theo owner
        KhachHang customer = getAuthenticatedCustomer(auth);
        return xeRepository.findByKhachHangMaKhachHang(customer.getMaKhachHang()).stream()
                    .map(this::mapToVehicleResponse)
                    .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết xe.
     * SYSTEM_ADMIN → bất kỳ xe hợp lệ.
     * CUSTOMER → chỉ xe của chính mình (403 nếu sai owner).
     */
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isSystemAdmin(auth)) {
            Xe xe = xeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + id));
            return mapToVehicleResponse(xe);
        }

        KhachHang customer = getAuthenticatedCustomer(auth);
        // Query trực tiếp theo owner — không thể leak xe của customer khác
        Xe xe = xeRepository.findByMaXeAndKhachHangMaKhachHang(id, customer.getMaKhachHang())
                .orElseThrow(() -> {
                    // Kiểm tra xe có tồn tại không để trả đúng 404 vs 403
                    if (xeRepository.existsById(id)) {
                        return new AccessDeniedException("Forbidden: Bạn không có quyền truy cập xe này");
                    }
                    return new ResourceNotFoundException("Không tìm thấy xe với ID: " + id);
                });
        return mapToVehicleResponse(xe);
    }

    /**
     * Tạo xe mới.
     * Validate bắt buộc ModelXe tồn tại và ModelXe.MaHangXe == request.maHangXe.
     */
    @Transactional
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        KhachHang owner;
        if (isSystemAdmin(auth)) {
            if (request.getMaKhachHang() == null) {
                throw new BadRequestException("ADMIN phải cung cấp maKhachHang khi tạo xe");
            }
            owner = khachHangRepository.findById(request.getMaKhachHang())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + request.getMaKhachHang()));
        } else {
            owner = getAuthenticatedCustomer(auth);
        }

        // Validate Brand & Model relationship
        ModelXe modelXe = validateBrandAndModel(request.getMaHangXe(), request.getMaModel());

        validateBienSoUnique(request.getBienSo(), null);
        if (request.getSoVIN() != null && !request.getSoVIN().isBlank()) {
            validateSoVINUnique(request.getSoVIN(), null);
        }

        Xe xe = new Xe();
        xe.setKhachHang(owner);
        xe.setBienSo(request.getBienSo().trim());
        xe.setModelXe(modelXe);
        xe.setNamSanXuat(request.getNamSanXuat());
        xe.setMauXe(request.getMauXe());
        xe.setSoVIN(request.getSoVIN());
        xe.setSoKmHienTai(request.getSoKmHienTai() != null ? request.getSoKmHienTai() : 0);
        xe.setTrangThai(true);

        Xe saved = xeRepository.save(xe);
        return mapToVehicleResponse(saved);
    }

    /**
     * Cập nhật xe.
     * SYSTEM_ADMIN → bất kỳ xe.
     * CUSTOMER → chỉ xe của chính mình.
     * Không cho phép thay đổi ownership.
     */
    @Transactional
    public VehicleResponse updateVehicle(Integer id, UpdateVehicleRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Xe xe;
        if (isSystemAdmin(auth)) {
            xe = xeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + id));
        } else {
            KhachHang customer = getAuthenticatedCustomer(auth);
            xe = xeRepository.findByMaXeAndKhachHangMaKhachHang(id, customer.getMaKhachHang())
                    .orElseThrow(() -> {
                        if (xeRepository.existsById(id)) {
                            return new AccessDeniedException("Forbidden: Bạn không có quyền cập nhật xe này");
                        }
                        return new ResourceNotFoundException("Không tìm thấy xe với ID: " + id);
                    });
        }

        applyVehicleUpdate(xe, request);
        Xe updated = xeRepository.save(xe);
        return mapToVehicleResponse(updated);
    }

    /**
     * Xóa xe.
     * SYSTEM_ADMIN → bất kỳ xe (nếu không có business dependency).
     * CUSTOMER → chỉ xe của chính mình (nếu không có business dependency).
     */
    @Transactional
    public void deleteVehicle(Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Xe xe;
        if (isSystemAdmin(auth)) {
            xe = xeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + id));
        } else {
            KhachHang customer = getAuthenticatedCustomer(auth);
            xe = xeRepository.findByMaXeAndKhachHangMaKhachHang(id, customer.getMaKhachHang())
                    .orElseThrow(() -> {
                        if (xeRepository.existsById(id)) {
                            return new AccessDeniedException("Forbidden: Bạn không có quyền xóa xe này");
                        }
                        return new ResourceNotFoundException("Không tìm thấy xe với ID: " + id);
                    });
        }

        try {
            xeRepository.delete(xe);
        } catch (Exception e) {
            throw new BadRequestException("Không thể xóa xe này vì đang được sử dụng trong lịch hẹn hoặc phiếu sửa chữa");
        }
    }

    // =========================================================
    // Private Helpers
    // =========================================================

    private ModelXe validateBrandAndModel(Integer maHangXe, Integer maModel) {
        if (maModel == null) {
            throw new BadRequestException("Mã Model xe không được để trống");
        }
        ModelXe model = modelXeRepository.findById(maModel)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy model xe với ID: " + maModel));

        if (maHangXe != null) {
            if (model.getHangXe() == null || !model.getHangXe().getMaHangXe().equals(maHangXe)) {
                throw new BadRequestException("Model xe ID " + maModel + " ('" + model.getTenModel() + "') không thuộc hãng xe ID " + maHangXe);
            }
        }
        return model;
    }

    private KhachHang getAuthenticatedCustomer(Authentication auth) {
        NguoiDung user = getAuthenticatedUser(auth);
        return khachHangRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hồ sơ khách hàng cho tài khoản hiện tại. " +
                        "Tài khoản này chưa được liên kết với khách hàng."));
    }

    private NguoiDung getAuthenticatedUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Yêu cầu xác thực tài khoản");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getNguoiDung();
        }
        String username = auth.getName();
        return nguoiDungRepository.findByTenDangNhapOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản: " + username));
    }

    private boolean isSystemAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> "ROLE_ADMIN".equals(r) || "ROLE_MANAGER".equals(r));
    }

    private void validateBienSoUnique(String bienSo, Integer excludeId) {
        if (xeRepository.existsByBienSo(bienSo)) {
            throw new DuplicateResourceException("Biển số xe '" + bienSo + "' đã tồn tại trong hệ thống");
        }
    }

    private void validateSoVINUnique(String soVIN, Integer excludeId) {
        if (xeRepository.existsBySoVIN(soVIN)) {
            throw new DuplicateResourceException("Số VIN '" + soVIN + "' đã tồn tại trong hệ thống");
        }
    }

    private void applyVehicleUpdate(Xe xe, UpdateVehicleRequest request) {
        if (request.getMaModel() != null) {
            ModelXe modelXe = validateBrandAndModel(request.getMaHangXe(), request.getMaModel());
            xe.setModelXe(modelXe);
        }
        if (request.getNamSanXuat() != null) xe.setNamSanXuat(request.getNamSanXuat());
        if (request.getMauXe() != null) xe.setMauXe(request.getMauXe());
        if (request.getSoVIN() != null) xe.setSoVIN(request.getSoVIN());
        if (request.getSoKmHienTai() != null) xe.setSoKmHienTai(request.getSoKmHienTai());
    }

    private VehicleResponse mapToVehicleResponse(Xe xe) {
        KhachHang kh = xe.getKhachHang();
        String tenChuXe = null;
        if (kh != null && kh.getNguoiDung() != null) {
            tenChuXe = kh.getNguoiDung().getHoTen();
        }

        Integer maHangXe = null;
        String tenHangXe = null;
        Integer maModel = null;
        String tenModel = null;

        if (xe.getModelXe() != null) {
            maModel = xe.getModelXe().getMaModel();
            tenModel = xe.getModelXe().getTenModel();
            if (xe.getModelXe().getHangXe() != null) {
                maHangXe = xe.getModelXe().getHangXe().getMaHangXe();
                tenHangXe = xe.getModelXe().getHangXe().getTenHangXe();
            }
        }

        return new VehicleResponse(
                xe.getMaXe(),
                kh != null ? kh.getMaKhachHang() : null,
                tenChuXe,
                xe.getBienSo(),
                maHangXe,
                tenHangXe,
                maModel,
                tenModel,
                xe.getNamSanXuat(),
                xe.getMauXe(),
                xe.getSoVIN(),
                xe.getSoKmHienTai(),
                xe.getTrangThai(),
                xe.getNgayTao()
        );
    }
}
