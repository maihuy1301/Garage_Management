package com.garage.websocket;

import com.garage.dto.RealtimeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Gửi event realtime đến private destination của một người dùng cụ thể
     * Client subscribe: /user/queue/notifications
     */
    public void sendToUser(String username, RealtimeEvent event) {
        if (username == null || event == null) {
            return;
        }
        try {
            log.info("Pushing WebSocket event to user {}: {} - {}", username, event.getEventType(), event.getMessage());
            messagingTemplate.convertAndSendToUser(username, "/queue/notifications", event);
        } catch (Exception e) {
            log.warn("Failed to push WebSocket event to user {}: {}", username, e.getMessage());
        }
    }

    /**
     * Gửi event realtime đến một destination cụ thể của người dùng
     */
    public void sendToUser(String username, String destination, RealtimeEvent event) {
        if (username == null || destination == null || event == null) {
            return;
        }
        try {
            log.info("Pushing WebSocket event to user {} at destination {}: {}", username, destination, event.getEventType());
            messagingTemplate.convertAndSendToUser(username, destination, event);
        } catch (Exception e) {
            log.warn("Failed to push WebSocket event to user {} at {}: {}", username, destination, e.getMessage());
        }
    }

    /**
     * Gửi event realtime đến channel của một chi nhánh (nếu có)
     * Client subscribe: /topic/branches/{branchCode}
     */
    public void sendToBranch(String branchCode, RealtimeEvent event) {
        if (branchCode == null || event == null) {
            return;
        }
        try {
            log.info("Pushing WebSocket event to branch {}: {}", branchCode, event.getEventType());
            messagingTemplate.convertAndSend("/topic/branches/" + branchCode, event);
        } catch (Exception e) {
            log.warn("Failed to push WebSocket event to branch {}: {}", branchCode, e.getMessage());
        }
    }
}
