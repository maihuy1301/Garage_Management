package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PhanCong")
public class PhanCong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhanCong")
    private Integer maPhanCong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuSuaChua", nullable = false)
    private PhieuSuaChua phieuSuaChua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNhanVien", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "VaiTroTrongCongViec", length = 100)
    private String vaiTroTrongCongViec;

    @Column(name = "ThoiGianPhanCong", insertable = false, updatable = false)
    private LocalDateTime thoiGianPhanCong;

    @Column(name = "TrangThai", length = 30)
    private String trangThai = "DA_GIAO";

    public PhanCong() {}

    public Integer getMaPhanCong() {
        return maPhanCong;
    }

    public void setMaPhanCong(Integer maPhanCong) {
        this.maPhanCong = maPhanCong;
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

    public String getVaiTroTrongCongViec() {
        return vaiTroTrongCongViec;
    }

    public void setVaiTroTrongCongViec(String vaiTroTrongCongViec) {
        this.vaiTroTrongCongViec = vaiTroTrongCongViec;
    }

    public LocalDateTime getThoiGianPhanCong() {
        return thoiGianPhanCong;
    }

    public void setThoiGianPhanCong(LocalDateTime thoiGianPhanCong) {
        this.thoiGianPhanCong = thoiGianPhanCong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
