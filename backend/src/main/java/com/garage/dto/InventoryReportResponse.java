package com.garage.dto;

import java.math.BigDecimal;

public class InventoryReportResponse {

    private Integer maPhuTung;
    private String maPhuTungCode;
    private String tenPhuTung;
    private BigDecimal donGia;
    private Integer soLuongTon;
    private Integer soLuongToiThieu;
    private BigDecimal giaTriTonKho;
    private boolean sapHetHang;

    public InventoryReportResponse() {}

    public InventoryReportResponse(Integer maPhuTung, String maPhuTungCode, String tenPhuTung,
                                   BigDecimal donGia, Integer soLuongTon, Integer soLuongToiThieu,
                                   BigDecimal giaTriTonKho, boolean sapHetHang) {
        this.maPhuTung = maPhuTung;
        this.maPhuTungCode = maPhuTungCode;
        this.tenPhuTung = tenPhuTung;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
        this.soLuongTon = soLuongTon != null ? soLuongTon : 0;
        this.soLuongToiThieu = soLuongToiThieu != null ? soLuongToiThieu : 0;
        this.giaTriTonKho = giaTriTonKho != null ? giaTriTonKho : BigDecimal.ZERO;
        this.sapHetHang = sapHetHang;
    }

    public Integer getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(Integer maPhuTung) { this.maPhuTung = maPhuTung; }

    public String getMaPhuTungCode() { return maPhuTungCode; }
    public void setMaPhuTungCode(String maPhuTungCode) { this.maPhuTungCode = maPhuTungCode; }

    public String getTenPhuTung() { return tenPhuTung; }
    public void setTenPhuTung(String tenPhuTung) { this.tenPhuTung = tenPhuTung; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }

    public Integer getSoLuongToiThieu() { return soLuongToiThieu; }
    public void setSoLuongToiThieu(Integer soLuongToiThieu) { this.soLuongToiThieu = soLuongToiThieu; }

    public BigDecimal getGiaTriTonKho() { return giaTriTonKho; }
    public void setGiaTriTonKho(BigDecimal giaTriTonKho) { this.giaTriTonKho = giaTriTonKho; }

    public boolean isSapHetHang() { return sapHetHang; }
    public void setSapHetHang(boolean sapHetHang) { this.sapHetHang = sapHetHang; }
}
