package com.garage.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public final class SupportChatDtos {
    private SupportChatDtos() {}
    public record Open(@NotNull Integer branchId) {}
    public record Send(@NotBlank @Size(max = 2000) String content,
                       @NotBlank @Pattern(regexp = "[a-zA-Z0-9-]{8,80}") String clientId) {}
    public record Read(@NotNull @PositiveOrZero Long lastReadId) {}
    public record Conversation(Integer id, Integer branchId, String branchName,
            Integer customerId, String customerName, Integer agentId, String agentName,
            String status, LocalDateTime updatedAt, Long lastMessageId, long unreadCount, boolean botPending) {}
    public record Message(Long id, Integer conversationId, Integer senderId, String senderName,
            String senderType, String content, LocalDateTime createdAt) {}
    public record History(List<Message> messages, boolean hasMore) {}
    public record BotRequested(Integer conversationId, Long messageId, String content) {}
    public record BookingDraft(Integer vehicleId, Integer branchId, LocalDateTime appointmentAt,
                               List<Integer> serviceIds, String note) {}
    public record BotAnswer(String text, boolean handoff, BookingDraft booking) {
        public BotAnswer(String text, boolean handoff) { this(text, handoff, null); }
    }
}
