package com.garage.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CustomerResponse {

    private Integer maKhachHang;
    private String maKhachHangCode;
    private String diaChi;
    private LocalDate ngaySinh;

    // User fields
    private Integer maNguoiDung;
    private String tenDangNhap;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String anhDaiDien;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private List<String> roles;

    public CustomerResponse() {}

    public CustomerResponse(Integer maKhachHang, String maKhachHangCode, String diaChi,
                            LocalDate ngaySinh, Integer maNguoiDung, String tenDangNhap,
                            String hoTen, String email, String soDienThoai, String anhDaiDien,
                            Boolean trangThai, LocalDateTime ngayTao, List<String> roles) {
        this.maKhachHang = maKhachHang;
        this.maKhachHangCode = maKhachHangCode;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.maNguoiDung = maNguoiDung;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.anhDaiDien = anhDaiDien;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.roles = roles;
    }

    public Integer getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(Integer maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaKhachHangCode() {
        return maKhachHangCode;
    }

    public void setMaKhachHangCode(String maKhachHangCode) {
        this.maKhachHangCode = maKhachHangCode;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
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

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
