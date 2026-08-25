package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CuocHoiThoai")
public class CuocHoiThoai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCuocHoiThoai")
    private Integer maCuocHoiThoai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKhachHang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTiepNhan")
    private PhieuTiepNhan phieuTiepNhan;

    @Column(name = "TrangThai", length = 30)
    private String trangThai = "DANG_MO";

    @Column(name = "NgayTao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhatCuoi", insertable = false, updatable = false)
    private LocalDateTime ngayCapNhatCuoi;

    public CuocHoiThoai() {}

    public Integer getMaCuocHoiThoai() {
        return maCuocHoiThoai;
    }

    public void setMaCuocHoiThoai(Integer maCuocHoiThoai) {
        this.maCuocHoiThoai = maCuocHoiThoai;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public PhieuTiepNhan getPhieuTiepNhan() {
        return phieuTiepNhan;
    }

    public void setPhieuTiepNhan(PhieuTiepNhan phieuTiepNhan) {
        this.phieuTiepNhan = phieuTiepNhan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayCapNhatCuoi() {
        return ngayCapNhatCuoi;
    }

    public void setNgayCapNhatCuoi(LocalDateTime ngayCapNhatCuoi) {
        this.ngayCapNhatCuoi = ngayCapNhatCuoi;
    }
}
