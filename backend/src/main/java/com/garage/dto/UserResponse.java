package com.garage.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UserResponse {

    private Integer maNguoiDung;
    private String tenDangNhap;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String anhDaiDien;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private List<String> roles;

    public UserResponse() {}

    public UserResponse(Integer maNguoiDung, String tenDangNhap, String hoTen, String email,
                        String soDienThoai, String anhDaiDien, Boolean trangThai,
                        LocalDateTime ngayTao, List<String> roles) {
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
