package com.garage.dto;

import java.math.BigDecimal;

public class BranchReportResponse {

    private Integer maChiNhanh;
    private String maChiNhanhCode;
    private String tenChiNhanh;
    private BigDecimal doanhThu;
    private long soLichHen;
    private long soPhieuSuaChua;
    private long soPhieuHoanTat;

    public BranchReportResponse() {}

    public BranchReportResponse(Integer maChiNhanh, String maChiNhanhCode, String tenChiNhanh,
                                BigDecimal doanhThu, long soLichHen, long soPhieuSuaChua, long soPhieuHoanTat) {
        this.maChiNhanh = maChiNhanh;
        this.maChiNhanhCode = maChiNhanhCode;
        this.tenChiNhanh = tenChiNhanh;
        this.doanhThu = doanhThu != null ? doanhThu : BigDecimal.ZERO;
        this.soLichHen = soLichHen;
        this.soPhieuSuaChua = soPhieuSuaChua;
        this.soPhieuHoanTat = soPhieuHoanTat;
    }

    public Integer getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Integer maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getMaChiNhanhCode() { return maChiNhanhCode; }
    public void setMaChiNhanhCode(String maChiNhanhCode) { this.maChiNhanhCode = maChiNhanhCode; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public BigDecimal getDoanhThu() { return doanhThu; }
    public void setDoanhThu(BigDecimal doanhThu) { this.doanhThu = doanhThu; }

    public long getSoLichHen() { return soLichHen; }
    public void setSoLichHen(long soLichHen) { this.soLichHen = soLichHen; }

    public long getSoPhieuSuaChua() { return soPhieuSuaChua; }
    public void setSoPhieuSuaChua(long soPhieuSuaChua) { this.soPhieuSuaChua = soPhieuSuaChua; }

    public long getSoPhieuHoanTat() { return soPhieuHoanTat; }
    public void setSoPhieuHoanTat(long soPhieuHoanTat) { this.soPhieuHoanTat = soPhieuHoanTat; }
}
