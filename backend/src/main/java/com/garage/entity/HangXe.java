package com.garage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "HangXe")
public class HangXe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHangXe")
    private Integer maHangXe;

    @Column(name = "TenHangXe", nullable = false, length = 255)
    private String tenHangXe;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai = true;

    public HangXe() {}

    public HangXe(String tenHangXe) {
        this.tenHangXe = tenHangXe;
        this.trangThai = true;
    }

    public Integer getMaHangXe() {
        return maHangXe;
    }

    public void setMaHangXe(Integer maHangXe) {
        this.maHangXe = maHangXe;
    }

    public String getTenHangXe() {
        return tenHangXe;
    }

    public void setTenHangXe(String tenHangXe) {
        this.tenHangXe = tenHangXe;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
