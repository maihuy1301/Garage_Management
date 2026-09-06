package com.garage.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateRepairItemRequest {

    @DecimalMin(value = "0.0", message = "Đơn giá không được nhỏ hơn 0")
    private BigDecimal donGia;

    @Size(max = 30, message = "Trạng thái tối đa 30 ký tự")
    private String trangThai;

    public UpdateRepairItemRequest() {}

    public UpdateRepairItemRequest(BigDecimal donGia, String trangThai) {
        this.donGia = donGia;
        this.trangThai = trangThai;
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
