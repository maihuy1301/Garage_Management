package com.garage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class QuotationServiceItemRequest {

    @NotNull(message = "Mã dịch vụ không được để trống")
    private Integer maDichVu;

    @NotNull(message = "Số lượng dịch vụ không được để trống")
    @Min(value = 1, message = "Số lượng dịch vụ phải lớn hơn hoặc bằng 1")
    private Integer soLuong;

    public QuotationServiceItemRequest() {}

    public QuotationServiceItemRequest(Integer maDichVu, Integer soLuong) {
        this.maDichVu = maDichVu;
        this.soLuong = soLuong;
    }

    public Integer getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(Integer maDichVu) {
        this.maDichVu = maDichVu;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
}
