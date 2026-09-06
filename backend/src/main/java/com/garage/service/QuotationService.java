package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuotationService {

    private final BaoGiaPhatSinhRepository baoGiaPhatSinhRepository;
    private final BaoGiaPhatSinhDichVuRepository baoGiaPhatSinhDichVuRepository;
    private final BaoGiaPhatSinhPhuTungRepository baoGiaPhatSinhPhuTungRepository;
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final DichVuRepository dichVuRepository;
    private final PhuTungRepository phuTungRepository;
    private final PhanCongRepository phanCongRepository;
    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public QuotationService(BaoGiaPhatSinhRepository baoGiaPhatSinhRepository,
                            BaoGiaPhatSinhDichVuRepository baoGiaPhatSinhDichVuRepository,
                            BaoGiaPhatSinhPhuTungRepository baoGiaPhatSinhPhuTungRepository,
                            PhieuSuaChuaRepository phieuSuaChuaRepository,
                            DichVuRepository dichVuRepository,
                            PhuTungRepository phuTungRepository,
                            PhanCongRepository phanCongRepository,
                            NhanVienRepository nhanVienRepository,
                            NguoiDungRepository nguoiDungRepository,
                            BranchAuthorizationService branchAuthorizationService) {
        this.baoGiaPhatSinhRepository = baoGiaPhatSinhRepository;
        this.baoGiaPhatSinhDichVuRepository = baoGiaPhatSinhDichVuRepository;
        this.baoGiaPhatSinhPhuTungRepository = baoGiaPhatSinhPhuTungRepository;
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.dichVuRepository = dichVuRepository;
        this.phuTungRepository = phuTungRepository;
        this.phanCongRepository = phanCongRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    /**
     * Tạo báo giá phát sinh cho phiếu sửa chữa:
     * - Giá dịch vụ lấy trực tiếp từ DichVu.donGia (không dùng SoLuong)
     * - Phụ tùng tính theo SoLuong * GiaBan
     * - Tính tổng tiền tự động
     * - Lưu chi tiết dịch vụ và phụ tùng trong Transaction
     */
    @Transactional
    public QuotationResponse createQuotation(Integer repairOrderId, CreateQuotationRequest request) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + repairOrderId));

        validateStaffAccessAuthorization(order);
        validateOrderNotCompletedOrCancelled(order, "tạo báo giá phát sinh");

        boolean hasServices = request.getServices() != null && !request.getServices().isEmpty();
        boolean hasParts = request.getParts() != null && !request.getParts().isEmpty();

        if (!hasServices && !hasParts) {
            throw new BadRequestException("Báo giá phát sinh phải chứa ít nhất một dịch vụ hoặc một phụ tùng");
        }

        BaoGiaPhatSinh quotation = new BaoGiaPhatSinh();
        quotation.setPhieuSuaChua(order);
        quotation.setLyDoPhatSinh(request.getLyDoPhatSinh());
        quotation.setTrangThai("CHO_KHACH_DUYET");
        quotation.setTongTien(BigDecimal.ZERO);

        BaoGiaPhatSinh savedQuotation = baoGiaPhatSinhRepository.save(quotation);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<QuotationServiceItemResponse> serviceResponses = new ArrayList<>();
        List<QuotationPartItemResponse> partResponses = new ArrayList<>();

        // Xử lý dịch vụ (không có số lượng, thành tiền = đơn giá)
        if (hasServices) {
            for (QuotationServiceItemRequest svcReq : request.getServices()) {
                DichVu dv = dichVuRepository.findById(svcReq.getMaDichVu())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ ID: " + svcReq.getMaDichVu()));

                if (Boolean.FALSE.equals(dv.getTrangThai())) {
                    throw new BadRequestException("Dịch vụ '" + dv.getTenDichVu() + "' hiện đang ngưng hoạt động");
                }

                BigDecimal donGia = dv.getDonGia() != null ? dv.getDonGia() : BigDecimal.ZERO;
                totalAmount = totalAmount.add(donGia);

                BaoGiaPhatSinhDichVu item = new BaoGiaPhatSinhDichVu();
                item.setBaoGiaPhatSinh(savedQuotation);
                item.setDichVu(dv);
                item.setDonGia(donGia);

                BaoGiaPhatSinhDichVu savedItem = baoGiaPhatSinhDichVuRepository.save(item);
                serviceResponses.add(new QuotationServiceItemResponse(
                        savedItem.getMaChiTiet(),
                        dv.getMaDichVu(),
                        dv.getTenDichVu(),
                        donGia,
                        donGia
                ));
            }
        }

        // Xử lý phụ tùng (có số lượng)
        if (hasParts) {
            for (QuotationPartItemRequest partReq : request.getParts()) {
                PhuTung pt = phuTungRepository.findById(partReq.getMaPhuTung())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ tùng ID: " + partReq.getMaPhuTung()));

                if (Boolean.FALSE.equals(pt.getTrangThai())) {
                    throw new BadRequestException("Phụ tùng '" + pt.getTenPhuTung() + "' hiện đang ngưng hoạt động");
                }

                BigDecimal donGia = pt.getGiaBan() != null ? pt.getGiaBan() : BigDecimal.ZERO;
                BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(partReq.getSoLuong()));
                totalAmount = totalAmount.add(thanhTien);

                BaoGiaPhatSinhPhuTung item = new BaoGiaPhatSinhPhuTung();
                item.setBaoGiaPhatSinh(savedQuotation);
                item.setPhuTung(pt);
                item.setSoLuong(partReq.getSoLuong());
                item.setDonGia(donGia);

                BaoGiaPhatSinhPhuTung savedItem = baoGiaPhatSinhPhuTungRepository.save(item);
                partResponses.add(new QuotationPartItemResponse(
                        savedItem.getMaChiTiet(),
                        pt.getMaPhuTung(),
                        pt.getMaPhuTungCode(),
                        pt.getTenPhuTung(),
                        pt.getDonViTinh(),
                        savedItem.getSoLuong(),
                        donGia,
                        thanhTien
                ));
            }
        }

        savedQuotation.setTongTien(totalAmount);
        baoGiaPhatSinhRepository.save(savedQuotation);

        return buildQuotationResponse(savedQuotation, serviceResponses, partResponses);
    }

    /**
     * Lấy danh sách báo giá của phiếu sửa chữa
     */
    @Transactional(readOnly = true)
    public List<QuotationResponse> getQuotationsByRepairOrder(Integer repairOrderId) {
        PhieuSuaChua order = phieuSuaChuaRepository.findById(repairOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa với ID: " + repairOrderId));

        validateReadAuthorization(order);

        return baoGiaPhatSinhRepository.findByPhieuSuaChuaMaPhieuSuaChua(repairOrderId)
                .stream()
                .map(this::loadAndMapQuotationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết báo giá theo ID
     */
    @Transactional(readOnly = true)
    public QuotationResponse getQuotationById(Integer quotationId) {
        BaoGiaPhatSinh quotation = baoGiaPhatSinhRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo giá với ID: " + quotationId));

        validateReadAuthorization(quotation.getPhieuSuaChua());

        return loadAndMapQuotationResponse(quotation);
    }

    /**
     * Khách hàng duyệt báo giá phát sinh
     */
    @Transactional
    public QuotationResponse approveQuotation(Integer quotationId) {
        BaoGiaPhatSinh quotation = baoGiaPhatSinhRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo giá với ID: " + quotationId));

        validateApprovalAuthorization(quotation.getPhieuSuaChua());
        validateQuotationPending(quotation, "duyệt");

        quotation.setTrangThai("DA_DUYET");
        quotation.setThoiGianDuyet(LocalDateTime.now());
        BaoGiaPhatSinh updated = baoGiaPhatSinhRepository.save(quotation);

        return loadAndMapQuotationResponse(updated);
    }

    /**
     * Khách hàng từ chối báo giá phát sinh
     */
    @Transactional
    public QuotationResponse rejectQuotation(Integer quotationId) {
        BaoGiaPhatSinh quotation = baoGiaPhatSinhRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo giá với ID: " + quotationId));

        validateApprovalAuthorization(quotation.getPhieuSuaChua());
        validateQuotationPending(quotation, "từ chối");

        quotation.setTrangThai("TU_CHOI");
        quotation.setThoiGianDuyet(LocalDateTime.now());
        BaoGiaPhatSinh updated = baoGiaPhatSinhRepository.save(quotation);

        return loadAndMapQuotationResponse(updated);
    }

    /**
     * Hủy báo giá phát sinh (dành cho quản lý/nhân viên)
     */
    @Transactional
    public QuotationResponse cancelQuotation(Integer quotationId) {
        BaoGiaPhatSinh quotation = baoGiaPhatSinhRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo giá với ID: " + quotationId));

        validateStaffAccessAuthorization(quotation.getPhieuSuaChua());
        validateQuotationPending(quotation, "hủy");

        quotation.setTrangThai("HUY");
        BaoGiaPhatSinh updated = baoGiaPhatSinhRepository.save(quotation);

        return loadAndMapQuotationResponse(updated);
    }

    // --- Helpers ---

    private void validateStaffAccessAuthorization(PhieuSuaChua order) {
        Integer branchId = order.getChiNhanh().getMaChiNhanh();
        if (!branchAuthorizationService.isAllowedBranch(branchId)) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền thao tác trên phiếu sửa chữa của chi nhánh khác");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECHNICIAN"))) {
            NguoiDung user = nguoiDungRepository.findByTenDangNhapOrEmail(auth.getName(), auth.getName()).orElse(null);
            if (user != null) {
                NhanVien tech = nhanVienRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()).orElse(null);
                if (tech != null) {
                    boolean isAssigned = phanCongRepository.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVien(
                            order.getMaPhieuSuaChua(), tech.getMaNhanVien()
                    );
                    if (!isAssigned) {
                        throw new AccessDeniedException("Forbidden: Bạn không được phân công phụ trách phiếu sửa chữa này");
                    }
                }
            }
        }
    }

    private void validateReadAuthorization(PhieuSuaChua order) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            validateCustomerOwnership(order, auth.getName());
            return;
        }
        validateStaffAccessAuthorization(order);
    }

    private void validateApprovalAuthorization(PhieuSuaChua order) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            validateCustomerOwnership(order, auth.getName());
            return;
        }
        validateStaffAccessAuthorization(order);
    }

    private void validateCustomerOwnership(PhieuSuaChua order, String username) {
        try {
            NguoiDung user = nguoiDungRepository.findByTenDangNhapOrEmail(username, username)
                    .orElseThrow(() -> new AccessDeniedException("Forbidden: Không tìm thấy tài khoản khách hàng"));

            PhieuTiepNhan ptn = order.getPhieuTiepNhan();
            if (ptn != null && ptn.getXe() != null && ptn.getXe().getKhachHang() != null) {
                KhachHang owner = ptn.getXe().getKhachHang();
                if (owner.getNguoiDung() != null && owner.getNguoiDung().getMaNguoiDung().equals(user.getMaNguoiDung())) {
                    return; // Ownership verified
                }
            }
        } catch (Exception ignored) {}

        throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập báo giá của khách hàng khác");
    }

    private void validateOrderNotCompletedOrCancelled(PhieuSuaChua order, String action) {
        String status = order.getTrangThai();
        if ("HOAN_TAT".equalsIgnoreCase(status) || "HUY".equalsIgnoreCase(status)) {
            throw new BadRequestException("Không thể " + action + " khi phiếu sửa chữa đã ở trạng thái: " + status);
        }
    }

    private void validateQuotationPending(BaoGiaPhatSinh quotation, String action) {
        String status = quotation.getTrangThai();
        if (!"CHO_KHACH_DUYET".equalsIgnoreCase(status) && !"CHO_DUYET".equalsIgnoreCase(status)) {
            throw new BadRequestException("Không thể " + action + " báo giá đã ở trạng thái: " + status);
        }
    }

    private QuotationResponse loadAndMapQuotationResponse(BaoGiaPhatSinh q) {
        List<QuotationServiceItemResponse> services = baoGiaPhatSinhDichVuRepository
                .findByBaoGiaPhatSinhMaBaoGia(q.getMaBaoGia())
                .stream()
                .map(item -> {
                    DichVu dv = item.getDichVu();
                    BigDecimal donGia = item.getDonGia() != null ? item.getDonGia() : BigDecimal.ZERO;
                    return new QuotationServiceItemResponse(
                            item.getMaChiTiet(),
                            dv != null ? dv.getMaDichVu() : null,
                            dv != null ? dv.getTenDichVu() : null,
                            donGia,
                            donGia
                    );
                })
                .collect(Collectors.toList());

        List<QuotationPartItemResponse> parts = baoGiaPhatSinhPhuTungRepository
                .findByBaoGiaPhatSinhMaBaoGia(q.getMaBaoGia())
                .stream()
                .map(item -> {
                    PhuTung pt = item.getPhuTung();
                    BigDecimal donGia = item.getDonGia() != null ? item.getDonGia() : BigDecimal.ZERO;
                    int sl = item.getSoLuong() != null ? item.getSoLuong() : 1;
                    BigDecimal tt = donGia.multiply(BigDecimal.valueOf(sl));
                    return new QuotationPartItemResponse(
                            item.getMaChiTiet(),
                            pt != null ? pt.getMaPhuTung() : null,
                            pt != null ? pt.getMaPhuTungCode() : null,
                            pt != null ? pt.getTenPhuTung() : null,
                            pt != null ? pt.getDonViTinh() : null,
                            sl,
                            donGia,
                            tt
                    );
                })
                .collect(Collectors.toList());

        return buildQuotationResponse(q, services, parts);
    }

    private QuotationResponse buildQuotationResponse(BaoGiaPhatSinh q,
                                                     List<QuotationServiceItemResponse> services,
                                                     List<QuotationPartItemResponse> parts) {
        ChiNhanh cn = (q.getPhieuSuaChua() != null) ? q.getPhieuSuaChua().getChiNhanh() : null;
        return new QuotationResponse(
                q.getMaBaoGia(),
                q.getPhieuSuaChua() != null ? q.getPhieuSuaChua().getMaPhieuSuaChua() : null,
                cn != null ? cn.getMaChiNhanh() : null,
                cn != null ? cn.getTenChiNhanh() : null,
                q.getLyDoPhatSinh(),
                q.getTongTien(),
                q.getTrangThai(),
                q.getThoiGianTao(),
                q.getThoiGianDuyet(),
                services,
                parts
        );
    }
}
