package com.garage.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateRepairItemRequest {

    @NotNull(message = "Mã dịch vụ không được để trống")
    private Integer maDichVu;

    @DecimalMin(value = "0.0", message = "Đơn giá không được nhỏ hơn 0")
    private BigDecimal donGia;

    @Size(max = 30, message = "Trạng thái tối đa 30 ký tự")
    private String trangThai;

    public CreateRepairItemRequest() {}

    public CreateRepairItemRequest(Integer maDichVu) {
        this.maDichVu = maDichVu;
    }

    public CreateRepairItemRequest(Integer maDichVu, BigDecimal donGia, String trangThai) {
        this.maDichVu = maDichVu;
        this.donGia = donGia;
        this.trangThai = trangThai;
    }

    public Integer getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(Integer maDichVu) {
        this.maDichVu = maDichVu;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
