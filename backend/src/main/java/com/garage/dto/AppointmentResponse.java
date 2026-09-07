package com.garage.dto;

import java.time.LocalDateTime;

/**
 * Response DTO cho DatLich — không expose trực tiếp JPA Entity.
 */
public class AppointmentResponse {

    private Integer maDatLich;
    private Integer maKhachHang;
    private String tenKhachHang;
    private String soDienThoaiKhachHang;

    private Integer maXe;
    private String bienSoXe;
    private String hangXe;
    private String modelXe;

    private Integer maChiNhanh;
    private String tenChiNhanh;

    private LocalDateTime thoiGianHen;
    private String trangThai;
    private String ghiChu;
    private LocalDateTime ngayDat;

    public AppointmentResponse() {}

    public AppointmentResponse(Integer maDatLich, Integer maKhachHang,
                               String tenKhachHang, String soDienThoaiKhachHang,
                               Integer maXe, String bienSoXe, String hangXe, String modelXe,
                               Integer maChiNhanh, String tenChiNhanh,
                               LocalDateTime thoiGianHen, String trangThai, String ghiChu,
                               LocalDateTime ngayDat) {
        this.maDatLich = maDatLich;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoaiKhachHang = soDienThoaiKhachHang;
        this.maXe = maXe;
        this.bienSoXe = bienSoXe;
        this.hangXe = hangXe;
        this.modelXe = modelXe;
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.thoiGianHen = thoiGianHen;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.ngayDat = ngayDat;
    }

    public Integer getMaDatLich() { return maDatLich; }
    public void setMaDatLich(Integer maDatLich) { this.maDatLich = maDatLich; }

    public Integer getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(Integer maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSoDienThoaiKhachHang() { return soDienThoaiKhachHang; }
    public void setSoDienThoaiKhachHang(String soDienThoaiKhachHang) { this.soDienThoaiKhachHang = soDienThoaiKhachHang; }

    public Integer getMaXe() { return maXe; }
    public void setMaXe(Integer maXe) { this.maXe = maXe; }

    public String getBienSoXe() { return bienSoXe; }
    public void setBienSoXe(String bienSoXe) { this.bienSoXe = bienSoXe; }

    public String getHangXe() { return hangXe; }
    public void setHangXe(String hangXe) { this.hangXe = hangXe; }

    public String getModelXe() { return modelXe; }
    public void setModelXe(String modelXe) { this.modelXe = modelXe; }

    public Integer getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Integer maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public LocalDateTime getThoiGianHen() { return thoiGianHen; }
    public void setThoiGianHen(LocalDateTime thoiGianHen) { this.thoiGianHen = thoiGianHen; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDateTime ngayDat) { this.ngayDat = ngayDat; }
}
