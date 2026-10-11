package com.garage.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public final class RepairChatDtos {
    private RepairChatDtos() {}
    public record Send(@NotBlank @Size(max = 2000) String content,
                       @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{8,64}") String clientId) {}
    public record Room(Integer receptionId, Integer repairOrderId, Integer vehicleId,
                       String plate, String vehicleName, String branchName,
                       String state, boolean canSend, boolean canDelete) {}
    public record Message(Long id, Integer senderId, String senderName, String senderRole,
                          String content, LocalDateTime sentAt, boolean mine) {}
    public record History(Room room, List<Message> messages, boolean hasMore) {}
}
