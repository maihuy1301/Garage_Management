package com.garage.dto;

import java.math.BigDecimal;

public class QuotationPartItemResponse {

    private Integer maChiTiet;
    private Integer maPhuTung;
    private String maPhuTungCode;
    private String tenPhuTung;
    private String donViTinh;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;

    public QuotationPartItemResponse() {}

    public QuotationPartItemResponse(Integer maChiTiet, Integer maPhuTung, String maPhuTungCode,
                                     String tenPhuTung, String donViTinh,
                                     Integer soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        this.maChiTiet = maChiTiet;
        this.maPhuTung = maPhuTung;
        this.maPhuTungCode = maPhuTungCode;
        this.tenPhuTung = tenPhuTung;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public Integer getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(Integer maChiTiet) { this.maChiTiet = maChiTiet; }

    public Integer getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(Integer maPhuTung) { this.maPhuTung = maPhuTung; }

    public String getMaPhuTungCode() { return maPhuTungCode; }
    public void setMaPhuTungCode(String maPhuTungCode) { this.maPhuTungCode = maPhuTungCode; }

    public String getTenPhuTung() { return tenPhuTung; }
    public void setTenPhuTung(String tenPhuTung) { this.tenPhuTung = tenPhuTung; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }
}
