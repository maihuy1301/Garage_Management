package com.garage.dto;

import java.time.LocalDateTime;

public class VehicleResponse {

    private Integer maXe;
    private Integer maKhachHang;
    private String tenChuXe;
    private String bienSo;
    private String hangXe;
    private String model;
    private Integer namSanXuat;
    private String mauXe;
    private String soVIN;
    private Integer soKmHienTai;
    private Boolean trangThai;
    private LocalDateTime ngayTao;

    public VehicleResponse() {}

    public VehicleResponse(Integer maXe, Integer maKhachHang, String tenChuXe,
                           String bienSo, String hangXe, String model,
                           Integer namSanXuat, String mauXe, String soVIN,
                           Integer soKmHienTai, Boolean trangThai, LocalDateTime ngayTao) {
        this.maXe = maXe;
        this.maKhachHang = maKhachHang;
        this.tenChuXe = tenChuXe;
        this.bienSo = bienSo;
        this.hangXe = hangXe;
        this.model = model;
        this.namSanXuat = namSanXuat;
        this.mauXe = mauXe;
        this.soVIN = soVIN;
        this.soKmHienTai = soKmHienTai;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
    }

    public Integer getMaXe() {
        return maXe;
    }

    public void setMaXe(Integer maXe) {
        this.maXe = maXe;
    }

    public Integer getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(Integer maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenChuXe() {
        return tenChuXe;
    }

    public void setTenChuXe(String tenChuXe) {
        this.tenChuXe = tenChuXe;
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String bienSo) {
        this.bienSo = bienSo;
    }

    public String getHangXe() {
        return hangXe;
    }

    public void setHangXe(String hangXe) {
        this.hangXe = hangXe;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getNamSanXuat() {
        return namSanXuat;
    }

    public void setNamSanXuat(Integer namSanXuat) {
        this.namSanXuat = namSanXuat;
    }

    public String getMauXe() {
        return mauXe;
    }

    public void setMauXe(String mauXe) {
        this.mauXe = mauXe;
    }

    public String getSoVIN() {
        return soVIN;
    }

    public void setSoVIN(String soVIN) {
        this.soVIN = soVIN;
    }

    public Integer getSoKmHienTai() {
        return soKmHienTai;
    }

    public void setSoKmHienTai(Integer soKmHienTai) {
        this.soKmHienTai = soKmHienTai;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
