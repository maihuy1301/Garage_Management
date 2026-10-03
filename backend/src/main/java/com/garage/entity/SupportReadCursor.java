package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SupportReadCursor")
public class SupportReadCursor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer conversationId;
    @Column(nullable = false)
    private Integer userId;
    @Column(nullable = false)
    private Long lastReadId;
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Integer getConversationId() { return conversationId; }
    public void setConversationId(Integer value) { this.conversationId = value; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer value) { this.userId = value; }
    public Long getLastReadId() { return lastReadId; }
    public void setLastReadId(Long value) { this.lastReadId = value; }
}
