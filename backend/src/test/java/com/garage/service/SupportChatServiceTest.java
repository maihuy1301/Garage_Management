package com.garage.service;

import com.garage.dto.*;
import com.garage.dto.SupportChatDtos.*;
import com.garage.entity.*;
import com.garage.exception.*;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import com.garage.websocket.WebSocketEventPublisher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class SupportChatServiceTest {
    @Mock SupportConversationRepository rooms;
    @Mock SupportMessageRepository messages;
    @Mock SupportReadCursorRepository cursors;
    @Mock NguoiDungRepository users;
    @Mock KhachHangRepository customers;
    @Mock ChiNhanhRepository branches;
    @Mock BranchAuthorizationService authorization;
    @Mock WebSocketEventPublisher websocket;
    @Mock ApplicationEventPublisher events;
    @Mock AppointmentService appointments;
    @Mock NotificationService notifications;
    @Mock SupportBookingDrafts drafts;
    SupportChatService service;
    NguoiDung customer, agent;
    SupportConversation room;
    @BeforeEach void setup() {
        service = new SupportChatService(rooms,messages,cursors,users,customers,branches,authorization,websocket,events,appointments,notifications,true,drafts);
        customer = user(10,"customer"); agent = user(20,"staff");
        var branch = new ChiNhanh(); branch.setMaChiNhanh(1); branch.setTenChiNhanh("Chi nhánh 1"); branch.setTrangThai(true);
        room = new SupportConversation(); room.setId(1); room.setCustomer(customer); room.setBranch(branch); room.setStatus("BOT"); room.setLastMessageId(0L); room.setUpdatedAt(LocalDateTime.now());
    }
    NguoiDung user(int id, String name) { var u = new NguoiDung(); u.setMaNguoiDung(id); u.setTenDangNhap(name); u.setHoTen(name); u.setTrangThai(true); return u; }
    void auth(NguoiDung user, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getTenDangNhap(), "", List.of(new SimpleGrantedAuthority(role))));
        lenient().when(users.findByTenDangNhapOrEmail(user.getTenDangNhap(),user.getTenDangNhap())).thenReturn(Optional.of(user));
    }
    void lock() { when(rooms.lockById(1)).thenReturn(Optional.of(room)); }
    void writes() {
        when(messages.saveAndFlush(any())).thenAnswer(i -> { SupportMessage m = i.getArgument(0); m.setId(100L); return m; });
        when(rooms.supportUsernames(1)).thenReturn(List.of("staff"));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); if (TransactionSynchronizationManager.isSynchronizationActive()) TransactionSynchronizationManager.clearSynchronization(); }

    @Test void otherCustomerCannotReadHistory() {
        auth(user(11,"other"),"ROLE_CUSTOMER"); when(rooms.findById(1)).thenReturn(Optional.of(room));
        assertThrows(AccessDeniedException.class, () -> service.history(1,null)); verifyNoInteractions(messages);
    }
    @Test void technicianCannotReadCustomerChat() {
        auth(agent,"ROLE_TECHNICIAN"); assertThrows(AccessDeniedException.class, () -> service.list()); verifyNoInteractions(rooms);
    }
    @Test void crossBranchStaffCannotClaim() {
        auth(agent,"ROLE_FRONT_DESK"); lock(); room.setStatus("WAITING");
        when(authorization.isAllowedBranch(1)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.claim(1)); verifyNoInteractions(messages);
    }
    @Test void staffMustClaimBeforeSending() {
        auth(agent,"ROLE_FRONT_DESK"); lock(); when(authorization.isAllowedBranch(1)).thenReturn(true);
        assertThrows(AccessDeniedException.class, () -> service.send(1,new Send("hello","request-1"))); verify(messages,never()).saveAndFlush(any());
    }
    @Test void claimStopsBotAndRejectsSecondAgent() {
        auth(agent,"ROLE_FRONT_DESK"); lock(); when(authorization.isAllowedBranch(1)).thenReturn(true);
        room.setStatus("WAITING"); room.setPendingBotMessageId(12L);
        var response = service.claim(1); assertEquals("HUMAN",response.status()); assertEquals(20,response.agentId()); assertNull(room.getPendingBotMessageId());
        auth(user(21,"otherStaff"),"ROLE_FRONT_DESK");
        assertThrows(DuplicateResourceException.class, () -> service.claim(1));
    }
    @Test void sameClaimIsIdempotent() {
        auth(agent,"ROLE_FRONT_DESK"); lock(); when(authorization.isAllowedBranch(1)).thenReturn(true); room.setStatus("HUMAN"); room.setAgent(agent);
        assertEquals(20,service.claim(1).agentId()); verify(messages,never()).saveAndFlush(any());
    }
    @Test void handoffCancelsPendingGeneration() {
        auth(customer,"ROLE_CUSTOMER"); lock(); room.setPendingBotMessageId(8L);
        assertEquals("WAITING", service.handoff(1).status()); assertNull(room.getPendingBotMessageId());
        verify(messages,never()).saveAndFlush(any());
    }
    @Test void lateBotResponseCannotInterruptHuman() {
        lock(); room.setStatus("HUMAN"); room.setPendingBotMessageId(null);
        service.completeBot(new BotRequested(1,12L,"hello"),new BotAnswer("late",false)); verifyNoInteractions(messages);
    }
    @Test void staleBotResponseCannotAnswerNewerMessage() {
        lock(); room.setPendingBotMessageId(13L);
        service.completeBot(new BotRequested(1,12L,"hello"),new BotAnswer("late",false)); verifyNoInteractions(messages);
    }
    @Test void botFailureChangesRoomToWaiting() {
        lock(); writes(); room.setPendingBotMessageId(12L);
        service.completeBot(new BotRequested(1,12L,"hello"),SupportBotService.handoff());
        assertEquals("WAITING",room.getStatus()); assertNull(room.getPendingBotMessageId());
    }
    @Test void sendIsPersistedBeforePrivateRealtimeAndQueuesBot() {
        auth(customer,"ROLE_CUSTOMER"); lock(); writes();
        TransactionSynchronizationManager.initSynchronization();
        var sent = service.send(1,new Send("Xin chào","request-1"));
        assertEquals(100L,sent.id()); assertEquals(100L,room.getPendingBotMessageId());
        verify(events).publishEvent(new BotRequested(1,100L,"Xin chào")); verifyNoInteractions(websocket);
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
        verify(websocket).sendToUser(eq("customer"),eq("/queue/support-chat"),any());
        verify(websocket).sendToUser(eq("staff"),eq("/queue/support-chat"),any());
        verify(websocket,never()).sendToBranch(anyInt(),any());
    }
    @Test void retryDoesNotCreateDuplicateOrInvokeBotTwice() {
        auth(customer,"ROLE_CUSTOMER"); lock();
        var m = new SupportMessage(); m.setId(9L); m.setConversation(room); m.setSender(customer); m.setSenderType("CUSTOMER"); m.setContent("hello");
        when(messages.findByConversationIdAndClientId(1,"10:request-1")).thenReturn(Optional.of(m));
        assertEquals(9L,service.send(1,new Send("hello","request-1")).id()); verifyNoInteractions(events); verify(messages,never()).saveAndFlush(any());
        assertThrows(DuplicateResourceException.class, () -> service.send(1,new Send("different","request-1")));
    }
    @Test void readCursorNeverMovesBackAndIsBounded() {
        auth(customer,"ROLE_CUSTOMER"); lock(); room.setLastMessageId(30L);
        var cursor = new SupportReadCursor(); cursor.setLastReadId(20L);
        when(cursors.findByConversationIdAndUserId(1,10)).thenReturn(Optional.of(cursor));
        service.read(1,10L); assertEquals(20L,cursor.getLastReadId()); verify(cursors,never()).save(any());
        service.read(1,999L); assertEquals(30L,cursor.getLastReadId());
    }
    @Test void bookingCannotChangeConversationBranch() {
        auth(customer,"ROLE_CUSTOMER"); lock();
        assertThrows(BadRequestException.class, () -> service.book(1,"request-1",new CreateAppointmentRequest(5,2,LocalDateTime.now().plusDays(1),"")));
        verifyNoInteractions(appointments);
    }
    @Test void bookingUsesAuthoritativeAppointmentServiceAndRecordsPendingConfirmation() {
        auth(customer,"ROLE_CUSTOMER"); lock(); writes();
        var req = new CreateAppointmentRequest(5,1,LocalDateTime.now().plusDays(1),""); req.setMaKhachHang(999);
        var result = new AppointmentResponse(); result.setMaDatLich(42); when(appointments.createAppointment(req)).thenReturn(result);
        assertEquals(42,service.book(1,"request-1",req).getMaDatLich()); assertNull(req.getMaKhachHang());
        var captor = ArgumentCaptor.forClass(SupportMessage.class); verify(messages).saveAndFlush(captor.capture());
        assertEquals(42,captor.getValue().getAppointmentId()); assertTrue(captor.getValue().getContent().contains("chờ tiếp tân"));
        when(messages.findByConversationIdAndClientId(1,"booking:request-1")).thenReturn(Optional.of(captor.getValue()));
        when(appointments.getAppointmentById(42)).thenReturn(result);
        assertEquals(42,service.book(1,"request-1",req).getMaDatLich()); verify(appointments,times(1)).createAppointment(any());
        req.setMaXe(6); assertThrows(DuplicateResourceException.class, () -> service.book(1,"request-1",req));
    }
    @Test void openReusesRoomAndRejectsInactiveBranch() {
        auth(customer,"ROLE_CUSTOMER"); when(customers.findByNguoiDungMaNguoiDung(10)).thenReturn(Optional.of(new KhachHang()));
        when(branches.findById(1)).thenReturn(Optional.of(room.getBranch())); when(rooms.lockCustomer(10)).thenReturn(Optional.of(customer));
        when(rooms.findByCustomerMaNguoiDungAndBranchMaChiNhanh(10,1)).thenReturn(Optional.of(room));
        assertEquals(1,service.open(1).id()); verify(messages,never()).saveAndFlush(any());
        room.getBranch().setTrangThai(false); assertThrows(BadRequestException.class, () -> service.open(1));
    }
    @Test void employeeMessageCreatesCustomerNotification() {
        auth(agent,"ROLE_FRONT_DESK"); lock(); writes(); when(authorization.isAllowedBranch(1)).thenReturn(true);
        room.setStatus("HUMAN"); room.setAgent(agent);
        service.send(1,new Send("Chào bạn","request-1"));
        verify(notifications).sendNotification(eq(customer),anyString(),anyString(),eq("SUPPORT_CHAT"),eq(1));
        verifyNoInteractions(events);
    }
    @Test void confirmedDraftCreatesRealBookingAndSecondConfirmationDoesNotDuplicate() {
        auth(customer,"ROLE_CUSTOMER"); lock(); writes();
        var draft = new BookingDraft(5,2,LocalDateTime.now().plusDays(1),List.of(),"Bảo dưỡng");
        var stored = new SupportBookingDrafts.Stored("summary",draft,true,LocalDateTime.now().plusMinutes(10));
        var last = new SupportMessage(); last.setId(90L); last.setConversation(room); last.setSenderType("BOT"); last.setContent("stored");
        room.setLastMessageId(90L); when(messages.findById(90L)).thenReturn(Optional.of(last));
        when(drafts.decode("stored")).thenReturn(stored);
        var req = new CreateAppointmentRequest(5,2,draft.appointmentAt(),draft.note(),List.of());
        when(drafts.request(stored)).thenReturn(req);
        var result = new AppointmentResponse(); result.setMaDatLich(42); when(appointments.createAppointment(req)).thenReturn(result);
        service.send(1,new Send("Đồng ý","confirm-1"));
        verify(appointments).createAppointment(req); verifyNoInteractions(events);
        var saved = ArgumentCaptor.forClass(SupportMessage.class); verify(messages,times(2)).saveAndFlush(saved.capture());
        assertEquals(42,saved.getAllValues().get(1).getAppointmentId());
        assertEquals("booking:draft-90",saved.getAllValues().get(1).getClientId());
        service.send(1,new Send("Đồng ý","confirm-2"));
        verify(appointments,times(1)).createAppointment(any());
    }
    @Test void confirmationWithoutDraftDoesNotAskAiToBook() {
        auth(customer,"ROLE_CUSTOMER"); lock(); writes();
        service.send(1,new Send("Đồng ý","confirm-1"));
        verifyNoInteractions(appointments,events);
    }
    @Test void cancellationDoesNotCreateOrCancelExistingAppointment() {
        auth(customer,"ROLE_CUSTOMER"); lock(); writes();
        service.send(1,new Send("Hủy","cancel-1"));
        verifyNoInteractions(appointments,events);
    }
    @Test void botProposalOnlyPersistsDraftAndNeverCreatesAppointment() {
        lock(); writes(); room.setPendingBotMessageId(50L);
        var answer = new BotAnswer("summary",false,new BookingDraft(5,1,LocalDateTime.now().plusDays(1),List.of(),"Bảo dưỡng"));
        when(drafts.prepare(room,answer)).thenReturn("stored draft");
        service.completeBot(new BotRequested(1,50L,"Đặt lịch"),answer);
        verifyNoInteractions(appointments);
        verify(drafts).prepare(room,answer);
    }
}
