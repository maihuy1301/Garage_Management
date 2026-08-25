package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DatLich")
public class DatLich {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDatLich")
    private Integer maDatLich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKhachHang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaXe", nullable = false)
    private Xe xe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChiNhanh", nullable = false)
    private ChiNhanh chiNhanh;

    @Column(name = "ThoiGianHen", nullable = false)
    private LocalDateTime thoiGianHen;

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai = "CHO_XAC_NHAN";

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    @Column(name = "NgayDat", insertable = false, updatable = false)
    private LocalDateTime ngayDat;

    public DatLich() {}

    public Integer getMaDatLich() {
        return maDatLich;
    }

    public void setMaDatLich(Integer maDatLich) {
        this.maDatLich = maDatLich;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public Xe getXe() {
        return xe;
    }

    public void setXe(Xe xe) {
        this.xe = xe;
    }

    public ChiNhanh getChiNhanh() {
        return chiNhanh;
    }

    public void setChiNhanh(ChiNhanh chiNhanh) {
        this.chiNhanh = chiNhanh;
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(LocalDateTime thoiGianHen) {
        this.thoiGianHen = thoiGianHen;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }
}
