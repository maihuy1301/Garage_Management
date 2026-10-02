package com.garage.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CreatePartRequest {

    @NotBlank(message = "Mã phụ tùng (code) không được để trống")
    @Size(max = 30, message = "Mã code tối đa 30 ký tự")
    private String maPhuTungCode;

    @NotBlank(message = "Tên phụ tùng không được để trống")
    @Size(max = 150, message = "Tên phụ tùng tối đa 150 ký tự")
    private String tenPhuTung;

    @Size(max = 30, message = "Đơn vị tính tối đa 30 ký tự")
    private String donViTinh;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá nhập không được âm")
    private BigDecimal giaNhap;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá bán không được âm")
    private BigDecimal giaBan;

    public CreatePartRequest() {}

    public String getMaPhuTungCode() {
        return maPhuTungCode;
    }

    public void setMaPhuTungCode(String maPhuTungCode) {
        this.maPhuTungCode = maPhuTungCode;
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }

    public void setTenPhuTung(String tenPhuTung) {
        this.tenPhuTung = tenPhuTung;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(BigDecimal giaNhap) {
        this.giaNhap = giaNhap;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }
}
