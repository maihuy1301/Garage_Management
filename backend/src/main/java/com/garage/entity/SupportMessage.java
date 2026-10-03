package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SupportMessage")
public class SupportMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "conversationId", nullable = false)
    private SupportConversation conversation;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "senderId")
    private NguoiDung sender;
    @Column(nullable = false, length = 12)
    private String senderType;
    @Column(nullable = false, length = 2000)
    private String content;
    @Column(nullable = false, length = 100)
    private String clientId;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    private Integer appointmentId;
    @Column(length = 64)
    private String requestHash;
    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String value) { requestHash = value; }
    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer value) { appointmentId = value; }
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public SupportConversation getConversation() { return conversation; }
    public void setConversation(SupportConversation value) { this.conversation = value; }
    public NguoiDung getSender() { return sender; }
    public void setSender(NguoiDung value) { this.sender = value; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String value) { this.senderType = value; }
    public String getContent() { return content; }
    public void setContent(String value) { this.content = value; }
    public String getClientId() { return clientId; }
    public void setClientId(String value) { this.clientId = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
