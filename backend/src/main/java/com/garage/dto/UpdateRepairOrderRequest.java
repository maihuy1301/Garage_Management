package com.garage.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO cập nhật thông tin phiếu sửa chữa.
 */
public class UpdateRepairOrderRequest {

    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianHoanTat;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String ghiChu;

    public UpdateRepairOrderRequest() {}

    public UpdateRepairOrderRequest(LocalDateTime thoiGianBatDau, LocalDateTime thoiGianHoanTat, String ghiChu) {
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianHoanTat = thoiGianHoanTat;
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }

    public LocalDateTime getThoiGianHoanTat() { return thoiGianHoanTat; }
    public void setThoiGianHoanTat(LocalDateTime thoiGianHoanTat) { this.thoiGianHoanTat = thoiGianHoanTat; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
