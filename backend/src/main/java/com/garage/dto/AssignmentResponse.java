package com.garage.dto;

import java.time.LocalDateTime;

/**
 * Response DTO cho Phân công kỹ thuật viên.
 */
public class AssignmentResponse {

    private Integer maPhanCong;
    private Integer maPhieuSuaChua;
    private Integer maNhanVien;
    private String maNhanVienCode;
    private String tenNhanVien;
    private Integer maChiNhanh;
    private String maChiNhanhCode;
    private String tenChiNhanh;
    private String vaiTroTrongCongViec;
    private LocalDateTime thoiGianPhanCong;
    private String trangThai;

    public AssignmentResponse() {}

    public AssignmentResponse(Integer maPhanCong, Integer maPhieuSuaChua,
                              Integer maNhanVien, String maNhanVienCode, String tenNhanVien,
                              Integer maChiNhanh, String maChiNhanhCode, String tenChiNhanh,
                              String vaiTroTrongCongViec, LocalDateTime thoiGianPhanCong,
                              String trangThai) {
        this.maPhanCong = maPhanCong;
        this.maPhieuSuaChua = maPhieuSuaChua;
        this.maNhanVien = maNhanVien;
        this.maNhanVienCode = maNhanVienCode;
        this.tenNhanVien = tenNhanVien;
        this.maChiNhanh = maChiNhanh;
        this.maChiNhanhCode = maChiNhanhCode;
        this.tenChiNhanh = tenChiNhanh;
        this.vaiTroTrongCongViec = vaiTroTrongCongViec;
        this.thoiGianPhanCong = thoiGianPhanCong;
        this.trangThai = trangThai;
    }

    public Integer getMaPhanCong() { return maPhanCong; }
    public void setMaPhanCong(Integer maPhanCong) { this.maPhanCong = maPhanCong; }

    public Integer getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(Integer maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }

    public Integer getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(Integer maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getMaNhanVienCode() { return maNhanVienCode; }
    public void setMaNhanVienCode(String maNhanVienCode) { this.maNhanVienCode = maNhanVienCode; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public Integer getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Integer maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getMaChiNhanhCode() { return maChiNhanhCode; }
    public void setMaChiNhanhCode(String maChiNhanhCode) { this.maChiNhanhCode = maChiNhanhCode; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public String getVaiTroTrongCongViec() { return vaiTroTrongCongViec; }
    public void setVaiTroTrongCongViec(String vaiTroTrongCongViec) { this.vaiTroTrongCongViec = vaiTroTrongCongViec; }

    public LocalDateTime getThoiGianPhanCong() { return thoiGianPhanCong; }
    public void setThoiGianPhanCong(LocalDateTime thoiGianPhanCong) { this.thoiGianPhanCong = thoiGianPhanCong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
