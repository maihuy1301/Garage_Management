package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "NhacBaoDuong")
public class NhacBaoDuong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhac")
    private Integer maNhac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaXe", nullable = false)
    private Xe xe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDichVu")
    private DichVu dichVu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuSuaChua")
    private PhieuSuaChua phieuSuaChua;

    @Column(name = "NgayDuKien")
    private LocalDate ngayDuKien;

    @Column(name = "KmDuKien")
    private Integer kmDuKien;

    @Column(name = "TrangThai", length = 30)
    private String trangThai = "CHO_NHAC";

    @Column(name = "DaGuiThongBao")
    private Boolean daGuiThongBao = false;

    public NhacBaoDuong() {}

    public Integer getMaNhac() {
        return maNhac;
    }

    public void setMaNhac(Integer maNhac) {
        this.maNhac = maNhac;
    }

    public Xe getXe() {
        return xe;
    }

    public void setXe(Xe xe) {
        this.xe = xe;
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
    }

    public PhieuSuaChua getPhieuSuaChua() {
        return phieuSuaChua;
    }

    public void setPhieuSuaChua(PhieuSuaChua phieuSuaChua) {
        this.phieuSuaChua = phieuSuaChua;
    }

    public LocalDate getNgayDuKien() {
        return ngayDuKien;
    }

    public void setNgayDuKien(LocalDate ngayDuKien) {
        this.ngayDuKien = ngayDuKien;
    }

    public Integer getKmDuKien() {
        return kmDuKien;
    }

    public void setKmDuKien(Integer kmDuKien) {
        this.kmDuKien = kmDuKien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Boolean getDaGuiThongBao() {
        return daGuiThongBao;
    }

    public void setDaGuiThongBao(Boolean daGuiThongBao) {
        this.daGuiThongBao = daGuiThongBao;
    }
}
