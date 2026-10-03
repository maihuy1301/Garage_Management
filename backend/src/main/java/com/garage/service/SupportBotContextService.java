package com.garage.service;

import com.garage.repository.SupportConversationRepository;
import com.garage.repository.SupportMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportBotContextService {
    private final SupportConversationRepository rooms;
    private final SupportBranchService branches;
    private final SupportMessageRepository messages;
    private final com.garage.repository.KhachHangRepository customers;
    private final com.garage.repository.XeRepository vehicles;
    private final SupportBookingDrafts drafts;
    public SupportBotContextService(SupportConversationRepository rooms, SupportBranchService branches, SupportMessageRepository messages,
            com.garage.repository.KhachHangRepository customers, com.garage.repository.XeRepository vehicles, SupportBookingDrafts drafts) {
        this.rooms = rooms; this.branches = branches; this.messages = messages;
        this.customers = customers; this.vehicles = vehicles; this.drafts = drafts;
    }
    @Transactional(readOnly = true)
    public String context(Integer roomId) {
        var room = rooms.findById(roomId).orElseThrow();
        var choices = branches.forCustomer(room.getCustomer().getMaNguoiDung(), null, null);
        var text = new StringBuilder("Cuộc trò chuyện hiện thuộc chi nhánh: ")
                .append(room.getBranch().getTenChiNhanh()).append("; địa chỉ: ")
                .append(room.getBranch().getDiaChi()).append(".\nCác chi nhánh đang hoạt động:\n");
        choices.branches().stream().limit(20).forEach(b -> text.append("• branchId=").append(b.id()).append(" ").append(b.name())
                .append(" — ").append(b.address()).append("; ").append(b.reason()).append("\n"));
        text.append("Bạn có thể bấm Tìm gara để xem gợi ý theo lịch đặt hoặc chia sẻ GPS. ")
                .append("Tôi chưa có kết quả khoảng cách GPS để xác định gara gần nhất. ")
                .append("Khách có thể yêu cầu đặt tại một chi nhánh hoạt động bất kỳ; phải hỏi xác nhận chi nhánh trong bản tóm tắt.");
        text.append("\nNgày giờ hiện tại tại Việt Nam: ").append(SupportBookingDrafts.now()).append("\nXe của khách (chỉ dùng ID trong danh sách):\n");
        var customer = customers.findByNguoiDungMaNguoiDung(room.getCustomer().getMaNguoiDung()).orElseThrow();
        vehicles.findByKhachHangMaKhachHang(customer.getMaKhachHang()).stream()
                .filter(v -> Boolean.TRUE.equals(v.getTrangThai())).forEach(v -> text.append("vehicleId=")
                    .append(v.getMaXe()).append(" ").append(v.getTenHangXe()).append(" ").append(v.getTenModel())
                    .append(" (xe số ").append(v.getMaXe()).append(")\n"));
        return text.toString();
    }

    @Transactional(readOnly = true)
    public String recent(Integer roomId, Long before) {
        var recent = new java.util.ArrayList<>(messages.history(roomId, before, PageRequest.of(0, 6)));
        java.util.Collections.reverse(recent);
        var text = new StringBuilder();
        for (var message : recent) {
            if ("SYSTEM".equals(message.getSenderType()) && message.getClientId().startsWith("booking:")) {
                text.setLength(0); text.append("Yêu cầu trước đã được gửi thành công. Không tạo lại trừ khi khách yêu cầu lịch mới.\n");
                continue;
            }
            if (!java.util.Set.of("CUSTOMER", "BOT").contains(message.getSenderType())) continue;
            var stored = "BOT".equals(message.getSenderType()) ? drafts.decode(message.getContent()) : null;
            if (stored != null) {
                text.append("Bản nháp trước (chưa tạo lịch): ").append(stored.booking()).append("\n");
                continue;
            }
            text.append(message.getSenderType()).append(": ")
                    .append(message.getContent(), 0, Math.min(message.getContent().length(), 800)).append("\n");
        }
        return text.toString();
    }
}
