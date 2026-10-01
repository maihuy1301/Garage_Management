package com.garage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PhienThanhToan")
public class PhienThanhToan {
    @Id @Column(name = "MaPhien", length = 36) private String id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "MaHoaDon", nullable = false) private HoaDon invoice;
    @Column(name = "SoTien", nullable = false, precision = 18, scale = 2) private BigDecimal amount;
    @Column(name = "NoiDung", nullable = false, unique = true, length = 40) private String content;
    @Column(name = "MoiTruong", nullable = false, length = 10) private String environment;
    @Column(name = "MaNganHang", nullable = false, length = 50) private String bankCode;
    @Column(name = "TenNganHang", nullable = false, length = 100) private String bankName;
    @Column(name = "SoTaiKhoan", nullable = false, length = 50) private String accountNumber;
    @Column(name = "TenChuTaiKhoan", nullable = false, length = 150) private String accountName;
    @Column(name = "TrangThai", nullable = false, length = 30) private String status;
    @Column(name = "TaoLuc", nullable = false) private LocalDateTime createdAt;
    @Column(name = "HetHanLuc", nullable = false) private LocalDateTime expiresAt;
    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public HoaDon getInvoice() { return invoice; }
    public void setInvoice(HoaDon v) { invoice = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { amount = v; }
    public String getContent() { return content; }
    public void setContent(String v) { content = v; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String v) { environment = v; }
    public String getBankCode() { return bankCode; }
    public void setBankCode(String v) { bankCode = v; }
    public String getBankName() { return bankName; }
    public void setBankName(String v) { bankName = v; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String v) { accountNumber = v; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String v) { accountName = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime v) { expiresAt = v; }
}
