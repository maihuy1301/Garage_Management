package com.garage.websocket;

import com.garage.security.BranchAuthorizationService;
import com.garage.security.CustomUserDetails;
import com.garage.security.CustomUserDetailsService;
import com.garage.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthChannelInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private WebSocketAuthChannelInterceptor interceptor;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User("technician1", "password", List.of(new SimpleGrantedAuthority("ROLE_TECHNICIAN")));
    }

    @Test
    void connect_validJwt_authenticatesSuccessfully() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.setNativeHeader("Authorization", "Bearer valid_jwt_token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(jwtService.validateToken("valid_jwt_token")).thenReturn(true);
        when(jwtService.extractUsername("valid_jwt_token")).thenReturn("technician1");
        when(userDetailsService.loadUserByUsername("technician1")).thenReturn(userDetails);

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertThat(result).isNotNull();
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo("technician1");
    }

    @Test
    void connect_missingAuthHeader_throwsCredentialsNotFound() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessageContaining("Thiếu token");
    }

    @Test
    void connect_invalidJwt_throwsBadCredentials() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer bad_token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(jwtService.validateToken("bad_token")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("không hợp lệ");
    }

    @Test
    void subscribe_unauthenticated_throwsAccessDenied() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/queue/notifications");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Yêu cầu đăng nhập");
    }

    @Test
    void subscribe_branchTopic_allowedBranch_success() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/branches/1");
        accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(branchAuthorizationService.isAllowedBranch(1)).thenReturn(true);

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertThat(result).isNotNull();
    }

    @Test
    void subscribe_branchTopic_disallowedBranch_throwsAccessDenied() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/branches/2");
        accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(branchAuthorizationService.isAllowedBranch(2)).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Bạn không có quyền subscribe");
    }

    @Test
    void subscribe_userQueue_authenticated_success() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/queue/notifications");
        accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, messageChannel);
        assertThat(result).isNotNull();
    }
}
