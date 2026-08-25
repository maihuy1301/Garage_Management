package com.garage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PhuTung")
public class PhuTung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhuTung")
    private Integer maPhuTung;

    @Column(name = "MaPhuTungCode", nullable = false, unique = true, length = 30)
    private String maPhuTungCode;

    @Column(name = "TenPhuTung", nullable = false, length = 150)
    private String tenPhuTung;

    @Column(name = "DonViTinh", length = 30)
    private String donViTinh;

    @Column(name = "GiaNhap", precision = 18, scale = 2)
    private BigDecimal giaNhap;

    @Column(name = "GiaBan", precision = 18, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    public PhuTung() {}

    public Integer getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(Integer maPhuTung) {
        this.maPhuTung = maPhuTung;
    }

    public String getMaPhuTungCode() {
        return maPhuTungCode;
    }

    public void setMaPhuTungCode(String maPhuTungCode) {
        this.maPhuTungCode = maPhuTungCode;
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }

    public void setTenPhuTung(String tenPhuTung) {
        this.tenPhuTung = tenPhuTung;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(BigDecimal giaNhap) {
        this.giaNhap = giaNhap;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
