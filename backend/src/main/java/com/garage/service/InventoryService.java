package com.garage.service;

import com.garage.dto.*;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final PhuTungRepository phuTungRepository;
    private final TonKhoRepository tonKhoRepository;
    private final ChiNhanhRepository chiNhanhRepository;
    private final GiaoDichKhoRepository giaoDichKhoRepository;
    private final PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository;
    private final DichVuPhuTungRepository dichVuPhuTungRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public InventoryService(PhuTungRepository phuTungRepository,
                            TonKhoRepository tonKhoRepository,
                            ChiNhanhRepository chiNhanhRepository,
                            GiaoDichKhoRepository giaoDichKhoRepository,
                            PhieuSuaChuaPhuTungRepository phieuSuaChuaPhuTungRepository,
                            DichVuPhuTungRepository dichVuPhuTungRepository,
                            BranchAuthorizationService branchAuthorizationService) {
        this.phuTungRepository = phuTungRepository;
        this.tonKhoRepository = tonKhoRepository;
        this.chiNhanhRepository = chiNhanhRepository;
        this.giaoDichKhoRepository = giaoDichKhoRepository;
        this.phieuSuaChuaPhuTungRepository = phieuSuaChuaPhuTungRepository;
        this.dichVuPhuTungRepository = dichVuPhuTungRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    // ==========================================
    // PART CATALOG MANAGEMENT (Admin CRUD, Manager Status Toggle)
    // ==========================================

    @Transactional(readOnly = true)
    public List<PartResponse> getAllParts(boolean onlyActive) {
        List<PhuTung> list = onlyActive
                ? phuTungRepository.findByTrangThaiTrue()
                : phuTungRepository.findAll();

        return list.stream()
                .map(this::mapToPartResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PartResponse> getAllParts() {
        return getAllParts(true);
    }

    @Transactional(readOnly = true)
    public PartResponse getPartById(Integer id) {
        PhuTung part = phuTungRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ tùng với ID: " + id));
        return mapToPartResponse(part);
    }

    @Transactional
    public PartResponse createPart(CreatePartRequest request) {
        String code = request.getMaPhuTungCode().trim().toUpperCase();
        if (phuTungRepository.existsByMaPhuTungCode(code)) {
            throw new DuplicateResourceException("Mã phụ tùng '" + code + "' đã tồn tại trong hệ thống");
        }

        PhuTung part = new PhuTung();
        part.setMaPhuTungCode(code);
        part.setTenPhuTung(request.getTenPhuTung().trim());
        part.setDonViTinh(request.getDonViTinh());
        part.setGiaNhap(request.getGiaNhap());
        part.setGiaBan(request.getGiaBan());
        part.setTrangThai(true);

        PhuTung saved = phuTungRepository.save(part);

        // Khởi tạo bản ghi TonKho = 0 cho tất cả chi nhánh
        List<ChiNhanh> branches = chiNhanhRepository.findAll();
        for (ChiNhanh branch : branches) {
            if (!tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(branch.getMaChiNhanh(), saved.getMaPhuTung()).isPresent()) {
                TonKho tk = new TonKho(branch, saved);
                tk.setSoLuongTon(0);
                tk.setSoLuongToiThieu(5);
                tonKhoRepository.save(tk);
            }
        }

        return mapToPartResponse(saved);
    }

    @Transactional
    public PartResponse updatePart(Integer id, UpdatePartRequest request) {
        PhuTung part = phuTungRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ tùng với ID: " + id));

        part.setTenPhuTung(request.getTenPhuTung().trim());
        part.setDonViTinh(request.getDonViTinh());
        part.setGiaNhap(request.getGiaNhap());
        part.setGiaBan(request.getGiaBan());

        PhuTung saved = phuTungRepository.save(part);
        return mapToPartResponse(saved);
    }

    @Transactional
    public PartResponse updatePartStatus(Integer id, UpdatePartStatusRequest request) {
        PhuTung part = phuTungRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ tùng với ID: " + id));

        part.setTrangThai(request.getTrangThai());
        PhuTung saved = phuTungRepository.save(part);
        return mapToPartResponse(saved);
    }

    @Transactional
    public void deletePart(Integer id) {
        PhuTung part = phuTungRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ tùng với ID: " + id));

        boolean usedInRepair = phieuSuaChuaPhuTungRepository.findAll().stream()
                .anyMatch(p -> p.getPhuTung() != null && p.getPhuTung().getMaPhuTung().equals(id));

        boolean usedInService = !dichVuPhuTungRepository.findByPhuTungMaPhuTung(id).isEmpty();

        boolean hasStockTransactions = tonKhoRepository.findAll().stream()
                .anyMatch(tk -> tk.getPhuTung() != null && tk.getPhuTung().getMaPhuTung().equals(id) && tk.getSoLuongTon() > 0);

        if (usedInRepair || usedInService || hasStockTransactions) {
            throw new BadRequestException("Phụ tùng này đã có lịch sử sử dụng trong sửa chữa, cấu hình dịch vụ hoặc đang có tồn kho > 0. Vui lòng chuyển trạng thái sang Tạm ngưng hoạt động.");
        }

        // Xóa các bản ghi TonKho rỗng
        List<TonKho> stockList = tonKhoRepository.findAll().stream()
                .filter(tk -> tk.getPhuTung() != null && tk.getPhuTung().getMaPhuTung().equals(id))
                .toList();
        tonKhoRepository.deleteAll(stockList);

        phuTungRepository.delete(part);
    }

    // ==========================================
    // INVENTORY & STOCK IMPORT (Manager & Admin)
    // ==========================================

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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Integer> userBranchOpt = branchAuthorizationService.resolveUserBranchId(auth);
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

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryDetail(Integer branchId, Integer partId) {
        if (!branchAuthorizationService.isAllowedBranch(branchId)) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập tồn kho của chi nhánh khác");
        }

        TonKho tonKho = tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(branchId, partId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tồn kho cho phụ tùng ID " + partId + " tại chi nhánh ID " + branchId));

        return mapToInventoryResponse(tonKho);
    }

    /**
     * Nhập kho phụ tùng cho chi nhánh:
     * - Manager: Tự động lấy branch của mình từ token (cấm nhập cho branch khác)
     * - Admin: Có thể chỉ định branchId
     * - Tăng TonKho.SoLuongTon
     * - Ghi GiaoDichKho với LoaiGiaoDich = "NHAP"
     */
    @Transactional
    public InventoryResponse importStock(StockImportRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer targetBranchId = request.getBranchId();

        Optional<Integer> managerBranchOpt = branchAuthorizationService.resolveUserBranchId(auth);
        if (managerBranchOpt.isPresent()) {
            targetBranchId = managerBranchOpt.get(); // Manager chỉ được nhập vào chi nhánh mình
        }

        if (targetBranchId == null) {
            List<ChiNhanh> branches = chiNhanhRepository.findAll();
            if (branches.isEmpty()) {
                throw new BadRequestException("Hệ thống chưa có chi nhánh");
            }
            targetBranchId = branches.get(0).getMaChiNhanh();
        }

        if (!branchAuthorizationService.isAllowedBranch(targetBranchId)) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền nhập kho cho chi nhánh này");
        }

        final Integer branchIdFinal = targetBranchId;
        ChiNhanh branch = chiNhanhRepository.findById(branchIdFinal)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh ID: " + branchIdFinal));

        PhuTung part = phuTungRepository.findById(request.getMaPhuTung())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ tùng ID: " + request.getMaPhuTung()));

        if (Boolean.FALSE.equals(part.getTrangThai())) {
            throw new BadRequestException("Phụ tùng '" + part.getTenPhuTung() + "' đang tạm ngưng hoạt động, không thể nhập kho");
        }

        // Tìm hoặc khởi tạo TonKho
        TonKho tonKho = tonKhoRepository.findByIdMaChiNhanhAndIdMaPhuTung(branch.getMaChiNhanh(), part.getMaPhuTung())
                .orElseGet(() -> {
                    TonKho newTk = new TonKho(branch, part);
                    newTk.setSoLuongTon(0);
                    newTk.setSoLuongToiThieu(5);
                    return tonKhoRepository.save(newTk);
                });

        // Tăng tồn kho
        tonKho.setSoLuongTon(tonKho.getSoLuongTon() + request.getSoLuong());
        TonKho savedTonKho = tonKhoRepository.save(tonKho);

        // Ghi GiaoDichKho
        GiaoDichKho gd = new GiaoDichKho();
        gd.setChiNhanh(branch);
        gd.setPhuTung(part);
        gd.setLoaiGiaoDich("NHAP");
        gd.setSoLuong(request.getSoLuong());
        gd.setGhiChu(request.getGhiChu() != null ? request.getGhiChu() : "Nhập kho chi nhánh " + branch.getTenChiNhanh());
        gd.setPhieuSuaChua(null);
        giaoDichKhoRepository.save(gd);

        return mapToInventoryResponse(savedTonKho);
    }

    /**
     * Lấy lịch sử giao dịch kho
     */
    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getStockTransactions(Integer branchId) {
        List<GiaoDichKho> transactions;

        if (branchId != null) {
            if (!branchAuthorizationService.isAllowedBranch(branchId)) {
                throw new AccessDeniedException("Forbidden: Bạn không có quyền xem giao dịch kho chi nhánh khác");
            }
            transactions = giaoDichKhoRepository.findByChiNhanhMaChiNhanhOrderByMaGiaoDichDesc(branchId);
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<Integer> userBranchOpt = branchAuthorizationService.resolveUserBranchId(auth);
            if (userBranchOpt.isPresent()) {
                transactions = giaoDichKhoRepository.findByChiNhanhMaChiNhanhOrderByMaGiaoDichDesc(userBranchOpt.get());
            } else if (branchAuthorizationService.isAllowedBranch(1)) {
                transactions = giaoDichKhoRepository.findAllByOrderByMaGiaoDichDesc();
            } else {
                throw new AccessDeniedException("Forbidden: Không xác định được chi nhánh của tài khoản");
            }
        }

        return transactions.stream().map(gd -> new StockTransactionResponse(
                gd.getMaGiaoDich(),
                gd.getChiNhanh() != null ? gd.getChiNhanh().getMaChiNhanh() : null,
                gd.getChiNhanh() != null ? gd.getChiNhanh().getTenChiNhanh() : null,
                gd.getPhuTung() != null ? gd.getPhuTung().getMaPhuTung() : null,
                gd.getPhuTung() != null ? gd.getPhuTung().getMaPhuTungCode() : null,
                gd.getPhuTung() != null ? gd.getPhuTung().getTenPhuTung() : null,
                gd.getPhuTung() != null ? gd.getPhuTung().getDonViTinh() : null,
                gd.getLoaiGiaoDich(),
                gd.getSoLuong(),
                gd.getPhieuSuaChua() != null ? gd.getPhieuSuaChua().getMaPhieuSuaChua() : null,
                gd.getGhiChu(),
                gd.getThoiGian()
        )).collect(Collectors.toList());
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
