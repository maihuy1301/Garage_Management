package com.garage.dto;

import java.math.BigDecimal;

public class InvoiceServiceItemResponse {

    private Integer maChiTiet;
    private Integer maPhieuDichVu;
    private Integer maDichVu;
    private String tenDichVu;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;

    public InvoiceServiceItemResponse() {}

    public InvoiceServiceItemResponse(Integer maChiTiet, Integer maDichVu, String tenDichVu,
                                      Integer soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        this(maChiTiet, null, maDichVu, tenDichVu, soLuong, donGia, thanhTien);
    }

    public InvoiceServiceItemResponse(Integer maChiTiet, Integer maPhieuDichVu, Integer maDichVu,
                                      String tenDichVu, Integer soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        this.maChiTiet = maChiTiet;
        this.maPhieuDichVu = maPhieuDichVu;
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.soLuong = soLuong != null ? soLuong : 1;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public Integer getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(Integer maChiTiet) { this.maChiTiet = maChiTiet; }

    public Integer getMaPhieuDichVu() { return maPhieuDichVu; }
    public void setMaPhieuDichVu(Integer maPhieuDichVu) { this.maPhieuDichVu = maPhieuDichVu; }

    public Integer getMaDichVu() { return maDichVu; }
    public void setMaDichVu(Integer maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }
}
