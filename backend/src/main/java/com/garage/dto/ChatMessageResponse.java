package com.garage.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private Integer maTinNhan;
    private Integer maCuocHoiThoai;
    private Integer maNguoiGui;
    private String tenNguoiGui;
    private String hoTenNguoiGui;
    private String noiDung;
    private String duongDanTep;
    private Boolean daDoc;
    private LocalDateTime thoiGianGui;

    public ChatMessageResponse() {}

    public ChatMessageResponse(Integer maTinNhan, Integer maCuocHoiThoai, Integer maNguoiGui,
                               String tenNguoiGui, String hoTenNguoiGui, String noiDung,
                               String duongDanTep, Boolean daDoc, LocalDateTime thoiGianGui) {
        this.maTinNhan = maTinNhan;
        this.maCuocHoiThoai = maCuocHoiThoai;
        this.maNguoiGui = maNguoiGui;
        this.tenNguoiGui = tenNguoiGui;
        this.hoTenNguoiGui = hoTenNguoiGui;
        this.noiDung = noiDung;
        this.duongDanTep = duongDanTep;
        this.daDoc = daDoc;
        this.thoiGianGui = thoiGianGui;
    }

    public Integer getMaTinNhan() { return maTinNhan; }
    public void setMaTinNhan(Integer maTinNhan) { this.maTinNhan = maTinNhan; }

    public Integer getMaCuocHoiThoai() { return maCuocHoiThoai; }
    public void setMaCuocHoiThoai(Integer maCuocHoiThoai) { this.maCuocHoiThoai = maCuocHoiThoai; }

    public Integer getMaNguoiGui() { return maNguoiGui; }
    public void setMaNguoiGui(Integer maNguoiGui) { this.maNguoiGui = maNguoiGui; }

    public String getTenNguoiGui() { return tenNguoiGui; }
    public void setTenNguoiGui(String tenNguoiGui) { this.tenNguoiGui = tenNguoiGui; }

    public String getHoTenNguoiGui() { return hoTenNguoiGui; }
    public void setHoTenNguoiGui(String hoTenNguoiGui) { this.hoTenNguoiGui = hoTenNguoiGui; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getDuongDanTep() { return duongDanTep; }
    public void setDuongDanTep(String duongDanTep) { this.duongDanTep = duongDanTep; }

    public Boolean getDaDoc() { return daDoc; }
    public void setDaDoc(Boolean daDoc) { this.daDoc = daDoc; }

    public LocalDateTime getThoiGianGui() { return thoiGianGui; }
    public void setThoiGianGui(LocalDateTime thoiGianGui) { this.thoiGianGui = thoiGianGui; }
}
