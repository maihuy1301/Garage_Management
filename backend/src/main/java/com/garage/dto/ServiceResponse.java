package com.garage.dto;

import java.math.BigDecimal;

/**
 * DTO trả về thông tin danh mục dịch vụ (DichVu).
 */
public class ServiceResponse {

    private Integer maDichVu;
    private Integer maLoaiDichVu;
    private String tenLoaiDichVu;
    private String tenDichVu;
    private String moTa;
    private BigDecimal donGia;
    private Integer thoiGianDuKien;
    private Boolean trangThai;

    public ServiceResponse() {}

    public ServiceResponse(Integer maDichVu, Integer maLoaiDichVu, String tenLoaiDichVu,
                           String tenDichVu, String moTa, BigDecimal donGia,
                           Integer thoiGianDuKien, Boolean trangThai) {
        this.maDichVu = maDichVu;
        this.maLoaiDichVu = maLoaiDichVu;
        this.tenLoaiDichVu = tenLoaiDichVu;
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.donGia = donGia;
        this.thoiGianDuKien = thoiGianDuKien;
        this.trangThai = trangThai;
    }

    public Integer getMaDichVu() { return maDichVu; }
    public void setMaDichVu(Integer maDichVu) { this.maDichVu = maDichVu; }

    public Integer getMaLoaiDichVu() { return maLoaiDichVu; }
    public void setMaLoaiDichVu(Integer maLoaiDichVu) { this.maLoaiDichVu = maLoaiDichVu; }

    public String getTenLoaiDichVu() { return tenLoaiDichVu; }
    public void setTenLoaiDichVu(String tenLoaiDichVu) { this.tenLoaiDichVu = tenLoaiDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public Integer getThoiGianDuKien() { return thoiGianDuKien; }
    public void setThoiGianDuKien(Integer thoiGianDuKien) { this.thoiGianDuKien = thoiGianDuKien; }

    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }
}
