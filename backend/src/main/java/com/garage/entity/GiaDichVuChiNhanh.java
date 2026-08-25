package com.garage.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "GiaDichVuChiNhanh", uniqueConstraints = {
    @UniqueConstraint(name = "UQ_GiaDichVu", columnNames = {"MaChiNhanh", "MaDichVu", "NgayApDung"})
})
public class GiaDichVuChiNhanh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGia")
    private Integer maGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChiNhanh", nullable = false)
    private ChiNhanh chiNhanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDichVu", nullable = false)
    private DichVu dichVu;

    @Column(name = "DonGia", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "NgayApDung", nullable = false)
    private LocalDate ngayApDung;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    public GiaDichVuChiNhanh() {}

    public Integer getMaGia() {
        return maGia;
    }

    public void setMaGia(Integer maGia) {
        this.maGia = maGia;
    }

    public ChiNhanh getChiNhanh() {
        return chiNhanh;
    }

    public void setChiNhanh(ChiNhanh chiNhanh) {
        this.chiNhanh = chiNhanh;
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

    public LocalDate getNgayApDung() {
        return ngayApDung;
    }

    public void setNgayApDung(LocalDate ngayApDung) {
        this.ngayApDung = ngayApDung;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
