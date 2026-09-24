package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ThietBiPush")
public class ThietBiPush {
    @Id
    @Column(name = "TokenHash", length = 64)
    private String tokenHash;
    @Column(name = "FcmToken", nullable = false, length = 2048)
    private String fcmToken;
    @Column(name = "MaNguoiDung", nullable = false)
    private Integer maNguoiDung;
    @Column(name = "CapNhatLuc", nullable = false)
    private LocalDateTime capNhatLuc;

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String value) { tokenHash = value; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String value) { fcmToken = value; }
    public Integer getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Integer value) { maNguoiDung = value; }
    public LocalDateTime getCapNhatLuc() { return capNhatLuc; }
    public void setCapNhatLuc(LocalDateTime value) { capNhatLuc = value; }
}
