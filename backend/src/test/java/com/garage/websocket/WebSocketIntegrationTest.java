package com.garage.websocket;

import com.garage.dto.RealtimeEvent;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private WebSocketEventPublisher webSocketEventPublisher;

    @MockBean
    private NguoiDungRepository nguoiDungRepository;

    @MockBean
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    private WebSocketStompClient stompClient;
    private String wsUrl;

    @BeforeEach
    void setUp() {
        wsUrl = "ws://localhost:" + port + "/ws";
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        stompClient = new WebSocketStompClient(new SockJsClient(transports));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        NguoiDung user = new NguoiDung();
        user.setMaNguoiDung(1);
        user.setTenDangNhap("technician1");
        user.setHoTen("Trần Kỹ Thuật");
        user.setEmail("tech1@garage.com");
        user.setMatKhauHash("$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO");
        user.setTrangThai(true);

        VaiTro role = new VaiTro();
        role.setMaVaiTro(1);
        role.setTenVaiTro("ROLE_TECHNICIAN");

        when(nguoiDungRepository.findByTenDangNhapOrEmail("technician1", "technician1"))
                .thenReturn(Optional.of(user));
        when(nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(1))
                .thenReturn(List.of(new NguoiDungVaiTro(user, role)));
    }

    @Test
    void connect_withValidJwt_connectsSuccessfully() throws Exception {
        String token = jwtService.generateToken("technician1", List.of("ROLE_TECHNICIAN"));
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        CompletableFuture<StompSession> sessionFuture = new CompletableFuture<>();

        stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionFuture.complete(session);
            }
        });

        StompSession session = sessionFuture.get(5, TimeUnit.SECONDS);
        assertThat(session).isNotNull();
        assertThat(session.isConnected()).isTrue();

        session.disconnect();
    }

    @Test
    void connect_withoutJwt_connectionFails() {
        StompHeaders connectHeaders = new StompHeaders(); // No Authorization header

        CompletableFuture<StompSession> sessionFuture = new CompletableFuture<>();

        stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionFuture.complete(session);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                sessionFuture.completeExceptionally(exception);
            }
        });

        assertThatThrownBy(() -> sessionFuture.get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class);
    }
}
