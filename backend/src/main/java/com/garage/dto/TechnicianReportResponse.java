package com.garage.dto;

public class TechnicianReportResponse {

    private Integer maNhanVien;
    private String maNhanVienCode;
    private String hoTen;
    private long soPhieuPhanCong;
    private long soPhieuHoanTat;
    private long soPhieuDangSua;

    public TechnicianReportResponse() {}

    public TechnicianReportResponse(Integer maNhanVien, String maNhanVienCode, String hoTen,
                                    long soPhieuPhanCong, long soPhieuHoanTat, long soPhieuDangSua) {
        this.maNhanVien = maNhanVien;
        this.maNhanVienCode = maNhanVienCode;
        this.hoTen = hoTen;
        this.soPhieuPhanCong = soPhieuPhanCong;
        this.soPhieuHoanTat = soPhieuHoanTat;
        this.soPhieuDangSua = soPhieuDangSua;
    }

    public Integer getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(Integer maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getMaNhanVienCode() { return maNhanVienCode; }
    public void setMaNhanVienCode(String maNhanVienCode) { this.maNhanVienCode = maNhanVienCode; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public long getSoPhieuPhanCong() { return soPhieuPhanCong; }
    public void setSoPhieuPhanCong(long soPhieuPhanCong) { this.soPhieuPhanCong = soPhieuPhanCong; }

    public long getSoPhieuHoanTat() { return soPhieuHoanTat; }
    public void setSoPhieuHoanTat(long soPhieuHoanTat) { this.soPhieuHoanTat = soPhieuHoanTat; }

    public long getSoPhieuDangSua() { return soPhieuDangSua; }
    public void setSoPhieuDangSua(long soPhieuDangSua) { this.soPhieuDangSua = soPhieuDangSua; }
}
