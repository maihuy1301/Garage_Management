package com.garage.dto;

import java.math.BigDecimal;

public class BranchReportResponse {

    private Integer maChiNhanh;
    private String tenChiNhanh;
    private BigDecimal tongDoanhThu;
    private long soLichHen;
    private long soPhieuSuaChua;
    private long soPhieuHoanTat;

    public BranchReportResponse() {}

    public BranchReportResponse(Integer maChiNhanh, String tenChiNhanh,
                                BigDecimal tongDoanhThu, long soLichHen,
                                long soPhieuSuaChua, long soPhieuHoanTat) {
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.tongDoanhThu = tongDoanhThu;
        this.soLichHen = soLichHen;
        this.soPhieuSuaChua = soPhieuSuaChua;
        this.soPhieuHoanTat = soPhieuHoanTat;
    }

    public Integer getMaChiNhanh() {
        return maChiNhanh;
    }

    public void setMaChiNhanh(Integer maChiNhanh) {
        this.maChiNhanh = maChiNhanh;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public BigDecimal getTongDoanhThu() {
        return tongDoanhThu;
    }

    public void setTongDoanhThu(BigDecimal tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }

    public long getSoLichHen() {
        return soLichHen;
    }

    public void setSoLichHen(long soLichHen) {
        this.soLichHen = soLichHen;
    }

    public long getSoPhieuSuaChua() {
        return soPhieuSuaChua;
    }

    public void setSoPhieuSuaChua(long soPhieuSuaChua) {
        this.soPhieuSuaChua = soPhieuSuaChua;
    }

    public long getSoPhieuHoanTat() {
        return soPhieuHoanTat;
    }

    public void setSoPhieuHoanTat(long soPhieuHoanTat) {
        this.soPhieuHoanTat = soPhieuHoanTat;
    }
}
