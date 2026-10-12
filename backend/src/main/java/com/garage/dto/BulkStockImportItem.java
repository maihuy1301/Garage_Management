package com.garage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BulkStockImportItem {

    private Integer maPhuTung;

    private String maPhuTungCode;

    @NotNull(message = "Số lượng nhập không được để trống")
    @Min(value = 1, message = "Số lượng nhập tối thiểu là 1")
    private Integer soLuong;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String ghiChu;

    public BulkStockImportItem() {}

    public BulkStockImportItem(Integer maPhuTung, String maPhuTungCode, Integer soLuong, String ghiChu) {
        this.maPhuTung = maPhuTung;
        this.maPhuTungCode = maPhuTungCode;
        this.soLuong = soLuong;
        this.ghiChu = ghiChu;
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

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
