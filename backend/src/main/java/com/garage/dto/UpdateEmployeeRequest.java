package com.garage.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateEmployeeRequest {

    @Size(max = 100, message = "Chức vụ tối đa 100 ký tự")
    private String chucVu;

    private LocalDate ngayVaoLam;

    private Integer maChiNhanh;

    public UpdateEmployeeRequest() {}

    public UpdateEmployeeRequest(String chucVu, LocalDate ngayVaoLam, Integer maChiNhanh) {
        this.chucVu = chucVu;
        this.ngayVaoLam = ngayVaoLam;
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

    public Integer getMaChiNhanh() {
        return maChiNhanh;
    }

    public void setMaChiNhanh(Integer maChiNhanh) {
        this.maChiNhanh = maChiNhanh;
    }
}
