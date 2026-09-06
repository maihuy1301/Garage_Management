package com.garage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "BaoGiaPhatSinh_DichVu")
public class BaoGiaPhatSinhDichVu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiTiet")
    private Integer maChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaBaoGia", nullable = false)
    private BaoGiaPhatSinh baoGiaPhatSinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDichVu", nullable = false)
    private DichVu dichVu;

    @Column(name = "DonGia", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGia;

    public BaoGiaPhatSinhDichVu() {}

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

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }
}
