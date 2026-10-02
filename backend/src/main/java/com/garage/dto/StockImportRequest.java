package com.garage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StockImportRequest {

    @NotNull(message = "Mã phụ tùng không được để trống")
    private Integer maPhuTung;

    @NotNull(message = "Số lượng nhập không được để trống")
    @Min(value = 1, message = "Số lượng nhập tối thiểu là 1")
    private Integer soLuong;

    private Integer branchId;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String ghiChu;

    public StockImportRequest() {}

    public Integer getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(Integer maPhuTung) {
        this.maPhuTung = maPhuTung;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
