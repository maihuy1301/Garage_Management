package com.garage.dto;

public class ServiceCategoryResponse {

    private Integer maLoaiDichVu;
    private String tenLoai;
    private String moTa;
    private Boolean trangThai;
    private Long soLuongDichVu;

    public ServiceCategoryResponse() {}

    public ServiceCategoryResponse(Integer maLoaiDichVu, String tenLoai, String moTa, Boolean trangThai, Long soLuongDichVu) {
        this.maLoaiDichVu = maLoaiDichVu;
        this.tenLoai = tenLoai;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.soLuongDichVu = soLuongDichVu;
    }

    public Integer getMaLoaiDichVu() {
        return maLoaiDichVu;
    }

    public void setMaLoaiDichVu(Integer maLoaiDichVu) {
        this.maLoaiDichVu = maLoaiDichVu;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public Long getSoLuongDichVu() {
        return soLuongDichVu;
    }

    public void setSoLuongDichVu(Long soLuongDichVu) {
        this.soLuongDichVu = soLuongDichVu;
    }
}
