package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PhieuTiepNhan")
public class PhieuTiepNhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTiepNhan")
    private Integer maTiepNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDatLich")
    private DatLich datLich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaXe", nullable = false)
    private Xe xe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChiNhanh", nullable = false)
    private ChiNhanh chiNhanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNhanVienTiepNhan", nullable = false)
    private NhanVien nhanVienTiepNhan;

    @Column(name = "ThoiGianTiepNhan", insertable = false, updatable = false)
    private LocalDateTime thoiGianTiepNhan;

    @Column(name = "SoKm")
    private Integer soKm;

    @Column(name = "TinhTrangNgoaiThat", length = 1000)
    private String tinhTrangNgoaiThat;

    @Column(name = "YeuCauKhachHang", length = 1000)
    private String yeuCauKhachHang;

    @Column(name = "TrangThai", length = 30)
    private String trangThai = "DA_TIEP_NHAN";

    public PhieuTiepNhan() {}

    public Integer getMaTiepNhan() {
        return maTiepNhan;
    }

    public void setMaTiepNhan(Integer maTiepNhan) {
        this.maTiepNhan = maTiepNhan;
    }

    public DatLich getDatLich() {
        return datLich;
    }

    public void setDatLich(DatLich datLich) {
        this.datLich = datLich;
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

    public NhanVien getNhanVienTiepNhan() {
        return nhanVienTiepNhan;
    }

    public void setNhanVienTiepNhan(NhanVien nhanVienTiepNhan) {
        this.nhanVienTiepNhan = nhanVienTiepNhan;
    }

    public LocalDateTime getThoiGianTiepNhan() {
        return thoiGianTiepNhan;
    }

    public void setThoiGianTiepNhan(LocalDateTime thoiGianTiepNhan) {
        this.thoiGianTiepNhan = thoiGianTiepNhan;
    }

    public Integer getSoKm() {
        return soKm;
    }

    public void setSoKm(Integer soKm) {
        this.soKm = soKm;
    }

    public String getTinhTrangNgoaiThat() {
        return tinhTrangNgoaiThat;
    }

    public void setTinhTrangNgoaiThat(String tinhTrangNgoaiThat) {
        this.tinhTrangNgoaiThat = tinhTrangNgoaiThat;
    }

    public String getYeuCauKhachHang() {
        return yeuCauKhachHang;
    }

    public void setYeuCauKhachHang(String yeuCauKhachHang) {
        this.yeuCauKhachHang = yeuCauKhachHang;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
