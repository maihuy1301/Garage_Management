package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DanhGia")
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDanhGia")
    private Integer maDanhGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKhachHang", nullable = false)
    private KhachHang khachHang;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuSuaChua", nullable = false, unique = true)
    private PhieuSuaChua phieuSuaChua;

    @Column(name = "SoSao", nullable = false)
    private Integer soSao;

    @Column(name = "NoiDung", length = 1000)
    private String noiDung;

    @Column(name = "NgayDanhGia", insertable = false, updatable = false)
    private LocalDateTime ngayDanhGia;

    public DanhGia() {}

    public Integer getMaDanhGia() {
        return maDanhGia;
    }

    public void setMaDanhGia(Integer maDanhGia) {
        this.maDanhGia = maDanhGia;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public PhieuSuaChua getPhieuSuaChua() {
        return phieuSuaChua;
    }

    public void setPhieuSuaChua(PhieuSuaChua phieuSuaChua) {
        this.phieuSuaChua = phieuSuaChua;
    }

    public Integer getSoSao() {
        return soSao;
    }

    public void setSoSao(Integer soSao) {
        this.soSao = soSao;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getNgayDanhGia() {
        return ngayDanhGia;
    }

    public void setNgayDanhGia(LocalDateTime ngayDanhGia) {
        this.ngayDanhGia = ngayDanhGia;
    }
}
