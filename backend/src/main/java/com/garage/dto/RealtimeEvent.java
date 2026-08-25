package com.garage.dto;

import java.time.LocalDateTime;

public class RealtimeEvent {

    private String eventType;
    private String entityType;
    private Object entityId;
    private String message;
    private Object payload;
    private LocalDateTime timestamp;

    public RealtimeEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public RealtimeEvent(String eventType, String entityType, Object entityId, String message, Object payload) {
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.message = message;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
    }

    public static RealtimeEvent of(String eventType, String entityType, Object entityId, String message, Object payload) {
        return new RealtimeEvent(eventType, entityType, entityId, message, payload);
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Object getEntityId() { return entityId; }
    public void setEntityId(Object entityId) { this.entityId = entityId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
