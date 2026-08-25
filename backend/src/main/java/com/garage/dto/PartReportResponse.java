package com.garage.dto;

import java.math.BigDecimal;

public class PartReportResponse {

    private Integer maPhuTung;
    private String maPhuTungCode;
    private String tenPhuTung;
    private long soLuongSuDung;
    private BigDecimal doanhThu;

    public PartReportResponse() {}

    public PartReportResponse(Integer maPhuTung, String maPhuTungCode, String tenPhuTung,
                              long soLuongSuDung, BigDecimal doanhThu) {
        this.maPhuTung = maPhuTung;
        this.maPhuTungCode = maPhuTungCode;
        this.tenPhuTung = tenPhuTung;
        this.soLuongSuDung = soLuongSuDung;
        this.doanhThu = doanhThu != null ? doanhThu : BigDecimal.ZERO;
    }

    public Integer getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(Integer maPhuTung) { this.maPhuTung = maPhuTung; }

    public String getMaPhuTungCode() { return maPhuTungCode; }
    public void setMaPhuTungCode(String maPhuTungCode) { this.maPhuTungCode = maPhuTungCode; }

    public String getTenPhuTung() { return tenPhuTung; }
    public void setTenPhuTung(String tenPhuTung) { this.tenPhuTung = tenPhuTung; }

    public long getSoLuongSuDung() { return soLuongSuDung; }
    public void setSoLuongSuDung(long soLuongSuDung) { this.soLuongSuDung = soLuongSuDung; }

    public BigDecimal getDoanhThu() { return doanhThu; }
    public void setDoanhThu(BigDecimal doanhThu) { this.doanhThu = doanhThu; }
}
