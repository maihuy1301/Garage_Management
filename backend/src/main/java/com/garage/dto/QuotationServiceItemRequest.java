package com.garage.dto;

import jakarta.validation.constraints.NotNull;

public class QuotationServiceItemRequest {

    @NotNull(message = "Mã dịch vụ không được để trống")
    private Integer maDichVu;

    public QuotationServiceItemRequest() {}

    public QuotationServiceItemRequest(Integer maDichVu) {
        this.maDichVu = maDichVu;
    }

    public Integer getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(Integer maDichVu) {
        this.maDichVu = maDichVu;
    }
}
