package com.garage.dto;

public class CreateConversationRequest {

    private Integer maKhachHang; // Only required if staff initiates conversation
    private Integer maTiepNhan;   // Optional reference to PhieuTiepNhan
    private Integer maNhanVien;   // Optional assigned employee
    private String initialMessage; // Optional first message

    public CreateConversationRequest() {}

    public CreateConversationRequest(Integer maKhachHang, Integer maTiepNhan, Integer maNhanVien, String initialMessage) {
        this.maKhachHang = maKhachHang;
        this.maTiepNhan = maTiepNhan;
        this.maNhanVien = maNhanVien;
        this.initialMessage = initialMessage;
    }

    public Integer getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(Integer maKhachHang) { this.maKhachHang = maKhachHang; }

    public Integer getMaTiepNhan() { return maTiepNhan; }
    public void setMaTiepNhan(Integer maTiepNhan) { this.maTiepNhan = maTiepNhan; }

    public Integer getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(Integer maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getInitialMessage() { return initialMessage; }
    public void setInitialMessage(String initialMessage) { this.initialMessage = initialMessage; }
}
