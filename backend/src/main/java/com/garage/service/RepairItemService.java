package com.garage.service;

import com.garage.dto.CreateRepairItemRequest;
import com.garage.dto.RepairItemResponse;
import com.garage.dto.UpdateRepairItemRequest;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.DichVuRepository;
import com.garage.repository.PhieuSuaChuaDichVuRepository;
import com.garage.repository.PhieuSuaChuaRepository;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepairItemService {

    private final PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository;
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final DichVuRepository dichVuRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public RepairItemService(PhieuSuaChuaDichVuRepository phieuSuaChuaDichVuRepository,
                             PhieuSuaChuaRepository phieuSuaChuaRepository,
                             DichVuRepository dichVuRepository,
                             BranchAuthorizationService branchAuthorizationService) {
        this.phieuSuaChuaDichVuRepository = phieuSuaChuaDichVuRepository;
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.dichVuRepository = dichVuRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Lấy danh sách dịch vụ thuộc phiếu sửa chữa
     */
    @Transactional(readOnly = true)
    public List<RepairItemResponse> getRepairItems(Integer repairOrderId) {
        PhieuSuaChua order = findAndValidateRepairOrder(repairOrderId);
        return phieuSuaChuaDichVuRepository.findByPhieuSuaChuaMaPhieuSuaChua(order.getMaPhieuSuaChua())
                .stream()
                .map(this::mapToRepairItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Thêm dịch vụ vào phiếu sửa chữa:
     * - Kiểm tra trạng thái phiếu sửa chữa (không được ở trạng thái HOAN_TAT hoặc HUY)
     * - Kiểm tra dịch vụ tồn tại & đang hoạt động
     * - Chống trùng lặp: Mỗi dịch vụ chỉ xuất hiện 1 lần trong 1 phiếu sửa chữa
     * - Tự động lấy đơn giá từ catalog DichVu (hoặc dùng đơn giá override từ request nếu có)
     * - Tính thành tiền = số lượng * đơn giá
     */
    @Transactional
    public RepairItemResponse addRepairItem(Integer repairOrderId, CreateRepairItemRequest request) {
        PhieuSuaChua order = findAndValidateRepairOrder(repairOrderId);
        validateOrderNotCompletedOrCancelled(order, "thêm");

        // 1. Kiểm tra Dịch vụ
        DichVu dichVu = dichVuRepository.findById(request.getMaDichVu())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + request.getMaDichVu()));

        if (Boolean.FALSE.equals(dichVu.getTrangThai())) {
            throw new BadRequestException("Dịch vụ '" + dichVu.getTenDichVu() + "' hiện đang tạm ngưng hoạt động");
        }

        // 2. Chống trùng lặp dịch vụ trong cùng 1 phiếu sửa chữa
        if (phieuSuaChuaDichVuRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndDichVuMaDichVu(repairOrderId, request.getMaDichVu())) {
            throw new DuplicateResourceException("Dịch vụ '" + dichVu.getTenDichVu() + "' đã tồn tại trong phiếu sửa chữa này");
        }

        // 3. Xác định đơn giá dịch vụ từ Catalog toàn hệ thống (DichVu.donGia)
        BigDecimal donGia = resolveServicePrice(dichVu, request.getDonGia());

        // 4. Số lượng
        int soLuong = (request.getSoLuong() != null && request.getSoLuong() >= 1) ? request.getSoLuong() : 1;

        PhieuSuaChuaDichVu item = new PhieuSuaChuaDichVu();
        item.setPhieuSuaChua(order);
        item.setDichVu(dichVu);
        item.setSoLuong(soLuong);
        item.setDonGia(donGia);
        item.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : "CHO_XU_LY");

        PhieuSuaChuaDichVu saved = phieuSuaChuaDichVuRepository.save(item);
        return mapToRepairItemResponse(saved);
    }

    /**
     * Cập nhật số lượng, đơn giá hoặc trạng thái của hạng mục dịch vụ
     */
    @Transactional
    public RepairItemResponse updateRepairItem(Integer repairOrderId, Integer itemId, UpdateRepairItemRequest request) {
        PhieuSuaChua order = findAndValidateRepairOrder(repairOrderId);
        validateOrderNotCompletedOrCancelled(order, "cập nhật");

        PhieuSuaChuaDichVu item = phieuSuaChuaDichVuRepository
                .findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(itemId, repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hạng mục dịch vụ ID " + itemId + " trong phiếu sửa chữa này"));

        if (request.getSoLuong() != null) {
            if (request.getSoLuong() < 1) {
                throw new BadRequestException("Số lượng phải lớn hơn hoặc bằng 1");
            }
            item.setSoLuong(request.getSoLuong());
        }

        if (request.getDonGia() != null) {
            if (request.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Đơn giá không được nhỏ hơn 0");
            }
            item.setDonGia(request.getDonGia());
        }

        if (request.getTrangThai() != null) {
            item.setTrangThai(request.getTrangThai());
        }

        PhieuSuaChuaDichVu updated = phieuSuaChuaDichVuRepository.save(item);
        return mapToRepairItemResponse(updated);
    }

    /**
     * Xóa hạng mục dịch vụ khỏi phiếu sửa chữa
     */
    @Transactional
    public void deleteRepairItem(Integer repairOrderId, Integer itemId) {
        PhieuSuaChua order = findAndValidateRepairOrder(repairOrderId);
        validateOrderNotCompletedOrCancelled(order, "xóa");

        PhieuSuaChuaDichVu item = phieuSuaChuaDichVuRepository
                .findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua(itemId, repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hạng mục dịch vụ ID " + itemId + " trong phiếu sửa chữa này"));

        phieuSuaChuaDichVuRepository.delete(item);
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
            throw new BadRequestException("Không thể " + action + " dịch vụ khi phiếu sửa chữa đã ở trạng thái: " + status);
        }
    }

    private BigDecimal resolveServicePrice(DichVu dichVu, BigDecimal requestPrice) {
        if (requestPrice != null && requestPrice.compareTo(BigDecimal.ZERO) >= 0) {
            return requestPrice;
        }

        if (dichVu.getDonGia() != null && dichVu.getDonGia().compareTo(BigDecimal.ZERO) >= 0) {
            return dichVu.getDonGia();
        }

        throw new BadRequestException("Dịch vụ '" + dichVu.getTenDichVu() + "' chưa có đơn giá hợp lệ trong hệ thống");
    }

    private RepairItemResponse mapToRepairItemResponse(PhieuSuaChuaDichVu item) {
        DichVu dv = item.getDichVu();
        Integer maLoaiDichVu = null;
        String tenLoaiDichVu = null;
        if (dv != null && dv.getLoaiDichVu() != null) {
            maLoaiDichVu = dv.getLoaiDichVu().getMaLoaiDichVu();
            tenLoaiDichVu = dv.getLoaiDichVu().getTenLoai();
        }

        BigDecimal thanhTien = BigDecimal.ZERO;
        if (item.getDonGia() != null && item.getSoLuong() != null) {
            thanhTien = item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong()));
        }

        return new RepairItemResponse(
                item.getMaChiTiet(),
                item.getPhieuSuaChua() != null ? item.getPhieuSuaChua().getMaPhieuSuaChua() : null,
                dv != null ? dv.getMaDichVu() : null,
                dv != null ? dv.getTenDichVu() : null,
                maLoaiDichVu,
                tenLoaiDichVu,
                item.getSoLuong(),
                item.getDonGia(),
                thanhTien,
                item.getTrangThai()
        );
    }
}
