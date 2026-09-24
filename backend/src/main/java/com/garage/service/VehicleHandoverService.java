package com.garage.service;

import com.garage.dto.HandoverRequest;
import com.garage.dto.HandoverResponse;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class VehicleHandoverService {
    private final PhieuTiepNhanRepository receptions;
    private final PhieuSuaChuaRepository repairs;
    private final HoaDonRepository invoices;
    private final BanGiaoXeRepository handovers;
    private final NguoiDungRepository users;
    private final BranchAuthorizationService branches;
    private final NotificationService notifications;

    public VehicleHandoverService(PhieuTiepNhanRepository receptions, PhieuSuaChuaRepository repairs,
            HoaDonRepository invoices, BanGiaoXeRepository handovers, NguoiDungRepository users,
            BranchAuthorizationService branches, NotificationService notifications) {
        this.receptions = receptions;
        this.repairs = repairs;
        this.invoices = invoices;
        this.handovers = handovers;
        this.users = users;
        this.branches = branches;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public HandoverResponse getStatus(Integer id) {
        var reception = receptions.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu tiếp nhận"));
        authorize(reception);
        return status(reception);
    }

    @Transactional
    public HandoverResponse handover(Integer id, HandoverRequest request) {
        // Serialize confirmation and repeat requests for the same reception.
        var reception = receptions.findForHandover(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu tiếp nhận"));
        authorize(reception);
        var existing = handovers.findById(id);
        if (existing.isPresent()) return response(existing.get());
        String reason = blockedReason(reception);
        if (reason != null) throw new BadRequestException(reason);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        var staff = users.findByTenDangNhapOrEmail(auth.getName(), auth.getName())
                .filter(u -> !Boolean.FALSE.equals(u.getTrangThai()))
                .orElseThrow(() -> new AccessDeniedException("Tài khoản bàn giao không hoạt động"));
        var handover = new BanGiaoXe();
        handover.setMaTiepNhan(id);
        handover.setNguoiBanGiao(staff);
        handover.setThoiGianBanGiao(LocalDateTime.now());
        handover.setGhiChu(request == null || request.ghiChu() == null ? null : request.ghiChu().trim());
        handovers.save(handover);
        reception.setTrangThai("DA_BAN_GIAO");
        if (reception.getDatLich() != null) reception.getDatLich().setTrangThai("HOAN_TAT");
        receptions.save(reception);

        Xe vehicle = reception.getXe();
        NguoiDung customer = vehicle.getKhachHang() == null ? null : vehicle.getKhachHang().getNguoiDung();
        if (customer != null && !Boolean.FALSE.equals(customer.getTrangThai())) {
            notifications.sendNotification(customer, "Xe đã được bàn giao",
                    "Xe " + vehicle.getBienSo() + " đã được bàn giao tại " + reception.getChiNhanh().getTenChiNhanh()
                            + ". Cảm ơn bạn đã sử dụng dịch vụ của garage.", "VEHICLE_HANDED_OVER", id);
        }
        return response(handover);
    }

    private HandoverResponse status(PhieuTiepNhan reception) {
        return handovers.findById(reception.getMaTiepNhan()).map(this::response).orElseGet(() -> {
            String reason = blockedReason(reception);
            return new HandoverResponse(reason == null, reason, null, null, null);
        });
    }

    private String blockedReason(PhieuTiepNhan reception) {
        if (Set.of("HUY", "DA_BAN_GIAO", "HOAN_TAT").contains(reception.getTrangThai())) {
            return "Phiếu tiếp nhận đã kết thúc, không thể bàn giao thêm lần nữa.";
        }
        var orders = repairs.findAllByPhieuTiepNhanMaTiepNhan(reception.getMaTiepNhan()).stream()
                .filter(o -> !"HUY".equals(o.getTrangThai())).toList();
        if (orders.isEmpty()) return "Chưa có phiếu sửa chữa để bàn giao.";
        for (var order : orders) {
            if (!"HOAN_TAT".equals(order.getTrangThai())) return "Cần hoàn tất tất cả phiếu sửa chữa trước khi bàn giao.";
            var invoice = invoices.findByPhieuSuaChuaMaPhieuSuaChua(order.getMaPhieuSuaChua());
            if (invoice.isEmpty() || !"DA_THANH_TOAN".equals(invoice.get().getTrangThai())) {
                return "Cần lập và thanh toán đầy đủ hóa đơn của tất cả phiếu sửa chữa trước khi bàn giao.";
            }
        }
        return null;
    }

    private void authorize(PhieuTiepNhan reception) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(
                a -> Set.of("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_FRONT_DESK").contains(a.getAuthority()))
                || !branches.isAllowedBranch(reception.getChiNhanh().getMaChiNhanh())) {
            throw new AccessDeniedException("Bạn không có quyền bàn giao xe tại chi nhánh này");
        }
    }

    private HandoverResponse response(BanGiaoXe handover) {
        return new HandoverResponse(false, null, handover.getThoiGianBanGiao(),
                handover.getNguoiBanGiao().getHoTen(), handover.getGhiChu());
    }
}
