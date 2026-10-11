package com.garage.service;

import com.garage.entity.PhieuTiepNhan;
import com.garage.entity.RepairChatMessage;
import com.garage.repository.NhanVienRepository;
import org.springframework.stereotype.Service;
import java.util.HashSet;

/** Durable notifications; NotificationService publishes privately only after commit. */
@Service
public class RepairChatNotifier {
    private final NhanVienRepository employees;
    private final NotificationService notifications;

    public RepairChatNotifier(NhanVienRepository employees, NotificationService notifications) {
        this.employees = employees; this.notifications = notifications;
    }

    public void sent(PhieuTiepNhan reception, Integer orderId, RepairChatMessage message) {
        if (!"TECHNICIAN".equals(message.getSenderRole())) return;
        var branch = reception.getChiNhanh().getMaChiNhanh();
        var notified = new HashSet<Integer>();
        for (var employee : employees.findActiveReceptionists(branch)) {
            var recipient = employee.getNguoiDung();
            if (!Boolean.TRUE.equals(employee.getTrangThai()) || recipient == null || !Boolean.TRUE.equals(recipient.getTrangThai())
                    || employee.getChiNhanh() == null || !branch.equals(employee.getChiNhanh().getMaChiNhanh())
                    || recipient.getMaNguoiDung().equals(message.getSender().getMaNguoiDung())
                    || !notified.add(recipient.getMaNguoiDung())) continue;
            // Do not retain chat text in notifications after permanent chat deletion.
            notifications.sendNotification(recipient, "Tin nhắn sửa chữa · " + reception.getXe().getBienSo(),
                    "Kỹ thuật viên vừa gửi tin nhắn về xe " + reception.getXe().getBienSo()
                            + ", phiếu #" + orderId + ". Mở trao đổi sửa chữa để xem và trả lời.",
                    "REPAIR_CHAT_MESSAGE", orderId);
        }
    }
}
