package com.garage.dto;

public class CurrentUserResponse {

    private Integer maNguoiDung;
    private String tenDangNhap;
    private String hoTen;
    private String email;
    private boolean hasPin;
    private Integer maChiNhanh;
    private String tenChiNhanh;

    public CurrentUserResponse() {}

    public CurrentUserResponse(Integer maNguoiDung, String tenDangNhap, String hoTen,
                               String email, boolean hasPin) {
        this.maNguoiDung = maNguoiDung;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.email = email;
        this.hasPin = hasPin;
    }

    public CurrentUserResponse(Integer maNguoiDung, String tenDangNhap, String hoTen,
                               String email, boolean hasPin, Integer maChiNhanh,
                               String tenChiNhanh) {
        this(maNguoiDung, tenDangNhap, hoTen, email, hasPin);
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
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

    public boolean isHasPin() {
        return hasPin;
    }

    public void setHasPin(boolean hasPin) {
        this.hasPin = hasPin;
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
