package com.garage.dto;

public class LoginResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Integer maNguoiDung;
    private String tenDangNhap;
    private String hoTen;
    private boolean hasPin;

    public LoginResponse() {}

    public LoginResponse(String accessToken, Integer maNguoiDung, String tenDangNhap, String hoTen,
                         boolean hasPin) {
        this.accessToken = accessToken;
        this.maNguoiDung = maNguoiDung;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.hasPin = hasPin;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
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

    public boolean isHasPin() {
        return hasPin;
    }

    public void setHasPin(boolean hasPin) {
        this.hasPin = hasPin;
    }
}
