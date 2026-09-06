package com.garage.dto;

import java.math.BigDecimal;

/**
 * Response DTO cho Tồn kho phụ tùng theo chi nhánh.
 */
public class InventoryResponse {

    private Integer maChiNhanh;
    private String tenChiNhanh;

    private Integer maPhuTung;
    private String maPhuTungCode;
    private String tenPhuTung;
    private String donViTinh;
    private BigDecimal giaBan;

    private Integer soLuongTon;
    private Integer soLuongToiThieu;

    public InventoryResponse() {}

    public InventoryResponse(Integer maChiNhanh, String tenChiNhanh,
                             Integer maPhuTung, String maPhuTungCode, String tenPhuTung,
                             String donViTinh, BigDecimal giaBan,
                             Integer soLuongTon, Integer soLuongToiThieu) {
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.maPhuTung = maPhuTung;
        this.maPhuTungCode = maPhuTungCode;
        this.tenPhuTung = tenPhuTung;
        this.donViTinh = donViTinh;
        this.giaBan = giaBan;
        this.soLuongTon = soLuongTon;
        this.soLuongToiThieu = soLuongToiThieu;
    }

    public Integer getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Integer maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public Integer getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(Integer maPhuTung) { this.maPhuTung = maPhuTung; }

    public String getMaPhuTungCode() { return maPhuTungCode; }
    public void setMaPhuTungCode(String maPhuTungCode) { this.maPhuTungCode = maPhuTungCode; }

    public String getTenPhuTung() { return tenPhuTung; }
    public void setTenPhuTung(String tenPhuTung) { this.tenPhuTung = tenPhuTung; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = domainGiaBan(giaBan); }
    private BigDecimal domainGiaBan(BigDecimal g) { return g; }

    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }

    public Integer getSoLuongToiThieu() { return soLuongToiThieu; }
    public void setSoLuongToiThieu(Integer soLuongToiThieu) { this.soLuongToiThieu = soLuongToiThieu; }
}
