package com.garage.dto;

import java.math.BigDecimal;

/**
 * Response DTO cho Phụ tùng trong danh mục phụ tùng.
 */
public class PartResponse {

    private Integer maPhuTung;
    private String maPhuTungCode;
    private String tenPhuTung;
    private String donViTinh;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private Boolean trangThai;

    public PartResponse() {}

    public PartResponse(Integer maPhuTung, String maPhuTungCode, String tenPhuTung,
                        String donViTinh, BigDecimal giaNhap, BigDecimal giaBan, Boolean trangThai) {
        this.maPhuTung = maPhuTung;
        this.maPhuTungCode = maPhuTungCode;
        this.tenPhuTung = tenPhuTung;
        this.donViTinh = donViTinh;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
        this.trangThai = trangThai;
    }

    public Integer getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(Integer maPhuTung) { this.maPhuTung = maPhuTung; }

    public String getMaPhuTungCode() { return maPhuTungCode; }
    public void setMaPhuTungCode(String maPhuTungCode) { this.maPhuTungCode = maPhuTungCode; }

    public String getTenPhuTung() { return tenPhuTung; }
    public void setTenPhuTung(String tenPhuTung) { this.tenPhuTung = tenPhuTung; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public BigDecimal getGiaNhap() { return giaNhap; }
    public void setGiaNhap(BigDecimal giaNhap) { this.giaNhap = giaNhap; }

    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }

    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}
