package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ServiceCategoryRequest {

    @NotBlank(message = "Tên loại dịch vụ không được để trống")
    @Size(max = 100, message = "Tên loại dịch vụ tối đa 100 ký tự")
    private String tenLoai;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String moTa;

    private Boolean trangThai = true;

    public ServiceCategoryRequest() {}

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
