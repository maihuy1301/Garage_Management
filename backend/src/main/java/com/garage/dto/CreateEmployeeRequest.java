package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateEmployeeRequest {

    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 20, message = "Mã nhân viên tối đa 20 ký tự")
    private String maNhanVienCode;

    @NotNull(message = "Mã người dùng không được để trống")
    private Integer maNguoiDung;

    @NotNull(message = "Mã chi nhánh không được để trống")
    private Integer maChiNhanh;

    @Size(max = 100, message = "Chức vụ tối đa 100 ký tự")
    private String chucVu;

    private LocalDate ngayVaoLam;

    public CreateEmployeeRequest() {}

    public CreateEmployeeRequest(String maNhanVienCode, Integer maNguoiDung, Integer maChiNhanh, String chucVu, LocalDate ngayVaoLam) {
        this.maNhanVienCode = maNhanVienCode;
        this.maNguoiDung = maNguoiDung;
        this.maChiNhanh = maChiNhanh;
        this.chucVu = chucVu;
        this.ngayVaoLam = ngayVaoLam;
    }

    public String getMaNhanVienCode() {
        return maNhanVienCode;
    }

    public void setMaNhanVienCode(String maNhanVienCode) {
        this.maNhanVienCode = maNhanVienCode;
    }

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public Integer getMaChiNhanh() {
        return maChiNhanh;
    }

    public void setMaChiNhanh(Integer maChiNhanh) {
        this.maChiNhanh = maChiNhanh;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public LocalDate getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(LocalDate ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }
}
