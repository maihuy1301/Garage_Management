package com.garage.service;

import com.garage.dto.AssignmentResponse;
import com.garage.dto.CreateAssignmentRequest;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.NhanVienRepository;
import com.garage.repository.PhanCongRepository;
import com.garage.repository.PhieuSuaChuaRepository;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicianAssignmentService {

    private final PhanCongRepository phanCongRepository;
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public TechnicianAssignmentService(PhanCongRepository phanCongRepository,
                                       PhieuSuaChuaRepository phieuSuaChuaRepository,
                                       NhanVienRepository nhanVienRepository,
                                       NguoiDungRepository nguoiDungRepository,
                                       NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
                                       BranchAuthorizationService branchAuthorizationService) {
        this.phanCongRepository = phanCongRepository;
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
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
     * - Xác định người quản lý phân công (từ request hoặc user hiện tại)
     * - Kiểm tra nhân viên được phân công tồn tại & đang hoạt động
     * - Kiểm tra vai trò TECHNICIAN của nhân viên được phân công
     * - Kiểm tra chi nhánh: Kỹ thuật viên phải thuộc cùng chi nhánh với phiếu sửa chữa
     * - Chống trùng lặp: Không phân công 1 technician nhiều lần vào 1 phiếu sửa chữa
     * - Đồng bộ trạng thái: Chuyển phiếu sửa chữa từ CHO_XU_LY -> DA_PHAN_CONG
     */
    @Transactional
    public AssignmentResponse createAssignment(Integer repairOrderId, CreateAssignmentRequest request) {
        PhieuSuaChua order = findAndValidateRepairOrder(repairOrderId);
        validateOrderNotCompletedOrCancelled(order, "phân công");

        // 1. Xác định Quản lý phân công
        NhanVien quanLy = null;
        if (request.getMaQuanLy() != null) {
            quanLy = nhanVienRepository.findById(request.getMaQuanLy())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quản lý với ID: " + request.getMaQuanLy()));
        } else {
            quanLy = getCurrentEmployee();
        }

        // 2. Kiểm tra Nhân viên được phân công
        Integer technicianId = request.getMaNhanVienDuocPhanCong();
        NhanVien nhanVien = nhanVienRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + technicianId));

        if (Boolean.FALSE.equals(nhanVien.getTrangThai())) {
            String name = (nhanVien.getNguoiDung() != null) ? nhanVien.getNguoiDung().getHoTen() : "ID " + nhanVien.getMaNhanVien();
            throw new BadRequestException("Nhân viên '" + name + "' hiện đang ngưng hoạt động");
        }

        NguoiDung user = nhanVien.getNguoiDung();
        if (user == null || Boolean.FALSE.equals(user.getTrangThai())) {
            throw new BadRequestException("Tài khoản người dùng của nhân viên này không hợp lệ hoặc đã bị khóa");
        }

        // 3. Kiểm tra vai trò ROLE_TECHNICIAN
        boolean isTechnician = nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                .stream()
                .anyMatch(nvt -> {
                    String roleName = nvt.getVaiTro().getTenVaiTro();
                    return "ROLE_TECHNICIAN".equalsIgnoreCase(roleName) || "TECHNICIAN".equalsIgnoreCase(roleName);
                });

        if (!isTechnician) {
            String name = user.getHoTen() != null ? user.getHoTen() : "ID " + nhanVien.getMaNhanVien();
            throw new BadRequestException("Nhân viên '" + name + "' không có vai trò Kỹ thuật viên (ROLE_TECHNICIAN)");
        }

        // 4. Kiểm tra chi nhánh khớp nhau
        Integer orderBranchId = order.getChiNhanh().getMaChiNhanh();
        Integer techBranchId = nhanVien.getChiNhanh() != null ? nhanVien.getChiNhanh().getMaChiNhanh() : null;

        if (techBranchId == null || !techBranchId.equals(orderBranchId)) {
            throw new AccessDeniedException("Forbidden: Kỹ thuật viên không thuộc chi nhánh của phiếu sửa chữa");
        }

        // 5. Chống phân công trùng lặp
        if (phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(repairOrderId, technicianId)) {
            String name = user.getHoTen() != null ? user.getHoTen() : "ID " + nhanVien.getMaNhanVien();
            throw new DuplicateResourceException("Kỹ thuật viên '" + name + "' đã được phân công cho phiếu sửa chữa này");
        }

        // 6. Tạo phân công
        PhanCong phanCong = new PhanCong();
        phanCong.setPhieuSuaChua(order);
        phanCong.setQuanLy(quanLy);
        phanCong.setNhanVienDuocPhanCong(nhanVien);
        phanCong.setTrangThai("DA_GIAO");

        PhanCong saved = phanCongRepository.save(phanCong);

        // 7. Đồng bộ trạng thái phiếu sửa chữa nếu đang là CHO_XU_LY
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

    private NhanVien getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            NguoiDung user = nguoiDungRepository.findByTenDangNhapOrEmail(auth.getName(), auth.getName()).orElse(null);
            if (user != null) {
                return nhanVienRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()).orElse(null);
            }
        }
        return null;
    }

    private AssignmentResponse mapToAssignmentResponse(PhanCong pc) {
        NhanVien ql = pc.getQuanLy();
        String tenQl = (ql != null && ql.getNguoiDung() != null) ? ql.getNguoiDung().getHoTen() : null;

        NhanVien nv = pc.getNhanVienDuocPhanCong();
        String tenNv = (nv != null && nv.getNguoiDung() != null) ? nv.getNguoiDung().getHoTen() : null;

        return new AssignmentResponse(
                pc.getMaPhanCong(),
                pc.getPhieuSuaChua() != null ? pc.getPhieuSuaChua().getMaPhieuSuaChua() : null,
                ql != null ? ql.getMaNhanVien() : null,
                tenQl,
                nv != null ? nv.getMaNhanVien() : null,
                tenNv,
                pc.getThoiGianPhanCong(),
                pc.getTrangThai()
        );
    }
}
