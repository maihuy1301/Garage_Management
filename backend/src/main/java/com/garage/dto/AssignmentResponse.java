package com.garage.dto;

import java.time.LocalDateTime;

public class AssignmentResponse {

    private Integer maPhanCong;
    private Integer maPhieuSuaChua;
    private Integer maQuanLy;
    private String tenQuanLy;
    private Integer maNhanVienDuocPhanCong;
    private String tenNhanVienDuocPhanCong;
    private LocalDateTime thoiGianPhanCong;
    private String trangThai;

    public AssignmentResponse() {}

    public AssignmentResponse(Integer maPhanCong, Integer maPhieuSuaChua,
                              Integer maQuanLy, String tenQuanLy,
                              Integer maNhanVienDuocPhanCong, String tenNhanVienDuocPhanCong,
                              LocalDateTime thoiGianPhanCong, String trangThai) {
        this.maPhanCong = maPhanCong;
        this.maPhieuSuaChua = maPhieuSuaChua;
        this.maQuanLy = maQuanLy;
        this.tenQuanLy = tenQuanLy;
        this.maNhanVienDuocPhanCong = maNhanVienDuocPhanCong;
        this.tenNhanVienDuocPhanCong = tenNhanVienDuocPhanCong;
        this.thoiGianPhanCong = thoiGianPhanCong;
        this.trangThai = trangThai;
    }

    public Integer getMaPhanCong() {
        return maPhanCong;
    }

    public void setMaPhanCong(Integer maPhanCong) {
        this.maPhanCong = maPhanCong;
    }

    public Integer getMaPhieuSuaChua() {
        return maPhieuSuaChua;
    }

    public void setMaPhieuSuaChua(Integer maPhieuSuaChua) {
        this.maPhieuSuaChua = maPhieuSuaChua;
    }

    public Integer getMaQuanLy() {
        return maQuanLy;
    }

    public void setMaQuanLy(Integer maQuanLy) {
        this.maQuanLy = maQuanLy;
    }

    public String getTenQuanLy() {
        return tenQuanLy;
    }

    public void setTenQuanLy(String tenQuanLy) {
        this.tenQuanLy = tenQuanLy;
    }

    public Integer getMaNhanVienDuocPhanCong() {
        return maNhanVienDuocPhanCong;
    }

    public void setMaNhanVienDuocPhanCong(Integer maNhanVienDuocPhanCong) {
        this.maNhanVienDuocPhanCong = maNhanVienDuocPhanCong;
    }

    public String getTenNhanVienDuocPhanCong() {
        return tenNhanVienDuocPhanCong;
    }

    public void setTenNhanVienDuocPhanCong(String tenNhanVienDuocPhanCong) {
        this.tenNhanVienDuocPhanCong = tenNhanVienDuocPhanCong;
    }

    public LocalDateTime getThoiGianPhanCong() {
        return thoiGianPhanCong;
    }

    public void setThoiGianPhanCong(LocalDateTime thoiGianPhanCong) {
        this.thoiGianPhanCong = thoiGianPhanCong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
