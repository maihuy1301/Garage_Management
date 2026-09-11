package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PhieuSuaChua")
public class PhieuSuaChua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhieuSuaChua")
    private Integer maPhieuSuaChua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTiepNhan", nullable = false)
    private PhieuTiepNhan phieuTiepNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChiNhanh", nullable = false)
    private ChiNhanh chiNhanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuCha")
    private PhieuSuaChua phieuCha;

    @Column(name = "ThoiGianBatDau")
    private LocalDateTime thoiGianBatDau;

    @Column(name = "ThoiGianHoanTat")
    private LocalDateTime thoiGianHoanTat;

    @Column(name = "TrangThai", length = 30)
    private String trangThai = "CHO_XU_LY";

    @Column(name = "GhiChu", length = 1000)
    private String ghiChu;

    public PhieuSuaChua() {}

    public Integer getMaPhieuSuaChua() {
        return maPhieuSuaChua;
    }

    public void setMaPhieuSuaChua(Integer maPhieuSuaChua) {
        this.maPhieuSuaChua = maPhieuSuaChua;
    }

    public PhieuTiepNhan getPhieuTiepNhan() {
        return phieuTiepNhan;
    }

    public void setPhieuTiepNhan(PhieuTiepNhan phieuTiepNhan) {
        this.phieuTiepNhan = phieuTiepNhan;
    }

    public ChiNhanh getChiNhanh() {
        return chiNhanh;
    }

    public void setChiNhanh(ChiNhanh chiNhanh) {
        this.chiNhanh = chiNhanh;
    }

    public PhieuSuaChua getPhieuCha() {
        return phieuCha;
    }

    public void setPhieuCha(PhieuSuaChua phieuCha) {
        this.phieuCha = phieuCha;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public LocalDateTime getThoiGianHoanTat() {
        return thoiGianHoanTat;
    }

    public void setThoiGianHoanTat(LocalDateTime thoiGianHoanTat) {
        this.thoiGianHoanTat = thoiGianHoanTat;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
