package com.garage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "HoaDon_PhuTung")
public class HoaDonPhuTung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiTiet")
    private Integer maChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHoaDon", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDichVuChiTiet")
    private HoaDonDichVu hoaDonDichVu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuPhuTung")
    private PhieuSuaChuaPhuTung phieuPhuTung;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "DonGia", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "ThanhTien", insertable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal thanhTien;

    public HoaDonPhuTung() {}

    public Integer getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(Integer maChiTiet) {
        this.maChiTiet = maChiTiet;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public HoaDonDichVu getHoaDonDichVu() {
        return hoaDonDichVu;
    }

    public void setHoaDonDichVu(HoaDonDichVu hoaDonDichVu) {
        this.hoaDonDichVu = hoaDonDichVu;
    }

    public PhieuSuaChuaPhuTung getPhieuPhuTung() {
        return phieuPhuTung;
    }

    public void setPhieuPhuTung(PhieuSuaChuaPhuTung phieuPhuTung) {
        this.phieuPhuTung = phieuPhuTung;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }
}
