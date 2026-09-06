package com.garage.service;

import com.garage.dto.InventoryResponse;
import com.garage.dto.PartResponse;
import com.garage.entity.ChiNhanh;
import com.garage.entity.PhuTung;
import com.garage.entity.TonKho;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.PhuTungRepository;
import com.garage.repository.TonKhoRepository;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final PhuTungRepository phuTungRepository;
    private final TonKhoRepository tonKhoRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public InventoryService(PhuTungRepository phuTungRepository,
                            TonKhoRepository tonKhoRepository,
                            BranchAuthorizationService branchAuthorizationService) {
        this.phuTungRepository = phuTungRepository;
        this.tonKhoRepository = tonKhoRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Lấy danh sách phụ tùng trong danh mục
     */
    @Transactional(readOnly = true)
    public List<PartResponse> getAllParts() {
        return phuTungRepository.findByTrangThaiTrue()
                .stream()
                .map(this::mapToPartResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết phụ tùng theo ID
     */
    @Transactional(readOnly = true)
    public PartResponse getPartById(Integer id) {
        PhuTung part = phuTungRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ tùng với ID: " + id));
        return mapToPartResponse(part);
    }

    /**
     * Lấy danh sách tồn kho theo chi nhánh (kèm kiểm tra Branch Authorization)
     */
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByBranch(Integer branchId) {
        if (branchId != null) {
            if (!branchAuthorizationService.isAllowedBranch(branchId)) {
                throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập tồn kho của chi nhánh khác");
            }
            return tonKhoRepository.findByIdMaChiNhanhAndPhuTungTrangThaiTrue(branchId)
                    .stream()
                    .map(this::mapToInventoryResponse)
                    .collect(Collectors.toList());
        }

        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        java.util.Optional<Integer> userBranchOpt = branchAuthorizationService.resolveUserBranchId(auth);
        if (userBranchOpt.isPresent()) {
            return tonKhoRepository.findByIdMaChiNhanhAndPhuTungTrangThaiTrue(userBranchOpt.get())
                    .stream()
                    .map(this::mapToInventoryResponse)
                    .collect(Collectors.toList());
        }

        if (branchAuthorizationService.isAllowedBranch(1)) {
            return tonKhoRepository.findAll()
                    .stream()
                    .map(this::mapToInventoryResponse)
                    .collect(Collectors.toList());
        }

        throw new AccessDeniedException("Forbidden: Không xác định được chi nhánh của tài khoản");
    }

    /**
     * Lấy chi tiết tồn kho 1 phụ tùng tại chi nhánh
     */
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryDetail(Integer branchId, Integer partId) {
        if (!branchAuthorizationService.isAllowedBranch(branchId)) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập tồn kho của chi nhánh khác");
        }

        TonKho tonKho = tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(branchId, partId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tồn kho cho phụ tùng ID " + partId + " tại chi nhánh ID " + branchId));

        return mapToInventoryResponse(tonKho);
    }

    private PartResponse mapToPartResponse(PhuTung p) {
        return new PartResponse(
                p.getMaPhuTung(),
                p.getMaPhuTungCode(),
                p.getTenPhuTung(),
                p.getDonViTinh(),
                p.getGiaNhap(),
                p.getGiaBan(),
                p.getTrangThai()
        );
    }

    private InventoryResponse mapToInventoryResponse(TonKho tk) {
        ChiNhanh cn = tk.getChiNhanh();
        PhuTung pt = tk.getPhuTung();

        return new InventoryResponse(
                cn != null ? cn.getMaChiNhanh() : null,
                cn != null ? cn.getTenChiNhanh() : null,
                pt != null ? pt.getMaPhuTung() : null,
                pt != null ? pt.getMaPhuTungCode() : null,
                pt != null ? pt.getTenPhuTung() : null,
                pt != null ? pt.getDonViTinh() : null,
                pt != null ? pt.getGiaBan() : null,
                tk.getSoLuongTon(),
                tk.getSoLuongToiThieu()
        );
    }
}
