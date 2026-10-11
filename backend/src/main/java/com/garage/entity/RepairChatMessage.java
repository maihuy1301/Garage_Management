package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TinNhanSuaChua")
public class RepairChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTinNhan")
    private Long id;
    @Column(name = "MaTiepNhan", nullable = false)
    private Integer receptionId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "MaNguoiGui", nullable = false)
    private NguoiDung sender;
    @Column(name = "VaiTroNguoiGui", nullable = false, length = 20)
    private String senderRole;
    @Column(name = "MaChongTrung", nullable = false, length = 64)
    private String clientId;
    @Column(name = "NoiDung", nullable = false, length = 2000)
    private String content;
    @Column(name = "ThoiDiemGui", nullable = false)
    private LocalDateTime sentAt;

    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public Integer getReceptionId() { return receptionId; }
    public void setReceptionId(Integer value) { receptionId = value; }
    public NguoiDung getSender() { return sender; }
    public void setSender(NguoiDung value) { sender = value; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String value) { senderRole = value; }
    public String getClientId() { return clientId; }
    public void setClientId(String value) { clientId = value; }
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime value) { sentAt = value; }
}
