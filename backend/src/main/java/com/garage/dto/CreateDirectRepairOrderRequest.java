package com.garage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO tạo phiếu sửa chữa chủ động / trực tiếp (không bắt đầu từ lịch hẹn).
 * Hỗ trợ chọn khách hàng, xe thuộc khách hàng, phiếu cha (optional cho phát sinh sửa chữa),
 * và danh sách dịch vụ yêu cầu.
 */
public class CreateDirectRepairOrderRequest {

    @NotNull(message = "Mã khách hàng không được để trống")
    private Integer maKhachHang;

    @NotNull(message = "Mã xe không được để trống")
    private Integer maXe;

    /**
     * Mã phiếu sửa chữa cha (OPTIONAL) - dùng cho trường hợp phát sinh sửa chữa.
     */
    private Integer maPhieuCha;

    @NotEmpty(message = "Danh sách dịch vụ không được để trống")
    private List<Integer> serviceIds;

    private LocalDateTime thoiGianBatDau;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String ghiChu;

    private Integer soKm;

    @Size(max = 1000, message = "Yêu cầu khách hàng không được vượt quá 1000 ký tự")
    private String yeuCauKhachHang;

    public CreateDirectRepairOrderRequest() {}

    public CreateDirectRepairOrderRequest(Integer maKhachHang, Integer maXe, List<Integer> serviceIds, String ghiChu) {
        this.maKhachHang = maKhachHang;
        this.maXe = maXe;
        this.serviceIds = serviceIds;
        this.ghiChu = ghiChu;
    }

    public CreateDirectRepairOrderRequest(Integer maKhachHang, Integer maXe, Integer maPhieuCha, List<Integer> serviceIds, String ghiChu) {
        this.maKhachHang = maKhachHang;
        this.maXe = maXe;
        this.maPhieuCha = maPhieuCha;
        this.serviceIds = serviceIds;
        this.ghiChu = ghiChu;
    }

    public Integer getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(Integer maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public Integer getMaXe() {
        return maXe;
    }

    public void setMaXe(Integer maXe) {
        this.maXe = maXe;
    }

    public Integer getMaPhieuCha() {
        return maPhieuCha;
    }

    public void setMaPhieuCha(Integer maPhieuCha) {
        this.maPhieuCha = maPhieuCha;
    }

    public List<Integer> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<Integer> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Integer getSoKm() {
        return soKm;
    }

    public void setSoKm(Integer soKm) {
        this.soKm = soKm;
    }

    public String getYeuCauKhachHang() {
        return yeuCauKhachHang;
    }

    public void setYeuCauKhachHang(String yeuCauKhachHang) {
        this.yeuCauKhachHang = yeuCauKhachHang;
    }
}
