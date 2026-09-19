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
    @JoinColumn(name = "MaNguoiPhanCong", nullable = false)
    private NhanVien nguoiPhanCong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDuyet")
    private NhanVien nguoiDuyet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNhanVienDuocPhanCong", nullable = false)
    private NhanVien nhanVienDuocPhanCong;

    @Column(name = "ThoiGianTao")
    private LocalDateTime thoiGianTao;

    @Column(name = "ThoiGianDuyet")
    private LocalDateTime thoiGianDuyet;

    @Column(name = "TrangThai", length = 30, nullable = false)
    private String trangThai = "CHO_DUYET";

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    public PhanCong() {}

    @PrePersist
    public void prePersist() {
        if (thoiGianTao == null) {
            thoiGianTao = LocalDateTime.now();
        }
    }

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

    public NhanVien getNguoiPhanCong() {
        return nguoiPhanCong;
    }

    public void setNguoiPhanCong(NhanVien nguoiPhanCong) {
        this.nguoiPhanCong = nguoiPhanCong;
    }

    public NhanVien getNguoiDuyet() {
        return nguoiDuyet;
    }

    public void setNguoiDuyet(NhanVien nguoiDuyet) {
        this.nguoiDuyet = nguoiDuyet;
    }

    public NhanVien getNhanVienDuocPhanCong() {
        return nhanVienDuocPhanCong;
    }

    public void setNhanVienDuocPhanCong(NhanVien nhanVienDuocPhanCong) {
        this.nhanVienDuocPhanCong = nhanVienDuocPhanCong;
    }

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public LocalDateTime getThoiGianDuyet() {
        return thoiGianDuyet;
    }

    public void setThoiGianDuyet(LocalDateTime thoiGianDuyet) {
        this.thoiGianDuyet = thoiGianDuyet;
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

    // --- Helper compatibility methods ---
    public NhanVien getNhanVien() {
        return nhanVienDuocPhanCong;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVienDuocPhanCong = nhanVien;
    }

    public LocalDateTime getThoiGianPhanCong() {
        return thoiGianTao;
    }

    public void setThoiGianPhanCong(LocalDateTime thoiGianPhanCong) {
        this.thoiGianTao = thoiGianPhanCong;
    }

    public String getVaiTroTrongCongViec() {
        return ghiChu;
    }

    public void setVaiTroTrongCongViec(String vaiTroTrongCongViec) {
        this.ghiChu = vaiTroTrongCongViec;
    }
}
