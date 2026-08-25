package com.garage.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO tạo lịch hẹn.
 * Đối với CUSTOMER: Khách hàng được tự động xác định từ JWT SecurityContext.
 * Đối với SYSTEM_ADMIN: Có thể truyền maKhachHang để tạo lịch hẹn cho khách hàng cụ thể.
 */
public class CreateAppointmentRequest {

    private Integer maKhachHang;

    @NotNull(message = "Mã xe không được để trống")
    private Integer maXe;

    @NotNull(message = "Mã chi nhánh không được để trống")
    private Integer maChiNhanh;

    @NotNull(message = "Thời gian hẹn không được để trống")
    @Future(message = "Thời gian hẹn phải ở tương lai")
    private LocalDateTime thoiGianHen;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String ghiChu;

    public CreateAppointmentRequest() {}

    public CreateAppointmentRequest(Integer maXe, Integer maChiNhanh, LocalDateTime thoiGianHen, String ghiChu) {
        this.maXe = maXe;
        this.maChiNhanh = maChiNhanh;
        this.thoiGianHen = thoiGianHen;
        this.ghiChu = ghiChu;
    }

    public Integer getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(Integer maKhachHang) { this.maKhachHang = maKhachHang; }

    public Integer getMaXe() { return maXe; }
    public void setMaXe(Integer maXe) { this.maXe = maXe; }

    public Integer getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Integer maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public LocalDateTime getThoiGianHen() { return thoiGianHen; }
    public void setThoiGianHen(LocalDateTime thoiGianHen) { this.thoiGianHen = thoiGianHen; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
