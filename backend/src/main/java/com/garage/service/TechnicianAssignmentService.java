package com.garage.service;

import com.garage.dto.AssignmentResponse;
import com.garage.dto.CreateAssignmentRequest;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.NhanVienRepository;
import com.garage.repository.PhanCongRepository;
import com.garage.repository.PhieuSuaChuaRepository;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicianAssignmentService {

    private final PhanCongRepository phanCongRepository;
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public TechnicianAssignmentService(PhanCongRepository phanCongRepository,
                                       PhieuSuaChuaRepository phieuSuaChuaRepository,
                                       NhanVienRepository nhanVienRepository,
                                       NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
                                       BranchAuthorizationService branchAuthorizationService) {
        this.phanCongRepository = phanCongRepository;
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Lấy danh sách phân công kỹ thuật viên của phiếu sửa chữa
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignments(Integer repairOrderId) {
        PhieuSuaChua order = findAndValidateRepairOrder(repairOrderId);
        return phanCongRepository.findByPhieuSuaChuaMaPhieuSuaChua(order.getMaPhieuSuaChua())
                .stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Phân công kỹ thuật viên cho phiếu sửa chữa:
     * - Kiểm tra trạng thái phiếu sửa chữa (không ở trạng thái HOAN_TAT hoặc HUY)
     * - Kiểm tra nhân viên tồn tại & đang hoạt động
     * - Kiểm tra vai trò TECHNICIAN của nhân viên
     * - Kiểm tra chi nhánh: Kỹ thuật viên phải thuộc cùng chi nhánh với phiếu sửa chữa
     * - Chống trùng lặp: Không phân công 1 technician nhiều lần vào 1 phiếu sửa chữa
     * - Đồng bộ trạng thái: Chuyển phiếu sửa chữa từ CHO_XU_LY -> DA_PHAN_CONG
     */
    @Transactional
    public AssignmentResponse createAssignment(Integer repairOrderId, CreateAssignmentRequest request) {
        PhieuSuaChua order = findAndValidateRepairOrder(repairOrderId);
        validateOrderNotCompletedOrCancelled(order, "phân công");

        // 1. Kiểm tra Nhân viên
        NhanVien nhanVien = nhanVienRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + request.getTechnicianId()));

        if (Boolean.FALSE.equals(nhanVien.getTrangThai())) {
            String name = (nhanVien.getNguoiDung() != null) ? nhanVien.getNguoiDung().getHoTen() : nhanVien.getMaNhanVienCode();
            throw new BadRequestException("Nhân viên '" + name + "' hiện đang ngưng hoạt động");
        }

        NguoiDung user = nhanVien.getNguoiDung();
        if (user == null || Boolean.FALSE.equals(user.getTrangThai())) {
            throw new BadRequestException("Tài khoản người dùng của nhân viên này không hợp lệ hoặc đã bị khóa");
        }

        // 2. Kiểm tra vai trò ROLE_TECHNICIAN
        boolean isTechnician = nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                .stream()
                .anyMatch(nvt -> {
                    String roleName = nvt.getVaiTro().getTenVaiTro();
                    return "ROLE_TECHNICIAN".equalsIgnoreCase(roleName) || "TECHNICIAN".equalsIgnoreCase(roleName);
                });

        if (!isTechnician) {
            String name = user.getHoTen() != null ? user.getHoTen() : nhanVien.getMaNhanVienCode();
            throw new BadRequestException("Nhân viên '" + name + "' không có vai trò Kỹ thuật viên (ROLE_TECHNICIAN)");
        }

        // 3. Kiểm tra chi nhánh khớp nhau
        Integer orderBranchId = order.getChiNhanh().getMaChiNhanh();
        Integer techBranchId = nhanVien.getChiNhanh() != null ? nhanVien.getChiNhanh().getMaChiNhanh() : null;

        if (techBranchId == null || !techBranchId.equals(orderBranchId)) {
            throw new AccessDeniedException("Forbidden: Kỹ thuật viên không thuộc chi nhánh của phiếu sửa chữa");
        }

        // 4. Chống phân công trùng lặp
        if (phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienMaNhanVien(repairOrderId, request.getTechnicianId())) {
            String name = user.getHoTen() != null ? user.getHoTen() : nhanVien.getMaNhanVienCode();
            throw new DuplicateResourceException("Kỹ thuật viên '" + name + "' đã được phân công cho phiếu sửa chữa này");
        }

        // 5. Tạo phân công
        PhanCong phanCong = new PhanCong();
        phanCong.setPhieuSuaChua(order);
        phanCong.setNhanVien(nhanVien);
        phanCong.setVaiTroTrongCongViec(request.getVaiTroTrongCongViec());
        phanCong.setTrangThai("DA_GIAO");

        PhanCong saved = phanCongRepository.save(phanCong);

        // 6. Đồng bộ trạng thái phiếu sửa chữa nếu đang là CHO_XU_LY
        if ("CHO_XU_LY".equalsIgnoreCase(order.getTrangThai())) {
            order.setTrangThai("DA_PHAN_CONG");
            phieuSuaChuaRepository.save(order);
        }

        return mapToAssignmentResponse(saved);
    }

    /**
     * Hủy/xóa phân công kỹ thuật viên khỏi phiếu sửa chữa
     */
    @Transactional
    public void deleteAssignment(Integer repairOrderId, Integer assignmentId) {
        PhieuSuaChua order = findAndValidateRepairOrder(repairOrderId);
        validateOrderNotCompletedOrCancelled(order, "hủy phân công");

        PhanCong assignment = phanCongRepository
                .findByMaPhanCongAndPhieuSuaChuaMaPhieuSuaChua(assignmentId, repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công với ID: " + assignmentId + " trong phiếu sửa chữa này"));

        phanCongRepository.delete(assignment);

        // Nếu sau khi xóa không còn phân công nào và trạng thái đang là DA_PHAN_CONG -> rollback về CHO_XU_LY
        long remaining = phanCongRepository.countByPhieuSuaChuaMaPhieuSuaChua(repairOrderId);
        if (remaining == 0 && "DA_PHAN_CONG".equalsIgnoreCase(order.getTrangThai())) {
            order.setTrangThai("CHO_XU_LY");
            phieuSuaChuaRepository.save(order);
        }
    }

    // --- Private Helpers ---

    private PhieuSuaChua findAndValidateRepairOrder(Integer repairOrderId) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + repairOrderId));

        if (!branchAuthorizationService.isAllowedBranch(order.getChiNhanh().getMaChiNhanh())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập phiếu sửa chữa của chi nhánh khác");
        }

        return order;
    }

    private void validateOrderNotCompletedOrCancelled(PhieuSuaChua order, String action) {
        String status = order.getTrangThai();
        if ("HOAN_TAT".equalsIgnoreCase(status) || "HUY".equalsIgnoreCase(status)) {
            throw new BadRequestException("Không thể " + action + " kỹ thuật viên khi phiếu sửa chữa đã ở trạng thái: " + status);
        }
    }

    private AssignmentResponse mapToAssignmentResponse(PhanCong pc) {
        NhanVien nv = pc.getNhanVien();
        NguoiDung user = (nv != null) ? nv.getNguoiDung() : null;
        ChiNhanh cn = (nv != null) ? nv.getChiNhanh() : null;

        return new AssignmentResponse(
                pc.getMaPhanCong(),
                pc.getPhieuSuaChua() != null ? pc.getPhieuSuaChua().getMaPhieuSuaChua() : null,
                nv != null ? nv.getMaNhanVien() : null,
                nv != null ? nv.getMaNhanVienCode() : null,
                user != null ? user.getHoTen() : null,
                cn != null ? cn.getMaChiNhanh() : null,
                cn != null ? cn.getMaChiNhanhCode() : null,
                cn != null ? cn.getTenChiNhanh() : null,
                pc.getVaiTroTrongCongViec(),
                pc.getThoiGianPhanCong(),
                pc.getTrangThai()
        );
    }
}
