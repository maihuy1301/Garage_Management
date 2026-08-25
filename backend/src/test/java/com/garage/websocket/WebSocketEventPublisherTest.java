package com.garage.websocket;

import com.garage.dto.RealtimeEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketEventPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketEventPublisher publisher;

    @Test
    void sendToUser_convertsAndSendsToUserDestination() {
        RealtimeEvent event = RealtimeEvent.of("APPOINTMENT_CONFIRMED", "DAT_LICH", 1, "Lịch hẹn đã xác nhận", null);

        publisher.sendToUser("customer1", event);

        verify(messagingTemplate).convertAndSendToUser("customer1", "/queue/notifications", event);
    }

    @Test
    void sendToUser_withSpecificDestination() {
        RealtimeEvent event = RealtimeEvent.of("REPAIR_PROGRESS_UPDATED", "PHIEU_SUA_CHUA", 5, "Tiến độ hoàn tất", null);

        publisher.sendToUser("technician1", "/queue/repairs", event);

        verify(messagingTemplate).convertAndSendToUser("technician1", "/queue/repairs", event);
    }

    @Test
    void sendToBranch_convertsAndSendsToBranchTopic() {
        RealtimeEvent event = RealtimeEvent.of("VEHICLE_CHECKED_IN", "TIEP_NHAN", 10, "Xe đã tiếp nhận tại xưởng", null);

        publisher.sendToBranch("CN001", event);

        verify(messagingTemplate).convertAndSend("/topic/branches/CN001", event);
    }

    @Test
    void sendToUser_nullUser_noop() {
        RealtimeEvent event = RealtimeEvent.of("TEST", "TEST", 1, "msg", null);

        publisher.sendToUser(null, event);

        verifyNoInteractions(messagingTemplate);
    }
}
