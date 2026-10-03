package com.garage.service;

import com.garage.dto.*;
import com.garage.dto.SupportChatDtos.*;
import com.garage.entity.*;
import com.garage.exception.*;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import com.garage.websocket.WebSocketEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SupportChatService {
    private final SupportConversationRepository rooms;
    private final SupportMessageRepository messages;
    private final SupportReadCursorRepository cursors;
    private final NguoiDungRepository users;
    private final KhachHangRepository customers;
    private final ChiNhanhRepository branches;
    private final BranchAuthorizationService authorization;
    private final WebSocketEventPublisher websocket;
    private final ApplicationEventPublisher events;
    private final AppointmentService appointments;
    private final NotificationService notifications;
    private final SupportBookingDrafts drafts;
    private final boolean enabled;

    public SupportChatService(SupportConversationRepository rooms, SupportMessageRepository messages,
            SupportReadCursorRepository cursors, NguoiDungRepository users, KhachHangRepository customers,
            ChiNhanhRepository branches, BranchAuthorizationService authorization,
            WebSocketEventPublisher websocket, ApplicationEventPublisher events, AppointmentService appointments, NotificationService notifications,
            @Value("${garage.support.enabled:false}") boolean enabled, SupportBookingDrafts drafts) {
        this.rooms = rooms; this.messages = messages; this.cursors = cursors; this.users = users;
        this.customers = customers; this.branches = branches; this.authorization = authorization;
        this.websocket = websocket; this.events = events; this.appointments = appointments; this.notifications = notifications; this.enabled = enabled;
        this.drafts = drafts;
    }

    public boolean isEnabled() { return enabled; }

    @Transactional
    public Conversation open(Integer branchId) {
        requireCustomer();
        var user = currentUser();
        if (customers.findByNguoiDungMaNguoiDung(user.getMaNguoiDung()).isEmpty())
            throw new AccessDeniedException("Thiếu hồ sơ khách hàng");
        var branch = branches.findById(branchId).filter(b -> Boolean.TRUE.equals(b.getTrangThai()))
                .orElseThrow(() -> new BadRequestException("Chi nhánh không hoạt động"));
        // Serialize first-open requests from multiple devices; unique index is the second guard.
        rooms.lockCustomer(user.getMaNguoiDung()).orElseThrow();
        var room = rooms.findByCustomerMaNguoiDungAndBranchMaChiNhanh(user.getMaNguoiDung(), branchId).orElse(null);
        if (room == null) {
            room = new SupportConversation(); room.setCustomer(user); room.setBranch(branch);
            room.setStatus("BOT"); room.setUpdatedAt(LocalDateTime.now()); room.setLastMessageId(0L);
            rooms.saveAndFlush(room);
            append(room, null, "BOT", "Xin chào! Tôi là trợ lý tự động AutoCare. Bạn có thể nói nhu cầu, xe và thời gian mong muốn; tôi sẽ giúp chuẩn bị yêu cầu đặt lịch ngay trong chat và hỏi bạn xác nhận trước khi gửi. Bạn cũng có thể tự điền bằng nút Đặt lịch hoặc chọn Gặp tiếp tân.", "welcome");
        }
        return view(room, user.getMaNguoiDung());
    }

    @Transactional(readOnly = true)
    public List<Conversation> list() {
        var user = currentUser();
        List<SupportConversation> found;
        if (customer()) found = rooms.findByCustomerMaNguoiDungOrderByUpdatedAtDesc(user.getMaNguoiDung());
        else if (role("ROLE_ADMIN")) found = rooms.findAllByOrderByUpdatedAtDesc();
        else {
            requireStaff();
            var branch = authorization.resolveCurrentUserBranchId().orElseThrow(() -> new AccessDeniedException("Thiếu chi nhánh"));
            found = rooms.findByBranchMaChiNhanhOrderByUpdatedAtDesc(branch);
        }
        return found.stream().map(c -> view(c, user.getMaNguoiDung())).toList();
    }

    @Transactional(readOnly = true)
    public History history(Integer id, Long before) {
        access(find(id, false));
        var rows = messages.history(id, before == null ? Long.MAX_VALUE : before, PageRequest.of(0, 51));
        boolean more = rows.size() > 50;
        var page = new ArrayList<>(rows.subList(0, Math.min(50, rows.size())));
        Collections.reverse(page);
        return new History(page.stream().map(this::messageView).toList(), more);
    }

    @Transactional
    public Message send(Integer id, Send request) {
        var user = currentUser(); var room = find(id, true); access(room);
        String clientId = user.getMaNguoiDung() + ":" + request.clientId();
        var previous = messages.findByConversationIdAndClientId(id, clientId);
        if (previous.isPresent()) {
            if (!previous.get().getContent().equals(request.content().trim()))
                throw new DuplicateResourceException("Mã gửi đã được dùng cho tin nhắn khác");
            return messageView(previous.get());
        }
        if (!customer() && (!"HUMAN".equals(room.getStatus()) || room.getAgent() == null ||
                !room.getAgent().getMaNguoiDung().equals(user.getMaNguoiDung())))
            throw new AccessDeniedException("Hãy nhận hỗ trợ trước khi trả lời");
        // One bounded outstanding generation per room; stale work expires after a restart.
        if (customer() && "BOT".equals(room.getStatus()) && room.getPendingBotMessageId() != null &&
                room.getUpdatedAt().isAfter(LocalDateTime.now().minusSeconds(45)))
            throw new DuplicateResourceException("Trợ lý đang trả lời. Bạn có thể chờ hoặc gặp tiếp tân.");
        // Confirmation is interpreted by server code, never by an AI-generated action flag.
        if (customer() && "BOT".equals(room.getStatus()) && SupportBookingDrafts.confirms(request.content())) {
            var latest = room.getLastMessageId() == null ? null : messages.findById(room.getLastMessageId()).orElse(null);
            var stored = latest != null && "BOT".equals(latest.getSenderType())
                    && latest.getConversation().getId().equals(id) ? drafts.decode(latest.getContent()) : null;
            if (stored != null && stored.ready()) {
                var booking = drafts.request(stored);
                // Revalidate all draft fields against current ownership/catalog before creating.
                drafts.prepare(room, new BotAnswer("", false, stored.booking()));
                var confirmation = append(room, user, "CUSTOMER", request.content().trim(), clientId);
                createBooking(room, "draft-" + latest.getId(), booking);
                return messageView(confirmation);
            }
            var confirmation = append(room, user, "CUSTOMER", request.content().trim(), clientId);
            append(room, null, "BOT", "Chưa có bản tóm tắt đầy đủ đang chờ xác nhận. Bạn hãy cho tôi biết xe, chi nhánh, ngày giờ và nhu cầu đặt lịch nhé.", "bot:" + confirmation.getId());
            return messageView(confirmation);
        }
        if (customer() && "BOT".equals(room.getStatus()) && Set.of("huy", "huy dat lich", "khong dong y").contains(SupportBookingDrafts.normalize(request.content()))) {
            var cancellation = append(room, user, "CUSTOMER", request.content().trim(), clientId);
            append(room, null, "BOT", "Đã bỏ yêu cầu đặt lịch đang trao đổi, chưa tạo lịch mới. Các lịch đã tạo trước đó không bị hủy. Bạn muốn tôi hỗ trợ gì tiếp?", "bot:" + cancellation.getId());
            return messageView(cancellation);
        }
        var message = append(room, user, customer() ? "CUSTOMER" : "STAFF", request.content().trim(), clientId);
        if (customer() && "BOT".equals(room.getStatus())) {
            room.setPendingBotMessageId(message.getId());
            events.publishEvent(new BotRequested(id, message.getId(), message.getContent()));
        }
        return messageView(message);
    }

    @Transactional
    public Conversation handoff(Integer id) {
        requireCustomer(); var room = find(id, true); access(room);
        if (!"WAITING".equals(room.getStatus())) {
            room.setStatus("WAITING"); room.setAgent(null); room.setPendingBotMessageId(null);
            notifyRoomChanged(room);
        }
        return view(room, currentUser().getMaNguoiDung());
    }

    @Transactional
    public Conversation claim(Integer id) {
        requireStaff(); var user = currentUser(); var room = find(id, true); access(room);
        if ("HUMAN".equals(room.getStatus()) && room.getAgent() != null &&
                room.getAgent().getMaNguoiDung().equals(user.getMaNguoiDung())) return view(room, user.getMaNguoiDung());
        if (!"WAITING".equals(room.getStatus())) throw new DuplicateResourceException("Cuộc trò chuyện không còn chờ tiếp nhận");
        room.setAgent(user); room.setStatus("HUMAN"); room.setPendingBotMessageId(null);
        notifyRoomChanged(room);
        return view(room, user.getMaNguoiDung());
    }

    @Transactional
    public Conversation resolve(Integer id) {
        requireStaff(); var user = currentUser(); var room = find(id, true); access(room);
        if (room.getAgent() == null || !room.getAgent().getMaNguoiDung().equals(user.getMaNguoiDung()))
            throw new AccessDeniedException("Chỉ người đang phụ trách được kết thúc hỗ trợ");
        room.setStatus("BOT"); room.setAgent(null); room.setPendingBotMessageId(null);
        notifyRoomChanged(room);
        return view(room, user.getMaNguoiDung());
    }

    @Transactional
    public Conversation clear(Integer id) {
        var user = currentUser(); var room = find(id, true); access(room);
        messages.deleteByConversationId(id);
        cursors.deleteByConversationId(id);
        room.setLastMessageId(0L);
        room.setStatus("BOT");
        room.setAgent(null);
        room.setPendingBotMessageId(null);
        notifyRoomChanged(room);
        return view(room, user.getMaNguoiDung());
    }

    @Transactional
    public void read(Integer id, Long lastReadId) {
        var room = find(id, true); access(room); var user = currentUser();
        long target = Math.min(lastReadId, room.getLastMessageId() == null ? 0L : room.getLastMessageId());
        var cursor = cursors.findByConversationIdAndUserId(id, user.getMaNguoiDung()).orElseGet(() -> {
            var c = new SupportReadCursor(); c.setConversationId(id); c.setUserId(user.getMaNguoiDung()); c.setLastReadId(0L); return c;
        });
        if (target <= cursor.getLastReadId()) return;
        cursor.setLastReadId(target); cursors.save(cursor);
        String username = user.getTenDangNhap();
        afterCommit(() -> websocket.sendToUser(username, "/queue/support-chat", RealtimeEvent.of("SUPPORT_READ", "SUPPORT_CHAT", id, "Đã đọc", null)));
    }

    @Transactional
    public AppointmentResponse book(Integer id, String requestId, CreateAppointmentRequest request) {
        requireCustomer(); var room = find(id, true); access(room);
        if (!room.getBranch().getMaChiNhanh().equals(request.getMaChiNhanh()))
            throw new BadRequestException("Hãy đặt lịch tại chi nhánh của cuộc trò chuyện");
        return createBooking(room, requestId, request);
    }

    private AppointmentResponse createBooking(SupportConversation room, String requestId, CreateAppointmentRequest request) {
        Integer id = room.getId();
        if (request.getMaDichVuList() != null && (request.getMaDichVuList().size() > 100 || request.getMaDichVuList().stream().anyMatch(Objects::isNull)))
            throw new BadRequestException("Danh sách dịch vụ không hợp lệ");
        String key = "booking:" + requestId;
        String fingerprint = bookingFingerprint(request);
        var previous = messages.findByConversationIdAndClientId(id, key);
        if (previous.isPresent()) {
            if (!fingerprint.equals(previous.get().getRequestHash())) throw new DuplicateResourceException("Mã đặt lịch đã được dùng cho nội dung khác");
            return appointments.getAppointmentById(previous.get().getAppointmentId());
        }
        request.setMaKhachHang(null);
        var result = appointments.createAppointment(request);
        var marker = append(room, null, "SYSTEM", "Yêu cầu đặt lịch #" + result.getMaDatLich() + " đã được gửi. Vui lòng chờ tiếp tân xác nhận lịch.", key);
        marker.setAppointmentId(result.getMaDatLich()); marker.setRequestHash(fingerprint);
        return result;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void completeBot(BotRequested request, BotAnswer answer) {
        var room = rooms.lockById(request.conversationId()).orElse(null);
        if (room == null || !"BOT".equals(room.getStatus()) ||
                !Objects.equals(room.getPendingBotMessageId(), request.messageId())) return;
        room.setPendingBotMessageId(null);
        if (answer.handoff()) { room.setStatus("WAITING"); room.setAgent(null); }
        String content = answer.text();
        if (answer.booking() != null && !answer.handoff()) {
            try { content = drafts.prepare(room, answer); }
            catch (BadRequestException e) { content = e.getMessage(); }
        }
        append(room, null, "BOT", content, "bot:" + request.messageId());
    }

    private SupportMessage append(SupportConversation room, NguoiDung sender, String type, String text, String clientId) {
        var m = new SupportMessage(); m.setConversation(room); m.setSender(sender); m.setSenderType(type);
        m.setContent(text); m.setClientId(clientId); m.setCreatedAt(LocalDateTime.now());
        messages.saveAndFlush(m); room.setLastMessageId(m.getId()); room.setUpdatedAt(m.getCreatedAt());
        rooms.save(room);
        if ("STAFF".equals(type)) {
            notifications.sendNotification(room.getCustomer(), "Tiếp tân đã trả lời",
                    "Bạn có tin nhắn mới trong Tư vấn & đặt lịch tại " + room.getBranch().getTenChiNhanh() + ".",
                    "SUPPORT_CHAT", room.getId());
        }
        // Only authorized staff receive an invalidation, never broadcast chat content to a branch topic.
        var recipients = new HashSet<>(rooms.supportUsernames(room.getBranch().getMaChiNhanh()));
        recipients.add(room.getCustomer().getTenDangNhap());
        if (room.getAgent() != null) recipients.add(room.getAgent().getTenDangNhap());
        Integer id = room.getId();
        afterCommit(() -> recipients.forEach(name -> websocket.sendToUser(name, "/queue/support-chat",
                RealtimeEvent.of("SUPPORT_CHANGED", "SUPPORT_CHAT", id, "Cuộc trò chuyện đã cập nhật", null))));
        return m;
    }

    private void notifyRoomChanged(SupportConversation room) {
        room.setUpdatedAt(LocalDateTime.now());
        rooms.save(room);
        var recipients = new HashSet<>(rooms.supportUsernames(room.getBranch().getMaChiNhanh()));
        recipients.add(room.getCustomer().getTenDangNhap());
        if (room.getAgent() != null) recipients.add(room.getAgent().getTenDangNhap());
        Integer id = room.getId();
        afterCommit(() -> recipients.forEach(name -> websocket.sendToUser(name, "/queue/support-chat",
                RealtimeEvent.of("SUPPORT_CHANGED", "SUPPORT_CHAT", id, "Cuộc trò chuyện đã cập nhật", null))));
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { action.run(); }
            });
        }
    }

    private Conversation view(SupportConversation c, Integer userId) {
        long cursor = cursors.findByConversationIdAndUserId(c.getId(), userId).map(SupportReadCursor::getLastReadId).orElse(0L);
        return new Conversation(c.getId(), c.getBranch().getMaChiNhanh(), c.getBranch().getTenChiNhanh(),
                c.getCustomer().getMaNguoiDung(), c.getCustomer().getHoTen(),
                c.getAgent() == null ? null : c.getAgent().getMaNguoiDung(), c.getAgent() == null ? null : c.getAgent().getHoTen(),
                c.getStatus(), c.getUpdatedAt(), c.getLastMessageId(), messages.unread(c.getId(), cursor, userId),
                c.getPendingBotMessageId() != null && c.getUpdatedAt().isAfter(LocalDateTime.now().minusSeconds(45)));
    }
    private Message messageView(SupportMessage m) {
        return new Message(m.getId(), m.getConversation().getId(), m.getSender() == null ? null : m.getSender().getMaNguoiDung(),
                m.getSender() == null ? ("BOT".equals(m.getSenderType()) ? "Trợ lý tự động" : "AutoCare") : m.getSender().getHoTen(),
                m.getSenderType(), "BOT".equals(m.getSenderType()) && m.getContent().startsWith(SupportBookingDrafts.PREFIX)
                        ? drafts.display(m.getContent()) : m.getContent(), m.getCreatedAt());
    }
    private SupportConversation find(Integer id, boolean lock) {
        currentUser();
        return (lock ? rooms.lockById(id) : rooms.findById(id)).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));
    }
    private void access(SupportConversation room) {
        if (customer()) {
            if (!room.getCustomer().getMaNguoiDung().equals(currentUser().getMaNguoiDung())) throw new AccessDeniedException("Không phải cuộc trò chuyện của bạn");
        } else {
            requireStaff();
            if (!authorization.isAllowedBranch(room.getBranch().getMaChiNhanh())) throw new AccessDeniedException("Khác chi nhánh");
        }
    }
    private NguoiDung currentUser() {
        if (!enabled) throw new BadRequestException("Chat hỗ trợ chưa được bật. Vui lòng đặt lịch tại mục Đặt lịch.");
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new AccessDeniedException("Yêu cầu đăng nhập");
        if (!customer() && !role("ROLE_ADMIN") && !role("ROLE_MANAGER") && !role("ROLE_FRONT_DESK")) throw new AccessDeniedException("Không có quyền hỗ trợ khách");
        return users.findByTenDangNhapOrEmail(auth.getName(), auth.getName()).filter(u -> Boolean.TRUE.equals(u.getTrangThai()))
                .orElseThrow(() -> new AccessDeniedException("Tài khoản không hoạt động"));
    }
    private boolean role(String name) {
        var a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(r -> name.equals(r.getAuthority()));
    }
    private String bookingFingerprint(CreateAppointmentRequest request) {
        try {
            String canonical = request.getMaXe() + "|" + request.getMaChiNhanh() + "|" + request.getThoiGianHen() + "|" +
                    Objects.toString(request.getGhiChu(), "") + "|" +
                    (request.getMaDichVuList() == null ? "[]" : request.getMaDichVuList().stream().sorted().toList().toString());
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private boolean customer() { return role("ROLE_CUSTOMER"); }
    private void requireCustomer() { currentUser(); if (!customer()) throw new AccessDeniedException("Chỉ khách hàng"); }
    private void requireStaff() { currentUser(); if (customer()) throw new AccessDeniedException("Chỉ tiếp tân/quản lý"); }
}
