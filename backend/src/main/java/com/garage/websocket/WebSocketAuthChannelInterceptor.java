package com.garage.websocket;

import com.garage.security.BranchAuthorizationService;
import com.garage.security.CustomUserDetailsService;
import com.garage.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final BranchAuthorizationService branchAuthorizationService;

    public WebSocketAuthChannelInterceptor(JwtService jwtService,
                                           CustomUserDetailsService userDetailsService,
                                           BranchAuthorizationService branchAuthorizationService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            accessor = StompHeaderAccessor.wrap(message);
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticateConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            authHeader = accessor.getFirstNativeHeader("token");
        }

        if (authHeader == null || authHeader.isBlank()) {
            log.warn("WebSocket CONNECT rejected: Missing Authorization header");
            throw new AuthenticationCredentialsNotFoundException("Thiếu token xác thực trong kết nối WebSocket");
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            if (!jwtService.validateToken(token)) {
                log.warn("WebSocket CONNECT rejected: Invalid JWT token");
                throw new BadCredentialsException("Token xác thực WebSocket không hợp lệ");
            }
            String username = jwtService.extractUsername(token);
            if (username == null) {
                log.warn("WebSocket CONNECT rejected: Missing username in JWT token");
                throw new BadCredentialsException("Token xác thực WebSocket không chứa thông tin người dùng");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            accessor.setUser(authentication);
            log.info("WebSocket connection authenticated for user: {}", username);
        } catch (Exception e) {
            log.warn("WebSocket CONNECT authentication failed: {}", e.getMessage());
            throw new BadCredentialsException("Lỗi xác thực WebSocket: " + e.getMessage());
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        if (user == null || !(user instanceof Authentication auth) || !auth.isAuthenticated()) {
            log.warn("WebSocket SUBSCRIBE rejected: Unauthenticated user");
            throw new AccessDeniedException("Yêu cầu đăng nhập trước khi subscribe");
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        // Branch-specific topic subscription authorization (/topic/branches/{branchId})
        if (destination.startsWith("/topic/branches/")) {
            String branchIdStr = destination.substring("/topic/branches/".length());
            try {
                Integer branchId = Integer.valueOf(branchIdStr);
                // Set SecurityContext temporarily so branchAuthorizationService can inspect the user
                SecurityContextHolder.getContext().setAuthentication(auth);
                try {
                    if (!branchAuthorizationService.isAllowedBranch(branchId)) {
                        log.warn("WebSocket SUBSCRIBE rejected: User {} not authorized for branch {}", user.getName(), branchId);
                        throw new AccessDeniedException("Bạn không có quyền subscribe channel của chi nhánh " + branchId);
                    }
                } finally {
                    SecurityContextHolder.clearContext();
                }
            } catch (NumberFormatException e) {
                log.warn("WebSocket SUBSCRIBE rejected: Invalid branchId in destination {}", destination);
                throw new AccessDeniedException("Mã chi nhánh không hợp lệ: " + branchIdStr);
            }
        }
    }
}
