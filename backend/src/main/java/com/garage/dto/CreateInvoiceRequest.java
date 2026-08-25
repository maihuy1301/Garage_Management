package com.garage.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public class CreateInvoiceRequest {

    @DecimalMin(value = "0.00", message = "Giảm giá không được âm")
    @Digits(integer = 16, fraction = 2, message = "Giảm giá không hợp lệ")
    private BigDecimal giamGia = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Thuế không được âm")
    @Digits(integer = 16, fraction = 2, message = "Thuế không hợp lệ")
    private BigDecimal thue = BigDecimal.ZERO;

    public CreateInvoiceRequest() {}

    public CreateInvoiceRequest(BigDecimal giamGia, BigDecimal thue) {
        this.giamGia = giamGia != null ? giamGia : BigDecimal.ZERO;
        this.thue = thue != null ? thue : BigDecimal.ZERO;
    }

    public BigDecimal getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(BigDecimal giamGia) {
        this.giamGia = giamGia != null ? giamGia : BigDecimal.ZERO;
    }

    public BigDecimal getThue() {
        return thue;
    }

    public void setThue(BigDecimal thue) {
        this.thue = thue != null ? thue : BigDecimal.ZERO;
    }
}
