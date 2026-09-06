package com.garage.dto;

import java.math.BigDecimal;

public class QuotationServiceItemResponse {

    private Integer maChiTiet;
    private Integer maDichVu;
    private String tenDichVu;
    private BigDecimal donGia;
    private BigDecimal thanhTien;

    public QuotationServiceItemResponse() {}

    public QuotationServiceItemResponse(Integer maChiTiet, Integer maDichVu, String tenDichVu,
                                       BigDecimal donGia, BigDecimal thanhTien) {
        this.maChiTiet = maChiTiet;
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public Integer getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(Integer maChiTiet) {
        this.maChiTiet = maChiTiet;
    }

    public Integer getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(Integer maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
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
}
