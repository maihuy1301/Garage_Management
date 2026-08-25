package com.garage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UpdateUserRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String soDienThoai;

    @Size(max = 500, message = "Đường dẫn ảnh đại diện tối đa 500 ký tự")
    private String anhDaiDien;

    private List<String> roles;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String hoTen, String email, String soDienThoai, String anhDaiDien, List<String> roles) {
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.anhDaiDien = anhDaiDien;
        this.roles = roles;
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

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
