package com.garage.dto;

public class TechnicianReportResponse {

    private Integer maNhanVien;
    private String tenNhanVien;
    private long tongPhanCong;
    private long daHoanTat;
    private long dangThucHien;

    public TechnicianReportResponse() {}

    public TechnicianReportResponse(Integer maNhanVien, String tenNhanVien,
                                    long tongPhanCong, long daHoanTat, long dangThucHien) {
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.tongPhanCong = tongPhanCong;
        this.daHoanTat = daHoanTat;
        this.dangThucHien = dangThucHien;
    }

    public Integer getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(Integer maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public long getTongPhanCong() {
        return tongPhanCong;
    }

    public void setTongPhanCong(long tongPhanCong) {
        this.tongPhanCong = tongPhanCong;
    }

    public long getDaHoanTat() {
        return daHoanTat;
    }

    public void setDaHoanTat(long daHoanTat) {
        this.daHoanTat = daHoanTat;
    }

    public long getDangThucHien() {
        return dangThucHien;
    }

    public void setDangThucHien(long dangThucHien) {
        this.dangThucHien = dangThucHien;
    }
}
