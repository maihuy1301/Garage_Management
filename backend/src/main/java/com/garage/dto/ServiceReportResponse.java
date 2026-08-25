package com.garage.dto;

import java.math.BigDecimal;

public class ServiceReportResponse {

    private Integer maDichVu;
    private String tenDichVu;
    private long soLanSuDung;
    private BigDecimal doanhThu;

    public ServiceReportResponse() {}

    public ServiceReportResponse(Integer maDichVu, String tenDichVu, long soLanSuDung, BigDecimal doanhThu) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.soLanSuDung = soLanSuDung;
        this.doanhThu = doanhThu != null ? doanhThu : BigDecimal.ZERO;
    }

    public Integer getMaDichVu() { return maDichVu; }
    public void setMaDichVu(Integer maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public long getSoLanSuDung() { return soLanSuDung; }
    public void setSoLanSuDung(long soLanSuDung) { this.soLanSuDung = soLanSuDung; }

    public BigDecimal getDoanhThu() { return doanhThu; }
    public void setDoanhThu(BigDecimal doanhThu) { this.doanhThu = doanhThu; }
}
