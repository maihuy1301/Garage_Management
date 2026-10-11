package com.garage.service;

import com.garage.dto.RepairChatDtos.*;
import com.garage.entity.*;
import com.garage.exception.*;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RepairChatService {
    private final PhieuSuaChuaRepository repairs;
    private final PhieuTiepNhanRepository receptions;
    private final PhanCongRepository assignments;
    private final NhanVienRepository employees;
    private final NguoiDungRepository users;
    private final BranchAuthorizationService branches;
    private final RepairChatRoomRepository rooms;
    private final RepairChatMessageRepository messages;
    private final VehicleImageService images;
    private final RepairChatNotifier notifier;
    private final boolean enabled;

    public RepairChatService(PhieuSuaChuaRepository repairs, PhieuTiepNhanRepository receptions,
            PhanCongRepository assignments, NhanVienRepository employees, NguoiDungRepository users,
            BranchAuthorizationService branches, RepairChatRoomRepository rooms,
            RepairChatMessageRepository messages, VehicleImageService images, RepairChatNotifier notifier,
            @Value("${GARAGE_REPAIR_CHAT_ENABLED:false}") boolean enabled) {
        this.repairs = repairs; this.receptions = receptions; this.assignments = assignments;
        this.employees = employees; this.users = users; this.branches = branches;
        this.rooms = rooms; this.messages = messages; this.images = images; this.enabled = enabled;
        this.notifier = notifier;
    }

    public boolean isEnabled() { return enabled; }

    @Transactional(readOnly = true)
    public com.garage.dto.VehicleImageDownload image(Integer orderId) {
        var access = access(orderId, false);
        return images.readAuthorizedImage(access.reception().getXe().getMaXe());
    }

    @Transactional(readOnly = true)
    public History history(Integer orderId, Long before, Long after) {
        if ((before != null && before <= 0) || (after != null && after < 0) || (before != null && after != null))
            throw new BadRequestException("Mốc tải tin nhắn không hợp lệ");
        var access = access(orderId, false);
        var room = roomView(access);
        if ("DELETED".equals(room.state())) return new History(room, List.of(), false);
        var rows = after == null
                ? messages.older(room.receptionId(), before == null ? Long.MAX_VALUE : before, PageRequest.of(0, 41))
                : messages.newer(room.receptionId(), after, PageRequest.of(0, 41));
        var page = new ArrayList<>(rows.subList(0, Math.min(40, rows.size())));
        if (after == null) Collections.reverse(page);
        return new History(room, page.stream().map(m -> view(m, access.user())).toList(), rows.size() > 40);
    }

    @Transactional
    public Message send(Integer orderId, Send request) {
        var access = access(orderId, true);
        var room = roomView(access);
        if ("DELETED".equals(room.state())) throw new BadRequestException("Cuộc trò chuyện đã bị xóa vĩnh viễn");
        // Authorization and deletion checks precede idempotency; removed staff cannot read old retries.
        var previous = messages.findByReceptionIdAndSenderMaNguoiDungAndClientId(
                room.receptionId(), access.user().getMaNguoiDung(), request.clientId());
        if (previous.isPresent()) {
            if (!previous.get().getContent().equals(request.content().trim()))
                throw new BadRequestException("Mã gửi lại đã được dùng cho nội dung khác");
            return view(previous.get(), access.user());
        }
        if (!room.canSend()) throw new BadRequestException("Cuộc trò chuyện hiện chỉ đọc hoặc đang chờ phân công");
        if (rooms.findById(room.receptionId()).isEmpty()) {
            var created = new RepairChatRoom(); created.setReceptionId(room.receptionId());
            rooms.saveAndFlush(created);
        }
        var message = new RepairChatMessage();
        message.setReceptionId(room.receptionId()); message.setSender(access.user());
        message.setSenderRole(role("ROLE_FRONT_DESK") ? "FRONT_DESK" : role("ROLE_ADMIN") ? "ADMIN" : role("ROLE_MANAGER") ? "MANAGER" : "TECHNICIAN");
        message.setContent(request.content().trim()); message.setClientId(request.clientId());
        message.setSentAt(LocalDateTime.now());
        var saved = messages.saveAndFlush(message);
        notifier.sent(access.reception(), room.repairOrderId(), saved);
        return view(saved, access.user());
    }

    @Transactional
    public void delete(Integer orderId) {
        var access = access(orderId, true);
        if (!role("ROLE_FRONT_DESK") && !role("ROLE_ADMIN")) throw new AccessDeniedException("Chỉ tiếp tân được xóa chat");
        var room = roomView(access);
        if ("DELETED".equals(room.state())) return;
        if (!room.canDelete()) throw new BadRequestException("Chỉ được xóa chat sau khi bàn giao xe");
        messages.purge(room.receptionId());
        var marker = rooms.findById(room.receptionId()).orElseGet(RepairChatRoom::new);
        marker.setReceptionId(room.receptionId()); marker.setDeletedAt(LocalDateTime.now());
        marker.setDeletedBy(access.user().getMaNguoiDung()); rooms.save(marker);
    }

    private record Access(PhieuTiepNhan reception, List<PhieuSuaChua> orders, NguoiDung user) {}

    private Access access(Integer orderId, boolean lock) {
        if (!enabled) throw new BadRequestException("Chat sửa chữa chưa được bật");
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(role("ROLE_ADMIN") || role("ROLE_MANAGER") || role("ROLE_FRONT_DESK") || role("ROLE_TECHNICIAN")))
            throw new AccessDeniedException("Chat chỉ dành cho nhân viên");
        var user = users.findByTenDangNhapOrEmail(auth.getName(), auth.getName())
                .filter(u -> Boolean.TRUE.equals(u.getTrangThai()))
                .orElseThrow(() -> new AccessDeniedException("Tài khoản không hoạt động"));
        var order = repairs.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu sửa chữa"));
        var reception = order.getPhieuTiepNhan();
        // Same lock as handover: a send cannot commit after handover has closed this visit.
        // Take the lock BEFORE initializing the reception proxy (avoid a stale managed status).
        if (reception == null) throw new AccessDeniedException("Thiếu phiếu tiếp nhận");
        if (lock) reception = receptions.findForHandover(reception.getMaTiepNhan()).orElseThrow();
        if (!branches.isAllowedBranch(reception.getChiNhanh().getMaChiNhanh()))
            throw new AccessDeniedException("Khác chi nhánh");
        var related = repairs.findAllByPhieuTiepNhanMaTiepNhan(reception.getMaTiepNhan());
        if (!(role("ROLE_ADMIN") || role("ROLE_MANAGER") || role("ROLE_FRONT_DESK"))) {
            var employee = employees.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                    .orElseThrow(() -> new AccessDeniedException("Thiếu hồ sơ kỹ thuật viên"));
            if (related.stream().noneMatch(r -> assignments.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVienAndTrangThai(
                    r.getMaPhieuSuaChua(), employee.getMaNhanVien(), "DA_DUYET")))
                throw new AccessDeniedException("Bạn không còn được phân công cho lần sửa này");
        }
        return new Access(reception, related, user);
    }

    private Room roomView(Access access) {
        var reception = access.reception(); var vehicle = reception.getXe();
        boolean deleted = rooms.findById(reception.getMaTiepNhan()).map(r -> r.getDeletedAt() != null).orElse(false);
        boolean archived = Set.of("DA_BAN_GIAO", "HOAN_TAT").contains(reception.getTrangThai());
        boolean cancelled = "HUY".equals(reception.getTrangThai()) || access.orders().stream().allMatch(r -> "HUY".equals(r.getTrangThai()));
        boolean assigned = access.orders().stream().anyMatch(r -> !"HUY".equals(r.getTrangThai()) &&
                assignments.countByPhieuSuaChuaMaPhieuSuaChuaAndTrangThai(r.getMaPhieuSuaChua(), "DA_DUYET") > 0);
        String state = deleted ? "DELETED" : archived ? "ARCHIVED" : cancelled ? "CLOSED" : assigned ? "OPEN" : "WAITING_ASSIGNMENT";
        int rootId = access.orders().stream().filter(r -> r.getPhieuCha() == null).map(PhieuSuaChua::getMaPhieuSuaChua)
                .min(Integer::compareTo).orElse(access.orders().get(0).getMaPhieuSuaChua());
        return new Room(reception.getMaTiepNhan(), rootId, vehicle.getMaXe(), vehicle.getBienSo(),
                (Objects.toString(vehicle.getTenHangXe(), "") + " " + Objects.toString(vehicle.getTenModel(), "")).trim(),
                reception.getChiNhanh().getTenChiNhanh(), state, "OPEN".equals(state),
                "ARCHIVED".equals(state) && (role("ROLE_FRONT_DESK") || role("ROLE_ADMIN")));
    }

    private Message view(RepairChatMessage message, NguoiDung user) {
        var sender = message.getSender();
        return new Message(message.getId(), sender.getMaNguoiDung(), sender.getHoTen(), message.getSenderRole(),
                message.getContent(), message.getSentAt(), sender.getMaNguoiDung().equals(user.getMaNguoiDung()));
    }

    private boolean role(String role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}
