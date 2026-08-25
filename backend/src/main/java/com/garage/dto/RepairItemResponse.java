package com.garage.dto;

import java.math.BigDecimal;

/**
 * Response DTO cho Hạng mục dịch vụ trong Phiếu sửa chữa.
 */
public class RepairItemResponse {

    private Integer maChiTiet;
    private Integer maPhieuSuaChua;
    private Integer maDichVu;
    private String tenDichVu;
    private Integer maLoaiDichVu;
    private String tenLoaiDichVu;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String trangThai;

    public RepairItemResponse() {}

    public RepairItemResponse(Integer maChiTiet, Integer maPhieuSuaChua,
                              Integer maDichVu, String tenDichVu,
                              Integer maLoaiDichVu, String tenLoaiDichVu,
                              Integer soLuong, BigDecimal donGia,
                              BigDecimal thanhTien, String trangThai) {
        this.maChiTiet = maChiTiet;
        this.maPhieuSuaChua = maPhieuSuaChua;
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.maLoaiDichVu = maLoaiDichVu;
        this.tenLoaiDichVu = tenLoaiDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.trangThai = trangThai;
    }

    public Integer getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(Integer maChiTiet) { this.maChiTiet = maChiTiet; }

    public Integer getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(Integer maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }

    public Integer getMaDichVu() { return maDichVu; }
    public void setMaDichVu(Integer maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public Integer getMaLoaiDichVu() { return maLoaiDichVu; }
    public void setMaLoaiDichVu(Integer maLoaiDichVu) { this.maLoaiDichVu = maLoaiDichVu; }

    public String getTenLoaiDichVu() { return tenLoaiDichVu; }
    public void setTenLoaiDichVu(String tenLoaiDichVu) { this.tenLoaiDichVu = tenLoaiDichVu; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
