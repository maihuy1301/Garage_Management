package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TrangThaiDocHoTro")
public class SupportReadCursor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTrangThaiDoc")
    private Long id;
    @Column(name = "MaHoiThoai", nullable = false)
    private Integer conversationId;
    @Column(name = "MaNguoiDung", nullable = false)
    private Integer userId;
    @Column(name = "MaTinNhanDaDocCuoi", nullable = false)
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
