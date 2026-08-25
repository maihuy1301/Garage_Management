package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GiaoDichKho")
public class GiaoDichKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGiaoDich")
    private Integer maGiaoDich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChiNhanh", nullable = false)
    private ChiNhanh chiNhanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhuTung", nullable = false)
    private PhuTung phuTung;

    @Column(name = "LoaiGiaoDich", nullable = false, length = 30)
    private String loaiGiaoDich;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuSuaChua")
    private PhieuSuaChua phieuSuaChua;

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    @Column(name = "ThoiGian", insertable = false, updatable = false)
    private LocalDateTime thoiGian;

    public GiaoDichKho() {}

    public Integer getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(Integer maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public ChiNhanh getChiNhanh() {
        return chiNhanh;
    }

    public void setChiNhanh(ChiNhanh chiNhanh) {
        this.chiNhanh = chiNhanh;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        this.phuTung = phuTung;
    }

    public String getLoaiGiaoDich() {
        return loaiGiaoDich;
    }

    public void setLoaiGiaoDich(String loaiGiaoDich) {
        this.loaiGiaoDich = loaiGiaoDich;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public PhieuSuaChua getPhieuSuaChua() {
        return phieuSuaChua;
    }

    public void setPhieuSuaChua(PhieuSuaChua phieuSuaChua) {
        this.phieuSuaChua = phieuSuaChua;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }
}
