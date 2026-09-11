package com.garage.dto;

public class BrandResponse {

    private Integer maHangXe;
    private String tenHangXe;
    private Boolean trangThai;

    public BrandResponse() {}

    public BrandResponse(Integer maHangXe, String tenHangXe) {
        this(maHangXe, tenHangXe, true);
    }

    public BrandResponse(Integer maHangXe, String tenHangXe, Boolean trangThai) {
        this.maHangXe = maHangXe;
        this.tenHangXe = tenHangXe;
        this.trangThai = trangThai;
    }

    public Integer getMaHangXe() {
        return maHangXe;
    }

    public void setMaHangXe(Integer maHangXe) {
        this.maHangXe = maHangXe;
    }

    public String getTenHangXe() {
        return tenHangXe;
    }

    public void setTenHangXe(String tenHangXe) {
        this.tenHangXe = tenHangXe;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
