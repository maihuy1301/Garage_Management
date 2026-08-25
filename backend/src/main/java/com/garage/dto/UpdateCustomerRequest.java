package com.garage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateCustomerRequest {

    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String soDienThoai;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String diaChi;

    private LocalDate ngaySinh;

    @Size(max = 500, message = "Đường dẫn ảnh đại diện tối đa 500 ký tự")
    private String anhDaiDien;

    public UpdateCustomerRequest() {}

    public UpdateCustomerRequest(String hoTen, String email, String soDienThoai, String diaChi, LocalDate ngaySinh, String anhDaiDien) {
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.anhDaiDien = anhDaiDien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
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

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }
}
