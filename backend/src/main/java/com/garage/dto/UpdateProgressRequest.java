package com.garage.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request DTO cập nhật tiến độ phiếu sửa chữa (cập nhật trực tiếp PhieuSuaChua)
 */
public class UpdateProgressRequest {

    @Size(max = 30, message = "Trạng thái không được vượt quá 30 ký tự")
    private String trangThai;

    private LocalDateTime thoiGianBatDau;

    private LocalDateTime thoiGianHoanTat;

    private String ghiChu;

    public UpdateProgressRequest() {}

    public UpdateProgressRequest(String trangThai, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianHoanTat, String ghiChu) {
        this.trangThai = trangThai;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianHoanTat = thoiGianHoanTat;
        this.ghiChu = ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public LocalDateTime getThoiGianHoanTat() {
        return thoiGianHoanTat;
    }

    public void setThoiGianHoanTat(LocalDateTime thoiGianHoanTat) {
        this.thoiGianHoanTat = thoiGianHoanTat;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
