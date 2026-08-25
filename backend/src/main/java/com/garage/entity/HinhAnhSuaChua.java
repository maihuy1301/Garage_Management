package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "HinhAnhSuaChua")
public class HinhAnhSuaChua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHinhAnh")
    private Integer maHinhAnh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuSuaChua", nullable = false)
    private PhieuSuaChua phieuSuaChua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNhanVien", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "DuongDanAnh", nullable = false, length = 500)
    private String duongDanAnh;

    @Column(name = "LoaiAnh", length = 30)
    private String loaiAnh;

    @Column(name = "MoTa", length = 500)
    private String moTa;

    @Column(name = "ThoiGianChup", insertable = false, updatable = false)
    private LocalDateTime thoiGianChup;

    public HinhAnhSuaChua() {}

    public Integer getMaHinhAnh() {
        return maHinhAnh;
    }

    public void setMaHinhAnh(Integer maHinhAnh) {
        this.maHinhAnh = maHinhAnh;
    }

    public PhieuSuaChua getPhieuSuaChua() {
        return phieuSuaChua;
    }

    public void setPhieuSuaChua(PhieuSuaChua phieuSuaChua) {
        this.phieuSuaChua = phieuSuaChua;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getDuongDanAnh() {
        return duongDanAnh;
    }

    public void setDuongDanAnh(String duongDanAnh) {
        this.duongDanAnh = duongDanAnh;
    }

    public String getLoaiAnh() {
        return loaiAnh;
    }

    public void setLoaiAnh(String loaiAnh) {
        this.loaiAnh = loaiAnh;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public LocalDateTime getThoiGianChup() {
        return thoiGianChup;
    }

    public void setThoiGianChup(LocalDateTime thoiGianChup) {
        this.thoiGianChup = thoiGianChup;
    }
}
