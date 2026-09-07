package com.garage.dto;

import java.time.LocalDate;
import java.util.List;

public class EmployeeResponse {

    private Integer maNhanVien;
    private String chucVu;
    private LocalDate ngayVaoLam;
    private Boolean trangThai;

    // User details
    private Integer maNguoiDung;
    private String tenDangNhap;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private List<String> roles;

    // Branch details
    private Integer maChiNhanh;
    private String tenChiNhanh;

    public EmployeeResponse() {}

    public EmployeeResponse(Integer maNhanVien, String chucVu,
                            LocalDate ngayVaoLam, Boolean trangThai, Integer maNguoiDung,
                            String tenDangNhap, String hoTen, String email, String soDienThoai,
                            List<String> roles, Integer maChiNhanh,
                            String tenChiNhanh) {
        this.maNhanVien = maNhanVien;
        this.chucVu = chucVu;
        this.ngayVaoLam = ngayVaoLam;
        this.trangThai = trangThai;
        this.maNguoiDung = maNguoiDung;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.roles = roles;
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
    }

    public Integer getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(Integer maNhanVien) {
        this.maNhanVien = maNhanVien;
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

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

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
}
