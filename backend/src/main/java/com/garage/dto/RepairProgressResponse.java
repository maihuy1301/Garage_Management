package com.garage.dto;

import java.time.LocalDateTime;

/**
 * Response DTO cho Tiến độ sửa chữa.
 */
public class RepairProgressResponse {

    private Integer maTienDo;
    private Integer maPhieuSuaChua;
    private Integer maNhanVien;
    private String tenNhanVien;
    private String trangThai;
    private Integer phanTramHoanThanh;
    private String moTa;
    private LocalDateTime thoiGian;

    public RepairProgressResponse() {}

    public RepairProgressResponse(Integer maTienDo, Integer maPhieuSuaChua,
                                  Integer maNhanVien, String tenNhanVien,
                                  String trangThai, Integer phanTramHoanThanh,
                                  String moTa, LocalDateTime thoiGian) {
        this.maTienDo = maTienDo;
        this.maPhieuSuaChua = maPhieuSuaChua;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.trangThai = trangThai;
        this.phanTramHoanThanh = phanTramHoanThanh;
        this.moTa = moTa;
        this.thoiGian = thoiGian;
    }

    public Integer getMaTienDo() { return maTienDo; }
    public void setMaTienDo(Integer maTienDo) { this.maTienDo = maTienDo; }

    public Integer getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(Integer maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }

    public Integer getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(Integer maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Integer getPhanTramHoanThanh() { return phanTramHoanThanh; }
    public void setPhanTramHoanThanh(Integer phanTramHoanThanh) { this.phanTramHoanThanh = phanTramHoanThanh; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
}
