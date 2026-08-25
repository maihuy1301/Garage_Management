package com.garage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "BaoGiaPhatSinh_PhuTung")
public class BaoGiaPhatSinhPhuTung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiTiet")
    private Integer maChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaBaoGia", nullable = false)
    private BaoGiaPhatSinh baoGiaPhatSinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhuTung", nullable = false)
    private PhuTung phuTung;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "DonGia", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGia;

    public BaoGiaPhatSinhPhuTung() {}

    public Integer getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(Integer maChiTiet) {
        this.maChiTiet = maChiTiet;
    }

    public BaoGiaPhatSinh getBaoGiaPhatSinh() {
        return baoGiaPhatSinh;
    }

    public void setBaoGiaPhatSinh(BaoGiaPhatSinh baoGiaPhatSinh) {
        this.baoGiaPhatSinh = baoGiaPhatSinh;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        this.phuTung = phuTung;
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
}
