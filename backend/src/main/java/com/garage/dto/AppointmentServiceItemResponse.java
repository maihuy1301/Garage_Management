package com.garage.dto;

import java.math.BigDecimal;

/**
 * DTO trả về thông tin dịch vụ được chọn trong Lịch hẹn (DatLich_DichVu).
 */
public class AppointmentServiceItemResponse {

    private Integer maDichVu;
    private String tenDichVu;
    private String moTa;
    private BigDecimal donGia;
    private Integer thoiGianDuKien;
    private Integer soLuong;
    private String ghiChu;

    public AppointmentServiceItemResponse() {}

    public AppointmentServiceItemResponse(Integer maDichVu, String tenDichVu, String moTa,
                                          BigDecimal donGia, Integer thoiGianDuKien,
                                          Integer soLuong, String ghiChu) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.donGia = donGia;
        this.thoiGianDuKien = thoiGianDuKien;
        this.soLuong = soLuong;
        this.ghiChu = ghiChu;
    }

    public Integer getMaDichVu() { return maDichVu; }
    public void setMaDichVu(Integer maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public Integer getThoiGianDuKien() { return thoiGianDuKien; }
    public void setThoiGianDuKien(Integer thoiGianDuKien) { this.thoiGianDuKien = thoiGianDuKien; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
