package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "NhanVien")
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhanVien")
    private Integer maNhanVien;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung", nullable = false, unique = true)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaChiNhanh", nullable = false)
    private ChiNhanh chiNhanh;

    @Column(name = "MaNhanVienCode", nullable = false, unique = true, length = 20)
    private String maNhanVienCode;

    @Column(name = "ChucVu", length = 100)
    private String chucVu;

    @Column(name = "NgayVaoLam")
    private LocalDate ngayVaoLam;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    public NhanVien() {}

    public Integer getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(Integer maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public ChiNhanh getChiNhanh() {
        return chiNhanh;
    }

    public void setChiNhanh(ChiNhanh chiNhanh) {
        this.chiNhanh = chiNhanh;
    }

    public String getMaNhanVienCode() {
        return maNhanVienCode;
    }

    public void setMaNhanVienCode(String maNhanVienCode) {
        this.maNhanVienCode = maNhanVienCode;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public LocalDate getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(LocalDate ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
