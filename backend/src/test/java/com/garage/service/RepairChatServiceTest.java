package com.garage.service;

import com.garage.dto.RepairChatDtos.Send;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairChatServiceTest {
    @Mock PhieuSuaChuaRepository repairs;
    @Mock PhieuTiepNhanRepository receptions;
    @Mock PhanCongRepository assignments;
    @Mock NhanVienRepository employees;
    @Mock NguoiDungRepository users;
    @Mock BranchAuthorizationService branches;
    @Mock RepairChatRoomRepository rooms;
    @Mock RepairChatMessageRepository messages;
    @Mock VehicleImageService images;
    @Mock RepairChatNotifier notifier;
    RepairChatService service;
    NguoiDung user;
    PhieuTiepNhan reception;
    PhieuSuaChua order;

    @BeforeEach void setup() {
        service = make(true);
        user = new NguoiDung(); user.setMaNguoiDung(5); user.setTrangThai(true); user.setHoTen("Nhân viên");
        var branch = new ChiNhanh(); branch.setMaChiNhanh(1); branch.setTenChiNhanh("Chi nhánh 1");
        var car = new Xe(); car.setMaXe(7); car.setBienSo("51A-12345");
        reception = new PhieuTiepNhan(); reception.setMaTiepNhan(10); reception.setChiNhanh(branch); reception.setXe(car);
        order = new PhieuSuaChua(); order.setMaPhieuSuaChua(20); order.setPhieuTiepNhan(reception);
    }
    RepairChatService make(boolean enabled) {
        return new RepairChatService(repairs, receptions, assignments, employees, users, branches, rooms, messages, images, notifier, enabled);
    }
    void auth(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("staff", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
    void accessible(String role, boolean lock) {
        auth(role);
        when(users.findByTenDangNhapOrEmail("staff", "staff")).thenReturn(Optional.of(user));
        when(repairs.findById(20)).thenReturn(Optional.of(order));
        if (lock) when(receptions.findForHandover(10)).thenReturn(Optional.of(reception));
        when(branches.isAllowedBranch(1)).thenReturn(true);
        when(repairs.findAllByPhieuTiepNhanMaTiepNhan(10)).thenReturn(List.of(order));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    @Test void disabledNeverTouchesDatabase() {
        service = make(false);
        assertFalse(service.isEnabled());
        assertThrows(BadRequestException.class, () -> service.history(20, null, null));
        verifyNoInteractions(repairs, rooms, messages, users);
    }
    @Test void customerCannotReadInternalChat() {
        auth("CUSTOMER");
        assertThrows(AccessDeniedException.class, () -> service.history(20, null, null));
        verifyNoInteractions(repairs, rooms, messages);
    }
    @Test void anotherBranchCannotRead() {
        auth("FRONT_DESK");
        when(users.findByTenDangNhapOrEmail("staff", "staff")).thenReturn(Optional.of(user));
        when(repairs.findById(20)).thenReturn(Optional.of(order));
        assertThrows(AccessDeniedException.class, () -> service.history(20, null, null));
        verifyNoInteractions(rooms, messages);
    }
    @Test void removedTechnicianCannotReadOrDownloadImage() {
        accessible("TECHNICIAN", false);
        var employee = new NhanVien(); employee.setMaNhanVien(9);
        when(employees.findByNguoiDungMaNguoiDung(5)).thenReturn(Optional.of(employee));
        assertThrows(AccessDeniedException.class, () -> service.history(20, null, null));
        assertThrows(AccessDeniedException.class, () -> service.image(20));
        verifyNoInteractions(rooms, messages, images);
    }
    @Test void assignmentOnAdditionalOrderSharesVisitChat() {
        accessible("TECHNICIAN", false);
        var child = new PhieuSuaChua(); child.setMaPhieuSuaChua(21); child.setPhieuCha(order);
        when(repairs.findAllByPhieuTiepNhanMaTiepNhan(10)).thenReturn(List.of(order, child));
        var employee = new NhanVien(); employee.setMaNhanVien(9);
        when(employees.findByNguoiDungMaNguoiDung(5)).thenReturn(Optional.of(employee));
        when(assignments.existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienDuocPhanCongMaNhanVienAndTrangThai(anyInt(), eq(9), eq("DA_DUYET")))
                .thenAnswer(call -> call.getArgument(0).equals(21));
        when(assignments.countByPhieuSuaChuaMaPhieuSuaChuaAndTrangThai(anyInt(), eq("DA_DUYET")))
                .thenAnswer(call -> call.getArgument(0).equals(21) ? 1L : 0L);
        var history = service.history(20, null, null);
        assertEquals(10, history.room().receptionId()); assertEquals(20, history.room().repairOrderId());
        assertTrue(history.room().canSend());
    }
    @Test void repairCompleteStillAllowsChatUntilHandover() {
        accessible("FRONT_DESK", false);
        order.setTrangThai("HOAN_TAT");
        when(assignments.countByPhieuSuaChuaMaPhieuSuaChuaAndTrangThai(20, "DA_DUYET")).thenReturn(1L);
        assertTrue(service.history(20, null, null).room().canSend());
    }
    @Test void sendAfterHandoverRejected() {
        accessible("FRONT_DESK", true); reception.setTrangThai("DA_BAN_GIAO");
        assertThrows(BadRequestException.class, () -> service.send(20, new Send("Tin mới", "request_123")));
        verify(messages, never()).saveAndFlush(any());
    }
    @Test void activeChatCannotBeDeleted() {
        accessible("FRONT_DESK", true);
        assertThrows(BadRequestException.class, () -> service.delete(20));
        verify(messages, never()).purge(any());
    }
    @Test void deletionPurgesMessagesAndKeepsOnlyAuditMarker() {
        accessible("FRONT_DESK", true); reception.setTrangThai("DA_BAN_GIAO");
        service.delete(20);
        verify(messages).purge(10);
        var saved = ArgumentCaptor.forClass(RepairChatRoom.class); verify(rooms).save(saved.capture());
        assertEquals(10, saved.getValue().getReceptionId()); assertEquals(5, saved.getValue().getDeletedBy());
        assertNotNull(saved.getValue().getDeletedAt()); verifyNoInteractions(images);
    }
    @Test void deletedChatCannotResurrectOrReturnMessages() {
        accessible("FRONT_DESK", true);
        var deleted = new RepairChatRoom(); deleted.setDeletedAt(LocalDateTime.now());
        when(rooms.findById(10)).thenReturn(Optional.of(deleted));
        assertThrows(BadRequestException.class, () -> service.send(20, new Send("Tin mới", "request_123")));
        assertEquals("DELETED", service.history(20, null, null).room().state());
        verifyNoInteractions(messages);
    }
    @Test void retryDoesNotDuplicateMessage() {
        accessible("FRONT_DESK", true);
        var previous = new RepairChatMessage(); previous.setId(3L); previous.setContent("Chào"); previous.setSender(user);
        when(messages.findByReceptionIdAndSenderMaNguoiDungAndClientId(10, 5, "request_123")).thenReturn(Optional.of(previous));
        assertEquals(3L, service.send(20, new Send("Chào", "request_123")).id());
        assertThrows(BadRequestException.class, () -> service.send(20, new Send("Khác", "request_123")));
        verify(messages, never()).saveAndFlush(any());
        verifyNoInteractions(notifier);
    }
    @Test void sendCreatesVisitRoomAndStoresOnlyServerResolvedSender() {
        accessible("FRONT_DESK", true);
        when(assignments.countByPhieuSuaChuaMaPhieuSuaChuaAndTrangThai(20, "DA_DUYET")).thenReturn(1L);
        when(messages.saveAndFlush(any())).thenAnswer(call -> {
            RepairChatMessage m = call.getArgument(0); m.setId(9L); return m;
        });
        var sent = service.send(20, new Send("  Kết quả kiểm tra  ", "request_123"));
        assertEquals("Kết quả kiểm tra", sent.content()); assertEquals(5, sent.senderId());
        assertEquals("FRONT_DESK", sent.senderRole()); assertTrue(sent.mine());
        var saved = ArgumentCaptor.forClass(RepairChatMessage.class); verify(messages).saveAndFlush(saved.capture());
        assertEquals(10, saved.getValue().getReceptionId());
        verify(rooms).saveAndFlush(any(RepairChatRoom.class));
        verify(notifier).sent(eq(reception), eq(20), same(saved.getValue()));
    }
    @Test void cannotSendBeforeApprovedAssignment() {
        accessible("FRONT_DESK", true);
        assertThrows(BadRequestException.class, () -> service.send(20, new Send("Chào", "request_123")));
        verify(messages, never()).saveAndFlush(any());
    }
    @Test void historyIsBoundedAndChronological() {
        accessible("FRONT_DESK", false);
        var rows = new ArrayList<RepairChatMessage>();
        for (long i = 41; i > 0; i--) { var m = new RepairChatMessage(); m.setId(i); m.setSender(user); rows.add(m); }
        when(messages.older(eq(10), eq(Long.MAX_VALUE), any())).thenReturn(rows);
        var page = service.history(20, null, null);
        assertTrue(page.hasMore()); assertEquals(40, page.messages().size());
        assertEquals(2L, page.messages().get(0).id()); assertEquals(41L, page.messages().get(39).id());
    }
    @Test void invalidCursorsRejectedBeforeQueries() {
        assertThrows(BadRequestException.class, () -> service.history(20, 1L, 2L));
        assertThrows(BadRequestException.class, () -> service.history(20, -1L, null));
        verifyNoInteractions(repairs, messages);
    }
}
