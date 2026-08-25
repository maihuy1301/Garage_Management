package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateCustomerRequest {

    @NotBlank(message = "Mã khách hàng không được để trống")
    @Size(max = 20, message = "Mã khách hàng tối đa 20 ký tự")
    private String maKhachHangCode;

    @NotNull(message = "Mã người dùng không được để trống")
    private Integer maNguoiDung;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String diaChi;

    private LocalDate ngaySinh;

    public CreateCustomerRequest() {}

    public CreateCustomerRequest(String maKhachHangCode, Integer maNguoiDung, String diaChi, LocalDate ngaySinh) {
        this.maKhachHangCode = maKhachHangCode;
        this.maNguoiDung = maNguoiDung;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
    }

    public String getMaKhachHangCode() {
        return maKhachHangCode;
    }

    public void setMaKhachHangCode(String maKhachHangCode) {
        this.maKhachHangCode = maKhachHangCode;
    }

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }
}
