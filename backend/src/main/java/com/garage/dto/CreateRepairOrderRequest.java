package com.garage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO tạo phiếu sửa chữa từ phiếu tiếp nhận.
 * Chi nhánh, xe, khách hàng được tự động xác định từ PhieuTiepNhan.
 * Hỗ trợ maPhieuCha cho sub-repair orders / phiếu phát sinh.
 */
public class CreateRepairOrderRequest {

    @NotNull(message = "Mã phiếu tiếp nhận không được để trống")
    private Integer maTiepNhan;

    private Integer maPhieuCha;

    private LocalDateTime thoiGianBatDau;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String ghiChu;

    public CreateRepairOrderRequest() {}

    public CreateRepairOrderRequest(Integer maTiepNhan, String ghiChu) {
        this.maTiepNhan = maTiepNhan;
        this.ghiChu = ghiChu;
    }

    public CreateRepairOrderRequest(Integer maTiepNhan, Integer maPhieuCha, String ghiChu) {
        this.maTiepNhan = maTiepNhan;
        this.maPhieuCha = maPhieuCha;
        this.ghiChu = ghiChu;
    }

    public CreateRepairOrderRequest(Integer maTiepNhan, LocalDateTime thoiGianBatDau, String ghiChu) {
        this.maTiepNhan = maTiepNhan;
        this.thoiGianBatDau = thoiGianBatDau;
        this.ghiChu = ghiChu;
    }

    public Integer getMaTiepNhan() { return maTiepNhan; }
    public void setMaTiepNhan(Integer maTiepNhan) { this.maTiepNhan = maTiepNhan; }

    public Integer getMaPhieuCha() { return maPhieuCha; }
    public void setMaPhieuCha(Integer maPhieuCha) { this.maPhieuCha = maPhieuCha; }

    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
