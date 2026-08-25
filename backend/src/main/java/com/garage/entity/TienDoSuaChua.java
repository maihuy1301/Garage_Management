package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TienDoSuaChua")
public class TienDoSuaChua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTienDo")
    private Integer maTienDo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuSuaChua", nullable = false)
    private PhieuSuaChua phieuSuaChua;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNhanVien", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @Column(name = "PhanTramHoanThanh")
    private Integer phanTramHoanThanh;

    @Column(name = "MoTa", length = 1000)
    private String moTa;

    @Column(name = "ThoiGian", insertable = false, updatable = false)
    private LocalDateTime thoiGian;

    public TienDoSuaChua() {}

    public Integer getMaTienDo() {
        return maTienDo;
    }

    public void setMaTienDo(Integer maTienDo) {
        this.maTienDo = maTienDo;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getPhanTramHoanThanh() {
        return phanTramHoanThanh;
    }

    public void setPhanTramHoanThanh(Integer phanTramHoanThanh) {
        this.phanTramHoanThanh = phanTramHoanThanh;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }
}
