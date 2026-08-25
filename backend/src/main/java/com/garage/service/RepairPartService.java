package com.garage.service;

import com.garage.dto.CreateRepairPartRequest;
import com.garage.dto.RepairPartResponse;
import com.garage.dto.UpdateRepairPartRequest;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepairPartService {

    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final PhuTungRepository phuTungRepository;
    private final TonKhoRepository tonKhoRepository;
    private final PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository;
    private final GiaoDichKhoRepository giaoDichKhoRepository;
    private final PhanCongRepository phanCongRepository;
    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public RepairPartService(PhieuSuaChuaRepository phieuSuaChuaRepository,
                             PhuTungRepository phuTungRepository,
                             TonKhoRepository tonKhoRepository,
                             PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository,
                             GiaoDichKhoRepository giaoDichKhoRepository,
                             PhanCongRepository phanCongRepository,
                             NhanVienRepository nhanVienRepository,
                             NguoiDungRepository nguoiDungRepository,
                             BranchAuthorizationService branchAuthorizationService) {
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.phuTungRepository = phuTungRepository;
        this.tonKhoRepository = tonKhoRepository;
        this.phieuSuaChuaPhuTungRepository = phieuSuaChuaPhuTungRepository;
        this.giaoDichKhoRepository = giaoDichKhoRepository;
        this.phanCongRepository = phanCongRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Lấy danh sách phụ tùng sử dụng trong Phiếu sửa chữa
     */
    @Transactional(readOnly = true)
    public List<RepairPartResponse> getRepairOrderParts(Integer repairOrderId) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + repairOrderId));

        validateAccessAuthorization(order, false);

        return phieuSuaChuaPhuTungRepository.findByPhieuSuaChuaMaPhieuSuaChua(repairOrderId)
                .stream()
                .map(this::mapToRepairPartResponse)
                .collect(Collectors.toList());
    }

    /**
     * Thêm phụ tùng vào Phiếu sửa chữa:
     * - Kiểm tra quyền hạn và phân công
     * - Chặn khi phiếu đã HOAN_TAT hoặc HUY (400)
     * - Phụ tùng phải tồn tại và đang hoạt động (404/400)
     * - Tránh thêm trùng phụ tùng (409)
     * - Kiểm tra tồn kho tại chi nhánh của phiếu sửa chữa (400)
     * - Trừ tồn kho và ghi nhật ký GiaoDichKho (Transactional)
     * - Lấy giá bán chính thức từ catalog, không tin giá từ client
     */
    @Transactional
    public RepairPartResponse addPartToRepairOrder(Integer repairOrderId, CreateRepairPartRequest request) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + repairOrderId));

        validateAccessAuthorization(order, true);
        validateOrderNotCompletedOrCancelled(order, "thêm phụ tùng");

        PhuTung part = phuTungRepository.findById(request.getMaPhuTung())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ tùng với ID: " + request.getMaPhuTung()));

        if (Boolean.FALSE.equals(part.getTrangThai())) {
            throw new BadRequestException("Phụ tùng '" + part.getTenPhuTung() + "' hiện đang ngưng hoạt động");
        }

        if (phieuSuaChuaPhuTungRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndPhuTungMaPhuTung(repairOrderId, part.getMaPhuTung())) {
            throw new DuplicateResourceException("Phụ tùng '" + part.getTenPhuTung() + "' đã có trong phiếu sửa chữa. Vui lòng cập nhật số lượng thay vì thêm mới");
        }

        Integer branchId = order.getChiNhanh().getMaChiNhanh();
        TonKho tonKho = tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(branchId, part.getMaPhuTung())
                .orElseThrow(() -> new BadRequestException("Phụ tùng chưa được thiết lập tồn kho tại chi nhánh " + order.getChiNhanh().getTenChiNhanh()));

        if (tonKho.getSoLuongTon() < request.getSoLuong()) {
            throw new BadRequestException("Số lượng tồn kho không đủ (Hiện còn: " + tonKho.getSoLuongTon() + ", yêu cầu: " + request.getSoLuong() + ")");
        }

        // Trừ tồn kho
        tonKho.setSoLuongTon(tonKho.getSoLuongTon() - request.getSoLuong());
        tonKhoRepository.save(tonKho);

        // Ghi nhật ký giao dịch kho
        GiaoDichKho gd = new GiaoDichKho();
        gd.setChiNhanh(order.getChiNhanh());
        gd.setPhuTung(part);
        gd.setLoaiGiaoDich("XUAT_SUA_CHUA");
        gd.setSoLuong(request.getSoLuong());
        gd.setPhieuSuaChua(order);
        gd.setGhiChu("Xuất phụ tùng cho phiếu sửa chữa #" + repairOrderId);
        giaoDichKhoRepository.save(gd);

        // Tạo chi tiết phụ tùng sửa chữa (đơn giá lấy từ catalog part.getGiaBan())
        PhieuSuaChuaPhuTung item = new PhieuSuaChuaPhuTung();
        item.setPhieuSuaChua(order);
        item.setPhuTung(part);
        item.setSoLuong(request.getSoLuong());
        item.setDonGia(part.getGiaBan() != null ? part.getGiaBan() : BigDecimal.ZERO);

        PhieuSuaChuaPhuTung saved = phieuSuaChuaPhuTungRepository.save(item);
        return mapToRepairPartResponse(saved);
    }

    /**
     * Cập nhật số lượng phụ tùng sử dụng:
     * - Nếu tăng số lượng: Kiểm tra tồn kho và trừ thêm
     * - Nếu giảm số lượng: Hoàn trả phần chênh lệch vào tồn kho
     * - Ghi nhật ký GiaoDichKho tương ứng
     */
    @Transactional
    public RepairPartResponse updatePartQuantity(Integer repairOrderId, Integer partDetailId, UpdateRepairPartRequest request) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + repairOrderId));

        validateAccessAuthorization(order, true);
        validateOrderNotCompletedOrCancelled(order, "cập nhật phụ tùng");

        PhieuSuaChuaPhuTung item = phieuSuaChuaPhuTungRepository
                .findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(partDetailId, repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ tùng chi tiết ID " + partDetailId + " trong phiếu sửa chữa này"));

        int oldQuantity = item.getSoLuong();
        int newQuantity = request.getSoLuong();
        int diff = newQuantity - oldQuantity;

        if (diff != 0) {
            Integer branchId = order.getChiNhanh().getMaChiNhanh();
            TonKho tonKho = tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(branchId, item.getPhuTung().getMaPhuTung())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy thông tin tồn kho phụ tùng"));

            if (diff > 0) {
                // Cần lấy thêm từ kho
                if (tonKho.getSoLuongTon() < diff) {
                    throw new BadRequestException("Số lượng tồn kho không đủ để tăng thêm (Hiện còn: " + tonKho.getSoLuongTon() + ", cần thêm: " + diff + ")");
                }
                tonKho.setSoLuongTon(tonKho.getSoLuongTon() - diff);
                tonKhoRepository.save(tonKho);

                GiaoDichKho gd = new GiaoDichKho();
                gd.setChiNhanh(order.getChiNhanh());
                gd.setPhuTung(item.getPhuTung());
                gd.setLoaiGiaoDich("XUAT_SUA_CHUA");
                gd.setSoLuong(diff);
                gd.setPhieuSuaChua(order);
                gd.setGhiChu("Xuất thêm " + diff + " phụ tùng cho phiếu sửa chữa #" + repairOrderId);
                giaoDichKhoRepository.save(gd);
            } else {
                // Hoàn trả bớt vào kho (diff < 0)
                int returnQty = -diff;
                tonKho.setSoLuongTon(tonKho.getSoLuongTon() + returnQty);
                tonKhoRepository.save(tonKho);

                GiaoDichKho gd = new GiaoDichKho();
                gd.setChiNhanh(order.getChiNhanh());
                gd.setPhuTung(item.getPhuTung());
                gd.setLoaiGiaoDich("HOAN_TRA");
                gd.setSoLuong(returnQty);
                gd.setPhieuSuaChua(order);
                gd.setGhiChu("Hoàn trả " + returnQty + " phụ tùng từ phiếu sửa chữa #" + repairOrderId);
                giaoDichKhoRepository.save(gd);
            }

            item.setSoLuong(newQuantity);
            item = phieuSuaChuaPhuTungRepository.save(item);
        }

        return mapToRepairPartResponse(item);
    }

    /**
     * Xóa phụ tùng khỏi Phiếu sửa chữa và hoàn trả 100% số lượng vào tồn kho
     */
    @Transactional
    public void deletePartFromRepairOrder(Integer repairOrderId, Integer partDetailId) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + repairOrderId));

        validateAccessAuthorization(order, true);
        validateOrderNotCompletedOrCancelled(order, "xóa phụ tùng");

        PhieuSuaChuaPhuTung item = phieuSuaChuaPhuTungRepository
                .findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(partDetailId, repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ tùng chi tiết ID " + partDetailId + " trong phiếu sửa chữa này"));

        Integer branchId = order.getChiNhanh().getMaChiNhanh();
        TonKho tonKho = tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(branchId, item.getPhuTung().getMaPhuTung())
                .orElse(null);

        if (tonKho != null) {
            tonKho.setSoLuongTon(tonKho.getSoLuongTon() + item.getSoLuong());
            tonKhoRepository.save(tonKho);

            GiaoDichKho gd = new GiaoDichKho();
            gd.setChiNhanh(order.getChiNhanh());
            gd.setPhuTung(item.getPhuTung());
            gd.setLoaiGiaoDich("HOAN_TRA");
            gd.setSoLuong(item.getSoLuong());
            gd.setPhieuSuaChua(order);
            gd.setGhiChu("Hoàn trả phụ tùng do xóa khỏi phiếu sửa chữa #" + repairOrderId);
            giaoDichKhoRepository.save(gd);
        }

        phieuSuaChuaPhuTungRepository.delete(item);
    }

    // --- Helpers ---

    private void validateAccessAuthorization(PhieuSuaChua order, boolean isModify) {
        Integer branchId = order.getChiNhanh().getMaChiNhanh();

        if (!branchAuthorizationService.isAllowedBranch(branchId)) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền thao tác trên phiếu sửa chữa của chi nhánh khác");
        }

        // Nếu người gọi là TECHNICIAN, kiểm tra xem có được phân công phụ trách phiếu này không
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECHNICIAN"))) {
            NguoiDung user = nguoiDungRepository.findByTenDangNhapOrEmail(auth.getName(), auth.getName()).orElse(null);
            if (user != null) {
                NhanVien tech = nhanVienRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()).orElse(null);
                if (tech != null) {
                    boolean isAssigned = phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienMaNhanVien(
                            order.getMaPhieuSuaChua(), tech.getMaNhanVien()
                    );
                    if (!isAssigned) {
                        throw new AccessDeniedException("Forbidden: Bạn không được phân công phụ trách phiếu sửa chữa này");
                    }
                }
            }
        }
    }

    private void validateOrderNotCompletedOrCancelled(PhieuSuaChua order, String action) {
        String status = order.getTrangThai();
        if ("HOAN_TAT".equalsIgnoreCase(status) || "HUY".equalsIgnoreCase(status)) {
            throw new BadRequestException("Không thể " + action + " khi phiếu sửa chữa đã ở trạng thái: " + status);
        }
    }

    private RepairPartResponse mapToRepairPartResponse(PhieuSuaChuaPhuTung item) {
        PhuTung pt = item.getPhuTung();
        BigDecimal thanhTien = BigDecimal.ZERO;
        if (item.getDonGia() != null && item.getSoLuong() != null) {
            thanhTien = item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong()));
        }

        return new RepairPartResponse(
                item.getMaChiTiet(),
                item.getPhieuSuaChua() != null ? item.getPhieuSuaChua().getMaPhieuSuaChua() : null,
                pt != null ? pt.getMaPhuTung() : null,
                pt != null ? pt.getMaPhuTungCode() : null,
                pt != null ? pt.getTenPhuTung() : null,
                pt != null ? pt.getDonViTinh() : null,
                item.getSoLuong(),
                item.getDonGia(),
                thanhTien
        );
    }
}
