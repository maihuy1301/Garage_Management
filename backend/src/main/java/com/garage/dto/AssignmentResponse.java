package com.garage.dto;

import java.time.LocalDateTime;

/**
 * Response DTO cho Phân công kỹ thuật viên.
 */
public class AssignmentResponse {

    private Integer maPhanCong;
    private Integer maPhieuSuaChua;
    private Integer maNguoiPhanCong;
    private String tenNguoiPhanCong;
    private Integer maNguoiDuyet;
    private String tenNguoiDuyet;
    private Integer maNhanVien;
    private String tenNhanVien;
    private Integer maChiNhanh;
    private String tenChiNhanh;
    private String ghiChu;
    private LocalDateTime thoiGianTao;
    private LocalDateTime thoiGianDuyet;
    private String trangThai;

    public AssignmentResponse() {}

    public AssignmentResponse(Integer maPhanCong, Integer maPhieuSuaChua,
                              Integer maNguoiPhanCong, String tenNguoiPhanCong,
                              Integer maNguoiDuyet, String tenNguoiDuyet,
                              Integer maNhanVien, String tenNhanVien,
                              Integer maChiNhanh, String tenChiNhanh,
                              String ghiChu,
                              LocalDateTime thoiGianTao, LocalDateTime thoiGianDuyet,
                              String trangThai) {
        this.maPhanCong = maPhanCong;
        this.maPhieuSuaChua = maPhieuSuaChua;
        this.maNguoiPhanCong = maNguoiPhanCong;
        this.tenNguoiPhanCong = tenNguoiPhanCong;
        this.maNguoiDuyet = maNguoiDuyet;
        this.tenNguoiDuyet = tenNguoiDuyet;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.ghiChu = ghiChu;
        this.thoiGianTao = thoiGianTao;
        this.thoiGianDuyet = thoiGianDuyet;
        this.trangThai = trangThai;
    }

    public Integer getMaPhanCong() { return maPhanCong; }
    public void setMaPhanCong(Integer maPhanCong) { this.maPhanCong = maPhanCong; }

    public Integer getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(Integer maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }

    public Integer getMaNguoiPhanCong() { return maNguoiPhanCong; }
    public void setMaNguoiPhanCong(Integer maNguoiPhanCong) { this.maNguoiPhanCong = maNguoiPhanCong; }

    public String getTenNguoiPhanCong() { return tenNguoiPhanCong; }
    public void setTenNguoiPhanCong(String tenNguoiPhanCong) { this.tenNguoiPhanCong = tenNguoiPhanCong; }

    public Integer getMaNguoiDuyet() { return maNguoiDuyet; }
    public void setMaNguoiDuyet(Integer maNguoiDuyet) { this.maNguoiDuyet = maNguoiDuyet; }

    public String getTenNguoiDuyet() { return tenNguoiDuyet; }
    public void setTenNguoiDuyet(String tenNguoiDuyet) { this.tenNguoiDuyet = tenNguoiDuyet; }

    public Integer getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(Integer maNhanVien) { this.maNhanVien = maNhanVien; }

    public Integer getMaNhanVienDuocPhanCong() { return maNhanVien; }
    public void setMaNhanVienDuocPhanCong(Integer maNhanVienDuocPhanCong) { this.maNhanVien = maNhanVienDuocPhanCong; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public Integer getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Integer maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getVaiTroTrongCongViec() { return ghiChu; }
    public void setVaiTroTrongCongViec(String vaiTroTrongCongViec) { this.ghiChu = vaiTroTrongCongViec; }

    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }

    public LocalDateTime getThoiGianPhanCong() { return thoiGianTao; }
    public void setThoiGianPhanCong(LocalDateTime thoiGianPhanCong) { this.thoiGianTao = thoiGianPhanCong; }

    public LocalDateTime getThoiGianDuyet() { return thoiGianDuyet; }
    public void setThoiGianDuyet(LocalDateTime thoiGianDuyet) { this.thoiGianDuyet = thoiGianDuyet; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
