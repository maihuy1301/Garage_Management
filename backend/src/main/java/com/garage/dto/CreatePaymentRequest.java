package com.garage.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreatePaymentRequest {

    @NotNull(message = "Số tiền thanh toán không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền thanh toán phải lớn hơn 0")
    private BigDecimal soTien;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @Size(max = 30, message = "Phương thức thanh toán tối đa 30 ký tự")
    private String phuongThuc; // TIEN_MAT, CHUYEN_KHOAN, THE, VNPAY, MOMO

    @Size(max = 100, message = "Mã giao dịch tối đa 100 ký tự")
    private String maGiaoDich;

    public CreatePaymentRequest() {}

    public CreatePaymentRequest(BigDecimal soTien, String phuongThuc, String maGiaoDich) {
        this.soTien = soTien;
        this.phuongThuc = phuongThuc;
        this.maGiaoDich = maGiaoDich;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public String getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(String maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }
}
