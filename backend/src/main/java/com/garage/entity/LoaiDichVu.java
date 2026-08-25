package com.garage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "LoaiDichVu")
public class LoaiDichVu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLoaiDichVu")
    private Integer maLoaiDichVu;

    @Column(name = "TenLoai", nullable = false, length = 100)
    private String tenLoai;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    public LoaiDichVu() {}

    public Integer getMaLoaiDichVu() {
        return maLoaiDichVu;
    }

    public void setMaLoaiDichVu(Integer maLoaiDichVu) {
        this.maLoaiDichVu = maLoaiDichVu;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
