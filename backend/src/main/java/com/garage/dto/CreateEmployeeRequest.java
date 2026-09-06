package com.garage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CreateEmployeeRequest {

    @NotNull(message = "Mã người dùng không được để trống")
    private Integer maNguoiDung;

    @NotNull(message = "Mã chi nhánh không được để trống")
    private Integer maChiNhanh;

    @Size(max = 100, message = "Chức vụ tối đa 100 ký tự")
    private String chucVu;

    private LocalDate ngayVaoLam;

    public CreateEmployeeRequest() {}

    public CreateEmployeeRequest(Integer maNguoiDung, Integer maChiNhanh, String chucVu, LocalDate ngayVaoLam) {
        this.maNguoiDung = maNguoiDung;
        this.maChiNhanh = maChiNhanh;
        this.chucVu = chucVu;
        this.ngayVaoLam = ngayVaoLam;
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
