package com.garage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Integer maThanhToan;
    private Integer maHoaDon;
    private BigDecimal soTien;
    private String phuongThuc;
    private String maGiaoDich;
    private LocalDateTime thoiGianThanhToan;
    private String trangThai;

    public PaymentResponse() {}

    public PaymentResponse(Integer maThanhToan, Integer maHoaDon, BigDecimal soTien,
                           String phuongThuc, String maGiaoDich,
                           LocalDateTime thoiGianThanhToan, String trangThai) {
        this.maThanhToan = maThanhToan;
        this.maHoaDon = maHoaDon;
        this.soTien = soTien;
        this.phuongThuc = phuongThuc;
        this.maGiaoDich = maGiaoDich;
        this.thoiGianThanhToan = thoiGianThanhToan;
        this.trangThai = trangThai;
    }

    public Integer getMaThanhToan() { return maThanhToan; }
    public void setMaThanhToan(Integer maThanhToan) { this.maThanhToan = maThanhToan; }

    public Integer getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(Integer maHoaDon) { this.maHoaDon = maHoaDon; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

    public String getMaGiaoDich() { return maGiaoDich; }
    public void setMaGiaoDich(String maGiaoDich) { this.maGiaoDich = maGiaoDich; }

    public LocalDateTime getThoiGianThanhToan() { return thoiGianThanhToan; }
    public void setThoiGianThanhToan(LocalDateTime thoiGianThanhToan) { this.thoiGianThanhToan = thoiGianThanhToan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
