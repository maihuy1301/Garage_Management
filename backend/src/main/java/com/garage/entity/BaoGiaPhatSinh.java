package com.garage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BaoGiaPhatSinh")
public class BaoGiaPhatSinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaBaoGia")
    private Integer maBaoGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuSuaChua", nullable = false)
    private PhieuSuaChua phieuSuaChua;

    @Column(name = "LyDoPhatSinh", length = 1000)
    private String lyDoPhatSinh;

    @Column(name = "TongTien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "TrangThai", length = 30)
    private String trangThai = "CHO_KHACH_DUYET";

    @Column(name = "ThoiGianTao", insertable = false, updatable = false)
    private LocalDateTime thoiGianTao;

    @Column(name = "ThoiGianDuyet")
    private LocalDateTime thoiGianDuyet;

    public BaoGiaPhatSinh() {}

    public Integer getMaBaoGia() {
        return maBaoGia;
    }

    public void setMaBaoGia(Integer maBaoGia) {
        this.maBaoGia = maBaoGia;
    }

    public PhieuSuaChua getPhieuSuaChua() {
        return phieuSuaChua;
    }

    public void setPhieuSuaChua(PhieuSuaChua phieuSuaChua) {
        this.phieuSuaChua = phieuSuaChua;
    }

    public String getLyDoPhatSinh() {
        return lyDoPhatSinh;
    }

    public void setLyDoPhatSinh(String lyDoPhatSinh) {
        this.lyDoPhatSinh = lyDoPhatSinh;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public LocalDateTime getThoiGianDuyet() {
        return thoiGianDuyet;
    }

    public void setThoiGianDuyet(LocalDateTime thoiGianDuyet) {
        this.thoiGianDuyet = thoiGianDuyet;
    }
}
