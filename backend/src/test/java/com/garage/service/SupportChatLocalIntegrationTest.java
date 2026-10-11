package com.garage.service;

import com.garage.entity.SupportMessage;
import com.garage.entity.SupportReadCursor;
import com.garage.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

/** Opt-in verification against the migrated local SQL Server; writes roll back. */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SUPPORT_CHAT_LOCAL_INTEGRATION", matches = "true")
@Transactional
class SupportChatLocalIntegrationTest {
    @Autowired SupportConversationRepository rooms;
    @Autowired SupportMessageRepository messages;
    @Autowired SupportReadCursorRepository cursors;
    @Autowired EntityManager em;

    @Test void renamedTablesSupportUnicodeHistoryAndReadCursor() {
        var existing = rooms.findAllByOrderByUpdatedAtDesc();
        assertThat(existing).as("Local fixture needs one support conversation").isNotEmpty();
        var room = rooms.lockById(existing.get(0).getId()).orElseThrow();
        var customerId = room.getCustomer().getMaNguoiDung();
        var branchId = room.getBranch().getMaChiNhanh();
        var agentId = room.getAgent() == null ? null : room.getAgent().getMaNguoiDung();
        var requestId = UUID.randomUUID().toString();
        var message = new SupportMessage();
        message.setConversation(room);
        message.setSenderType("BOT");
        message.setContent("Hỗ trợ bảo dưỡng — kiểm tra ánh xạ tiếng Việt");
        message.setClientId(requestId);
        message.setRequestHash("a".repeat(64));
        message.setCreatedAt(LocalDateTime.now());
        messages.saveAndFlush(message);
        var messageId = message.getId();
        room.setLastMessageId(messageId);
        room.setPendingBotMessageId(messageId);
        room.setUpdatedAt(LocalDateTime.now());
        rooms.saveAndFlush(room);
        var cursor = cursors.findByConversationIdAndUserId(room.getId(), customerId)
                .orElseGet(() -> {
                    var value = new SupportReadCursor();
                    value.setConversationId(room.getId());
                    value.setUserId(customerId);
                    return value;
                });
        cursor.setLastReadId(messageId);
        cursors.saveAndFlush(cursor);
        var roomId = room.getId();
        em.clear();
        var loaded = rooms.findByCustomerMaNguoiDungAndBranchMaChiNhanh(customerId, branchId).orElseThrow();
        assertThat(loaded.getLastMessageId()).isEqualTo(messageId);
        assertThat(loaded.getPendingBotMessageId()).isEqualTo(messageId);
        assertThat(loaded.getAgent() == null ? null : loaded.getAgent().getMaNguoiDung()).isEqualTo(agentId);
        var stored = messages.findByConversationIdAndClientId(roomId, requestId).orElseThrow();
        assertThat(stored.getContent()).isEqualTo("Hỗ trợ bảo dưỡng — kiểm tra ánh xạ tiếng Việt");
        assertThat(stored.getRequestHash()).isEqualTo("a".repeat(64));
        assertThat(messages.history(roomId, Long.MAX_VALUE, PageRequest.of(0, 40)))
                .extracting(SupportMessage::getId).contains(messageId);
        assertThat(cursors.findByConversationIdAndUserId(roomId, customerId).orElseThrow().getLastReadId())
                .isEqualTo(messageId);
        assertThat(messages.unread(roomId, messageId, customerId)).isZero();
    }
}
