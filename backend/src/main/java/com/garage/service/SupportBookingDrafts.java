package com.garage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.SupportChatDtos.*;
import com.garage.dto.CreateAppointmentRequest;
import com.garage.entity.SupportConversation;
import com.garage.repository.*;
import com.garage.exception.BadRequestException;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SupportBookingDrafts {
    public static final String PREFIX = "AUTOCare_BOOKING_V1:";
    public record Stored(String text, BookingDraft booking, boolean ready, LocalDateTime expiresAt) {}
    private final ObjectMapper mapper;
    private final XeRepository vehicles;
    private final ChiNhanhRepository branches;
    private final ServiceCatalogService catalog;
    public SupportBookingDrafts(ObjectMapper mapper, XeRepository vehicles, ChiNhanhRepository branches, ServiceCatalogService catalog) {
        this.mapper = mapper; this.vehicles = vehicles; this.branches = branches; this.catalog = catalog;
    }
    public String prepare(SupportConversation room, BotAnswer answer) {
        var d = answer.booking();
        if (d == null || answer.handoff()) return answer.text();
        if (d.note() != null && d.note().length() > 500) throw new BadRequestException("Nhu cầu đặt lịch quá dài, vui lòng mô tả dưới 500 ký tự.");
        var vehicle = d.vehicleId() == null ? null : vehicles.findById(d.vehicleId())
                .filter(v -> Boolean.TRUE.equals(v.getTrangThai()) && v.getKhachHang().getNguoiDung() != null
                        && v.getKhachHang().getNguoiDung().getMaNguoiDung().equals(room.getCustomer().getMaNguoiDung()))
                .orElseThrow(() -> new BadRequestException("Bạn hãy chọn một xe đang hoạt động trong tài khoản của mình."));
        var branch = d.branchId() == null ? null : branches.findById(d.branchId()).filter(b -> Boolean.TRUE.equals(b.getTrangThai()))
                .orElseThrow(() -> new BadRequestException("Chi nhánh không hoạt động. Bạn muốn chọn gara nào khác?"));
        if (d.appointmentAt() != null && !d.appointmentAt().isAfter(now())) throw new BadRequestException("Thời gian này đã qua. Bạn muốn đặt ngày và giờ nào khác?");
        var ids = d.serviceIds() == null ? List.<Integer>of() : d.serviceIds();
        if (ids.size() > 20 || ids.stream().anyMatch(Objects::isNull)) throw new BadRequestException("Vui lòng chọn lại dịch vụ cần đặt.");
        var active = catalog.getAllServices(true, null);
        var names = new ArrayList<String>();
        for (Integer id : new LinkedHashSet<>(ids)) names.add(active.stream().filter(s -> id.equals(s.getMaDichVu())).findFirst()
                .orElseThrow(() -> new BadRequestException("Dịch vụ không còn hoạt động. Bạn muốn chọn dịch vụ nào khác?" )).getTenDichVu());
        boolean ready = vehicle != null && branch != null && d.appointmentAt() != null && (!names.isEmpty() || (d.note() != null && !d.note().isBlank()));
        String text;
        if (ready) {
            text = "Bạn kiểm tra yêu cầu đặt lịch nhé:\n• Xe: " + vehicle.getBienSo() + " — " + vehicle.getTenHangXe()
                + " " + vehicle.getTenModel() + "\n• Gara: " + branch.getTenChiNhanh()
                + "\n• Thời gian: " + d.appointmentAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " (giờ Việt Nam)"
                + "\n• Dịch vụ: " + (names.isEmpty() ? "Tiếp tân tư vấn theo nhu cầu" : String.join(", ", names))
                + (d.note() == null || d.note().isBlank() ? "" : "\n• Nhu cầu: " + d.note())
                + "\nNhắn “Đồng ý” hoặc “Xác nhận” để tôi gửi yêu cầu. Bạn cũng có thể nhắn thông tin muốn sửa hoặc “Hủy”. Chưa tạo lịch; tiếp tân sẽ xác nhận sau khi gửi. Bản nháp có hiệu lực 15 phút.";
        } else if (vehicle == null) text = "Bạn muốn đặt lịch cho xe nào trong tài khoản của mình?";
        else if (branch == null) text = "Bạn muốn đến chi nhánh nào? Có thể bấm Tìm gara để xem gợi ý theo lịch đặt hoặc GPS.";
        else if (d.appointmentAt() == null) text = "Bạn muốn đặt ngày nào, lúc mấy giờ?";
        else text = "Bạn muốn chọn dịch vụ nào hoặc cần garage hỗ trợ việc gì cho xe?";
        try {
            String serialized = PREFIX + mapper.writeValueAsString(new Stored(text, d, ready, now().plusMinutes(15)));
            if (serialized.length() > 2000) throw new BadRequestException("Yêu cầu quá dài. Vui lòng rút gọn nhu cầu hoặc chọn ít dịch vụ hơn.");
            return serialized;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException(e); }
    }
    public Stored decode(String content) {
        if (content == null || !content.startsWith(PREFIX)) return null;
        try { return mapper.readValue(content.substring(PREFIX.length()), Stored.class); }
        catch (Exception e) { return null; }
    }
    public String display(String content) { var stored = decode(content); return stored == null ? content : stored.text(); }
    public CreateAppointmentRequest request(Stored stored) {
        if (stored == null || !stored.ready() || !stored.expiresAt().isAfter(now()))
            throw new BadRequestException("Bản nháp chưa đủ thông tin hoặc đã hết hạn. Hãy nhắn lại yêu cầu để tôi kiểm tra trước khi đặt.");
        var d = stored.booking();
        return new CreateAppointmentRequest(d.vehicleId(), d.branchId(), d.appointmentAt(), d.note(), d.serviceIds());
    }
    public static LocalDateTime now() { return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")); }
    public static String normalize(String value) {
        return java.text.Normalizer.normalize(value.toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ','d').replaceAll("[.!?]+$", "").trim();
    }
    public static boolean confirms(String value) { return Set.of("dong y", "xac nhan", "ok", "dong y dat lich", "xac nhan dat lich").contains(normalize(value)); }
}
