package com.garage.dto;

import java.math.BigDecimal;

/**
 * Response DTO cho Phụ tùng sử dụng trong Phiếu sửa chữa.
 */
public class RepairPartResponse {

    private Integer maChiTiet;
    private Integer maPhieuSuaChua;
    private Integer maPhuTung;
    private Integer maDichVuChiTiet;
    private String tenDichVuChiTiet;
    private String maPhuTungCode;
    private String tenPhuTung;
    private String donViTinh;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;

    public RepairPartResponse() {}

    public RepairPartResponse(Integer maChiTiet, Integer maPhieuSuaChua, Integer maPhuTung,
                              String maPhuTungCode, String tenPhuTung, String donViTinh,
                              Integer soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        this(maChiTiet, maPhieuSuaChua, maPhuTung, null, null, maPhuTungCode, tenPhuTung, donViTinh, soLuong, donGia, thanhTien);
    }

    public RepairPartResponse(Integer maChiTiet, Integer maPhieuSuaChua,
                              Integer maPhuTung, Integer maDichVuChiTiet, String tenDichVuChiTiet,
                              String maPhuTungCode, String tenPhuTung,
                              String donViTinh, Integer soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        this.maChiTiet = maChiTiet;
        this.maPhieuSuaChua = maPhieuSuaChua;
        this.maPhuTung = maPhuTung;
        this.maDichVuChiTiet = maDichVuChiTiet;
        this.tenDichVuChiTiet = tenDichVuChiTiet;
        this.maPhuTungCode = maPhuTungCode;
        this.tenPhuTung = tenPhuTung;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public Integer getMaChiTiet() { return maChiTiet; }
    public void setMaChiTiet(Integer maChiTiet) { this.maChiTiet = maChiTiet; }

    public Integer getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(Integer maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }

    public Integer getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(Integer maPhuTung) { this.maPhuTung = maPhuTung; }

    public Integer getMaDichVuChiTiet() { return maDichVuChiTiet; }
    public void setMaDichVuChiTiet(Integer maDichVuChiTiet) { this.maDichVuChiTiet = maDichVuChiTiet; }

    public String getTenDichVuChiTiet() { return tenDichVuChiTiet; }
    public void setTenDichVuChiTiet(String tenDichVuChiTiet) { this.tenDichVuChiTiet = tenDichVuChiTiet; }

    public String getMaPhuTungCode() { return maPhuTungCode; }
    public void setMaPhuTungCode(String maPhuTungCode) { this.maPhuTungCode = maPhuTungCode; }

    public String getTenPhuTung() { return tenPhuTung; }
    public void setTenPhuTung(String tenPhuTung) { this.tenPhuTung = tenPhuTung; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }
}
