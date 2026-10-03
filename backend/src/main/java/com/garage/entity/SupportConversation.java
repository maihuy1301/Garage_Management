package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SupportConversation")
public class SupportConversation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customerId", nullable = false)
    private NguoiDung customer;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "branchId", nullable = false)
    private ChiNhanh branch;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "agentId")
    private NguoiDung agent;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Long lastMessageId;

    private Long pendingBotMessageId;
    public Integer getId() { return id; }
    public void setId(Integer value) { this.id = value; }
    public NguoiDung getCustomer() { return customer; }
    public void setCustomer(NguoiDung value) { this.customer = value; }
    public ChiNhanh getBranch() { return branch; }
    public void setBranch(ChiNhanh value) { this.branch = value; }
    public NguoiDung getAgent() { return agent; }
    public void setAgent(NguoiDung value) { this.agent = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
    public Long getLastMessageId() { return lastMessageId; }
    public void setLastMessageId(Long value) { this.lastMessageId = value; }
    public Long getPendingBotMessageId() { return pendingBotMessageId; }
    public void setPendingBotMessageId(Long value) { this.pendingBotMessageId = value; }
}
