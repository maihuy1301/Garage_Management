package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateBrandRequest {

    @NotBlank(message = "Tên hãng xe không được để trống")
    @Size(max = 255, message = "Tên hãng xe không được vượt quá 255 ký tự")
    private String tenHangXe;

    public CreateBrandRequest() {}

    public CreateBrandRequest(String tenHangXe) {
        this.tenHangXe = tenHangXe;
    }

    public String getTenHangXe() {
        return tenHangXe;
    }

    public void setTenHangXe(String tenHangXe) {
        this.tenHangXe = tenHangXe;
    }
}
