package com.garage.dto;

import java.math.BigDecimal;

public class InvoicePartItemResponse {

    private Integer maChiTiet;
    private Integer maPhieuPhuTung;
    private Integer maDichVuChiTiet;
    private Integer maPhuTung;
    private String maPhuTungCode;
    private String tenPhuTung;
    private String donViTinh;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;

    public InvoicePartItemResponse() {}

    public InvoicePartItemResponse(Integer maChiTiet, Integer maPhuTung, String maPhuTungCode,
                                   String tenPhuTung, String donViTinh,
                                   Integer soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        this(maChiTiet, null, null, maPhuTung, maPhuTungCode, tenPhuTung, donViTinh, soLuong, donGia, thanhTien);
    }

    public InvoicePartItemResponse(Integer maChiTiet, Integer maPhieuPhuTung, Integer maDichVuChiTiet,
                                   Integer maPhuTung, String maPhuTungCode,
                                   String tenPhuTung, String donViTinh,
                                   Integer soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        this.maChiTiet = maChiTiet;
        this.maPhieuPhuTung = maPhieuPhuTung;
        this.maDichVuChiTiet = maDichVuChiTiet;
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

    public Integer getMaPhieuPhuTung() { return maPhieuPhuTung; }
    public void setMaPhieuPhuTung(Integer maPhieuPhuTung) { this.maPhieuPhuTung = maPhieuPhuTung; }

    public Integer getMaDichVuChiTiet() { return maDichVuChiTiet; }
    public void setMaDichVuChiTiet(Integer maDichVuChiTiet) { this.maDichVuChiTiet = maDichVuChiTiet; }

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
