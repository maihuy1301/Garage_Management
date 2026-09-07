package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ChiNhanh")
public class ChiNhanh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiNhanh")
    private Integer maChiNhanh;

    @Column(name = "TenChiNhanh", nullable = false, length = 150)
    private String tenChiNhanh;

    @Column(name = "DiaChi", nullable = false, length = 255)
    private String diaChi;

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    @Column(name = "NgayTao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    public ChiNhanh() {}

    public Integer getMaChiNhanh() {
        return maChiNhanh;
    }

    public void setMaChiNhanh(Integer maChiNhanh) {
        this.maChiNhanh = maChiNhanh;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
