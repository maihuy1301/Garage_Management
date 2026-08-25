package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Xe")
public class Xe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaXe")
    private Integer maXe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKhachHang", nullable = false)
    private KhachHang khachHang;

    @Column(name = "BienSo", nullable = false, unique = true, length = 20)
    private String bienSo;

    @Column(name = "HangXe", length = 50)
    private String hangXe;

    @Column(name = "Model", length = 100)
    private String model;

    @Column(name = "NamSanXuat")
    private Integer namSanXuat;

    @Column(name = "MauXe", length = 50)
    private String mauXe;

    @Column(name = "SoVIN", length = 50)
    private String soVIN;

    @Column(name = "SoKmHienTai")
    private Integer soKmHienTai = 0;

    @Column(name = "NgayTao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    public Xe() {}

    public Integer getMaXe() {
        return maXe;
    }

    public void setMaXe(Integer maXe) {
        this.maXe = maXe;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String bienSo) {
        this.bienSo = bienSo;
    }

    public String getHangXe() {
        return hangXe;
    }

    public void setHangXe(String hangXe) {
        this.hangXe = hangXe;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getNamSanXuat() {
        return namSanXuat;
    }

    public void setNamSanXuat(Integer namSanXuat) {
        this.namSanXuat = namSanXuat;
    }

    public String getMauXe() {
        return mauXe;
    }

    public void setMauXe(String mauXe) {
        this.mauXe = mauXe;
    }

    public String getSoVIN() {
        return soVIN;
    }

    public void setSoVIN(String soVIN) {
        this.soVIN = soVIN;
    }

    public Integer getSoKmHienTai() {
        return soKmHienTai;
    }

    public void setSoKmHienTai(Integer soKmHienTai) {
        this.soKmHienTai = soKmHienTai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
