package com.garage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DichVu")
public class DichVu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDichVu")
    private Integer maDichVu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaLoaiDichVu", nullable = false)
    private LoaiDichVu loaiDichVu;

    @Column(name = "TenDichVu", nullable = false, length = 150)
    private String tenDichVu;

    @Column(name = "MoTa", length = 500)
    private String moTa;

    @Column(name = "ThoiGianDuKien")
    private Integer thoiGianDuKien;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    public DichVu() {}

    public Integer getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(Integer maDichVu) {
        this.maDichVu = maDichVu;
    }

    public LoaiDichVu getLoaiDichVu() {
        return loaiDichVu;
    }

    public void setLoaiDichVu(LoaiDichVu loaiDichVu) {
        this.loaiDichVu = loaiDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Integer getThoiGianDuKien() {
        return thoiGianDuKien;
    }

    public void setThoiGianDuKien(Integer thoiGianDuKien) {
        this.thoiGianDuKien = thoiGianDuKien;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
