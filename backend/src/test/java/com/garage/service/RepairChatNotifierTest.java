package com.garage.service;

import com.garage.entity.*;
import com.garage.repository.NhanVienRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;

class RepairChatNotifierTest {
    final NhanVienRepository employees = mock(NhanVienRepository.class);
    final NotificationService notifications = mock(NotificationService.class);
    final RepairChatNotifier notifier = new RepairChatNotifier(employees, notifications);

    NhanVien employee(int id, int branchId, boolean active) {
        var branch = new ChiNhanh(); branch.setMaChiNhanh(branchId);
        var user = new NguoiDung(); user.setMaNguoiDung(id); user.setTrangThai(active);
        var employee = new NhanVien(); employee.setNguoiDung(user); employee.setChiNhanh(branch); employee.setTrangThai(true);
        return employee;
    }
    PhieuTiepNhan reception() {
        var reception = new PhieuTiepNhan(); reception.setChiNhanh(employee(1, 10, true).getChiNhanh());
        var car = new Xe(); car.setBienSo("51A-12345"); reception.setXe(car); return reception;
    }
    RepairChatMessage message(String role) {
        var message = new RepairChatMessage(); message.setSender(employee(1, 10, true).getNguoiDung());
        message.setSenderRole(role); message.setContent("Nội dung nội bộ không được sao chép vào thông báo"); return message;
    }
    @Test void notifyActiveBranchReceptionistsOnceWithoutCopyingMessage() {
        var valid = employee(2, 10, true);
        when(employees.findActiveReceptionists(10)).thenReturn(List.of(valid, valid, employee(3, 20, true), employee(4, 10, false), employee(1, 10, true)));
        notifier.sent(reception(), 25, message("TECHNICIAN"));
        verify(notifications).sendNotification(eq(valid.getNguoiDung()), eq("Tin nhắn sửa chữa · 51A-12345"),
                eq("Kỹ thuật viên vừa gửi tin nhắn về xe 51A-12345, phiếu #25. Mở trao đổi sửa chữa để xem và trả lời."),
                eq("REPAIR_CHAT_MESSAGE"), eq(25));
        verifyNoMoreInteractions(notifications);
    }
    @Test void receptionistRepliesDoNotNotifyOtherReceptionists() {
        notifier.sent(reception(), 25, message("FRONT_DESK"));
        verifyNoInteractions(employees, notifications);
    }
    @Test void disabledEmployeeIsNotNotified() {
        var employee = employee(2, 10, true); employee.setTrangThai(false);
        when(employees.findActiveReceptionists(10)).thenReturn(List.of(employee));
        notifier.sent(reception(), 25, message("TECHNICIAN")); verifyNoInteractions(notifications);
    }
    @Test void noReceptionistDoesNotPreventSending() {
        when(employees.findActiveReceptionists(10)).thenReturn(List.of());
        notifier.sent(reception(), 25, message("TECHNICIAN")); verifyNoInteractions(notifications);
    }
}
