package com.garage.dto;

import java.math.BigDecimal;

/**
 * Response DTO cho Phụ tùng định mức kèm theo Dịch vụ.
 */
public class ServicePartResponse {

    private Integer maPhuTung;
    private String tenPhuTung;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String donViTinh;

    public ServicePartResponse() {}

    public ServicePartResponse(Integer maPhuTung, String tenPhuTung, Integer soLuong,
                               BigDecimal donGia, BigDecimal thanhTien, String donViTinh) {
        this.maPhuTung = maPhuTung;
        this.tenPhuTung = tenPhuTung;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.donViTinh = donViTinh;
    }

    public Integer getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(Integer maPhuTung) {
        this.maPhuTung = maPhuTung;
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }

    public void setTenPhuTung(String tenPhuTung) {
        this.tenPhuTung = tenPhuTung;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }
}
