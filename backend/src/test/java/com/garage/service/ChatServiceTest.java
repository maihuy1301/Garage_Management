package com.garage.service;

import com.garage.dto.ChatMessageResponse;
import com.garage.dto.ConversationResponse;
import com.garage.dto.CreateConversationRequest;
import com.garage.dto.SendMessageRequest;
import com.garage.entity.*;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import com.garage.websocket.WebSocketEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private CuocHoiThoaiRepository cuocHoiThoaiRepository;

    @Mock
    private TinNhanRepository tinNhanRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private PhieuTiepNhanRepository phieuTiepNhanRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @Mock
    private WebSocketEventPublisher webSocketEventPublisher;

    @InjectMocks
    private ChatService chatService;

    private NguoiDung userCust1;
    private NguoiDung userCust2;
    private NguoiDung userStaff;
    private KhachHang cust1;
    private KhachHang cust2;
    private NhanVien staff;
    private ChiNhanh branch1;
    private CuocHoiThoai conversation1;
    private TinNhan message1;

    @BeforeEach
    void setUp() {
        userCust1 = new NguoiDung();
        userCust1.setMaNguoiDung(10);
        userCust1.setTenDangNhap("customer1");
        userCust1.setHoTen("Nguyễn Văn Khách");

        userCust2 = new NguoiDung();
        userCust2.setMaNguoiDung(20);
        userCust2.setTenDangNhap("customer2");
        userCust2.setHoTen("Lê Thị Khách");

        userStaff = new NguoiDung();
        userStaff.setMaNguoiDung(30);
        userStaff.setTenDangNhap("manager1");
        userStaff.setHoTen("Trần Quản Lý");

        cust1 = new KhachHang();
        cust1.setMaKhachHang(1);
        cust1.setNguoiDung(userCust1);

        cust2 = new KhachHang();
        cust2.setMaKhachHang(2);
        cust2.setNguoiDung(userCust2);

        branch1 = new ChiNhanh();
        branch1.setMaChiNhanh(1);

        staff = new NhanVien();
        staff.setMaNhanVien(100);
        staff.setNguoiDung(userStaff);
        staff.setChiNhanh(branch1);

        conversation1 = new CuocHoiThoai();
        conversation1.setMaCuocHoiThoai(1001);
        conversation1.setKhachHang(cust1);
        conversation1.setNhanVien(staff);
        conversation1.setTrangThai("DANG_MO");
        conversation1.setNgayCapNhatCuoi(LocalDateTime.now());

        message1 = new TinNhan();
        message1.setMaTinNhan(5001);
        message1.setCuocHoiThoai(conversation1);
        message1.setNguoiGui(userCust1);
        message1.setNoiDung("Xin chào Garage!");
        message1.setDaDoc(false);
        message1.setThoiGianGui(LocalDateTime.now());
    }

    private void stubAuth(NguoiDung user, String role) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                user.getTenDangNhap(), "pass", List.of(new SimpleGrantedAuthority("ROLE_" + role))
        ));
        SecurityContextHolder.setContext(ctx);
        when(nguoiDungRepository.findByTenDangNhapOrEmail(user.getTenDangNhap(), user.getTenDangNhap()))
                .thenReturn(Optional.of(user));
    }

    @Test
    void createConversation_customer_success() {
        stubAuth(userCust1, "CUSTOMER");
        when(khachHangRepository.findByNguoiDungMaNguoiDung(10)).thenReturn(Optional.of(cust1));
        when(cuocHoiThoaiRepository.save(any(CuocHoiThoai.class))).thenReturn(conversation1);

        CreateConversationRequest req = new CreateConversationRequest();
        req.setInitialMessage("Xin chào!");

        ConversationResponse res = chatService.createConversation(req);

        assertThat(res).isNotNull();
        assertThat(res.getMaCuocHoiThoai()).isEqualTo(1001);
        verify(cuocHoiThoaiRepository).save(any(CuocHoiThoai.class));
        verify(tinNhanRepository).save(any(TinNhan.class));
    }

    @Test
    void getUserConversations_customer_returnsList() {
        stubAuth(userCust1, "CUSTOMER");
        when(khachHangRepository.findByNguoiDungMaNguoiDung(10)).thenReturn(Optional.of(cust1));
        when(cuocHoiThoaiRepository.findByKhachHangMaKhachHangOrderByNgayCapNhatCuoiDesc(1))
                .thenReturn(List.of(conversation1));

        List<ConversationResponse> list = chatService.getUserConversations();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getMaCuocHoiThoai()).isEqualTo(1001);
    }

    @Test
    void getConversationById_customer_ownConversation_success() {
        stubAuth(userCust1, "CUSTOMER");
        when(cuocHoiThoaiRepository.findById(1001)).thenReturn(Optional.of(conversation1));

        ConversationResponse res = chatService.getConversationById(1001);

        assertThat(res).isNotNull();
        assertThat(res.getMaCuocHoiThoai()).isEqualTo(1001);
    }

    @Test
    void getConversationById_customer_otherConversation_throws403() {
        stubAuth(userCust2, "CUSTOMER");
        when(cuocHoiThoaiRepository.findById(1001)).thenReturn(Optional.of(conversation1));

        assertThatThrownBy(() -> chatService.getConversationById(1001))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("khách hàng khác");
    }

    @Test
    void getConversationMessages_member_returnsSortedList() {
        stubAuth(userCust1, "CUSTOMER");
        when(cuocHoiThoaiRepository.findById(1001)).thenReturn(Optional.of(conversation1));
        when(tinNhanRepository.findByCuocHoiThoaiMaCuocHoiThoaiOrderByThoiGianGuiAsc(1001))
                .thenReturn(List.of(message1));

        List<ChatMessageResponse> messages = chatService.getConversationMessages(1001);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getNoiDung()).isEqualTo("Xin chào Garage!");
    }

    @Test
    void sendMessage_customer_success_pushesWebSocket() {
        stubAuth(userCust1, "CUSTOMER");
        when(cuocHoiThoaiRepository.findById(1001)).thenReturn(Optional.of(conversation1));
        when(tinNhanRepository.save(any(TinNhan.class))).thenReturn(message1);

        SendMessageRequest req = new SendMessageRequest("Xe tôi đã xong chưa?");

        ChatMessageResponse res = chatService.sendMessage(1001, req);

        assertThat(res).isNotNull();
        assertThat(res.getMaTinNhan()).isEqualTo(5001);
        verify(tinNhanRepository).save(any(TinNhan.class));
        verify(webSocketEventPublisher).sendToUser(eq("manager1"), eq("/queue/chat"), any());
    }

    @Test
    void sendMessage_otherCustomer_throws403() {
        stubAuth(userCust2, "CUSTOMER");
        when(cuocHoiThoaiRepository.findById(1001)).thenReturn(Optional.of(conversation1));

        SendMessageRequest req = new SendMessageRequest("Tin nhắn giả mạo");

        assertThatThrownBy(() -> chatService.sendMessage(1001, req))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void markConversationAsRead_marksMessages() {
        stubAuth(userStaff, "ROLE_MANAGER");
        when(cuocHoiThoaiRepository.findById(1001)).thenReturn(Optional.of(conversation1));
        when(tinNhanRepository.findByCuocHoiThoaiMaCuocHoiThoaiAndDaDocFalseAndNguoiGuiMaNguoiDungNot(1001, 30))
                .thenReturn(List.of(message1));

        chatService.markConversationAsRead(1001);

        assertThat(message1.getDaDoc()).isTrue();
        verify(tinNhanRepository).saveAll(List.of(message1));
    }
}
