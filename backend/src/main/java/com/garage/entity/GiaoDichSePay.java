package com.garage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Minimal durable receipt. Deliberately excludes credentials and full bank payloads. */
@Entity
@Table(name = "GiaoDichSePay")
public class GiaoDichSePay implements org.springframework.data.domain.Persistable<String> {
    @Id @Column(name = "MaGiaoDich", length = 100) private String id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "MaPhien") private PhienThanhToan session;
    @Column(name = "SoTien", nullable = false, precision = 18, scale = 2) private BigDecimal amount;
    @Column(name = "MaNganHang", nullable = false, length = 50) private String bank;
    @Column(name = "SoTaiKhoan", nullable = false, length = 50) private String account;
    @Column(name = "NoiDung", nullable = false, length = 1000) private String content;
    @Column(name = "MaThamChieu", length = 100) private String reference;
    @Column(name = "KetQua", nullable = false, length = 40) private String outcome;
    @Column(name = "NhanLuc", nullable = false) private LocalDateTime receivedAt;
    public GiaoDichSePay() {}
    public GiaoDichSePay(String id, PhienThanhToan session, BigDecimal amount, String bank,
                         String account, String content, String reference, String outcome, LocalDateTime receivedAt) {
        this.id = id; this.session = session; this.amount = amount; this.bank = bank;
        this.account = account; this.content = content; this.reference = reference;
        this.outcome = outcome; this.receivedAt = receivedAt;
    }
    @Override
    public String getId() { return id; }
    /** Receipts are insert-only: an assigned provider ID must never trigger JPA merge/upsert. */
    @Override
    @Transient
    public boolean isNew() { return true; }
    public String getOutcome() { return outcome; }
}
