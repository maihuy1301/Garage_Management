package com.garage.dto;

import java.time.LocalDateTime;

public class StockTransactionResponse {

    private Integer maGiaoDich;
    private Integer maChiNhanh;
    private String tenChiNhanh;
    private Integer maPhuTung;
    private String maPhuTungCode;
    private String tenPhuTung;
    private String donViTinh;
    private String loaiGiaoDich;
    private Integer soLuong;
    private Integer maPhieuSuaChua;
    private String ghiChu;
    private LocalDateTime thoiGian;

    public StockTransactionResponse() {}

    public StockTransactionResponse(Integer maGiaoDich, Integer maChiNhanh, String tenChiNhanh,
                                    Integer maPhuTung, String maPhuTungCode, String tenPhuTung,
                                    String donViTinh, String loaiGiaoDich, Integer soLuong,
                                    Integer maPhieuSuaChua, String ghiChu, LocalDateTime thoiGian) {
        this.maGiaoDich = maGiaoDich;
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.maPhuTung = maPhuTung;
        this.maPhuTungCode = maPhuTungCode;
        this.tenPhuTung = tenPhuTung;
        this.donViTinh = donViTinh;
        this.loaiGiaoDich = loaiGiaoDich;
        this.soLuong = soLuong;
        this.maPhieuSuaChua = maPhieuSuaChua;
        this.ghiChu = ghiChu;
        this.thoiGian = thoiGian;
    }

    public Integer getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(Integer maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public Integer getMaChiNhanh() {
        return maChiNhanh;
    }

    public void setMaChiNhanh(Integer maChiNhanh) {
        this.maChiNhanh = maChiNhanh;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public Integer getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(Integer maPhuTung) {
        this.maPhuTung = maPhuTung;
    }

    public String getMaPhuTungCode() {
        return maPhuTungCode;
    }

    public void setMaPhuTungCode(String maPhuTungCode) {
        this.maPhuTungCode = maPhuTungCode;
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }

    public void setTenPhuTung(String tenPhuTung) {
        this.tenPhuTung = tenPhuTung;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getLoaiGiaoDich() {
        return loaiGiaoDich;
    }

    public void setLoaiGiaoDich(String loaiGiaoDich) {
        this.loaiGiaoDich = loaiGiaoDich;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Integer getMaPhieuSuaChua() {
        return maPhieuSuaChua;
    }

    public void setMaPhieuSuaChua(Integer maPhieuSuaChua) {
        this.maPhieuSuaChua = maPhieuSuaChua;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }
}
