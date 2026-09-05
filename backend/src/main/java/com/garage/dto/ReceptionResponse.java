package com.garage.dto;

import java.time.LocalDateTime;

/**
 * Response DTO cho Phiếu tiếp nhận (Reception Response).
 */
public class ReceptionResponse {

    private Integer maTiepNhan;
    private Integer maDatLich;

    private Integer maXe;
    private String bienSoXe;
    private String hangXe;
    private String modelXe;

    private Integer maKhachHang;
    private String tenKhachHang;
    private String soDienThoaiKhachHang;

    private Integer maChiNhanh;
    private String maChiNhanhCode;
    private String tenChiNhanh;

    private Integer maNhanVienTiepNhan;
    private String maNhanVienCode;
    private String tenNhanVienTiepNhan;

    private LocalDateTime thoiGianTiepNhan;
    private Integer soKm;
    private String tinhTrangNgoaiThat;
    private String yeuCauKhachHang;
    private String trangThai;

    public ReceptionResponse() {}

    public ReceptionResponse(Integer maTiepNhan, Integer maDatLich,
                             Integer maXe, String bienSoXe, String hangXe, String modelXe,
                             Integer maKhachHang, String tenKhachHang, String soDienThoaiKhachHang,
                             Integer maChiNhanh, String maChiNhanhCode, String tenChiNhanh,
                             Integer maNhanVienTiepNhan, String maNhanVienCode, String tenNhanVienTiepNhan,
                             LocalDateTime thoiGianTiepNhan, Integer soKm,
                             String tinhTrangNgoaiThat, String yeuCauKhachHang, String trangThai) {
        this.maTiepNhan = maTiepNhan;
        this.maDatLich = maDatLich;
        this.maXe = maXe;
        this.bienSoXe = bienSoXe;
        this.hangXe = hangXe;
        this.modelXe = modelXe;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoaiKhachHang = soDienThoaiKhachHang;
        this.maChiNhanh = maChiNhanh;
        this.maChiNhanhCode = maChiNhanhCode;
        this.tenChiNhanh = tenChiNhanh;
        this.maNhanVienTiepNhan = maNhanVienTiepNhan;
        this.maNhanVienCode = maNhanVienCode;
        this.tenNhanVienTiepNhan = tenNhanVienTiepNhan;
        this.thoiGianTiepNhan = thoiGianTiepNhan;
        this.soKm = soKm;
        this.tinhTrangNgoaiThat = tinhTrangNgoaiThat;
        this.yeuCauKhachHang = yeuCauKhachHang;
        this.trangThai = trangThai;
    }

    public Integer getMaTiepNhan() { return maTiepNhan; }
    public void setMaTiepNhan(Integer maTiepNhan) { this.maTiepNhan = maTiepNhan; }

    public Integer getMaDatLich() { return maDatLich; }
    public void setMaDatLich(Integer maDatLich) { this.maDatLich = maDatLich; }

    public Integer getMaXe() { return maXe; }
    public void setMaXe(Integer maXe) { this.maXe = maXe; }

    public String getBienSoXe() { return bienSoXe; }
    public void setBienSoXe(String bienSoXe) { this.bienSoXe = bienSoXe; }

    public String getHangXe() { return hangXe; }
    public void setHangXe(String hangXe) { this.hangXe = hangXe; }

    public String getModelXe() { return modelXe; }
    public void setModelXe(String modelXe) { this.modelXe = modelXe; }

    public Integer getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(Integer maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSoDienThoaiKhachHang() { return soDienThoaiKhachHang; }
    public void setSoDienThoaiKhachHang(String soDienThoaiKhachHang) { this.soDienThoaiKhachHang = soDienThoaiKhachHang; }

    public Integer getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Integer maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getMaChiNhanhCode() { return maChiNhanhCode; }
    public void setMaChiNhanhCode(String maChiNhanhCode) { this.maChiNhanhCode = maChiNhanhCode; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public Integer getMaNhanVienTiepNhan() { return maNhanVienTiepNhan; }
    public void setMaNhanVienTiepNhan(Integer maNhanVienTiepNhan) { this.maNhanVienTiepNhan = maNhanVienTiepNhan; }

    public String getMaNhanVienCode() { return maNhanVienCode; }
    public void setMaNhanVienCode(String maNhanVienCode) { this.maNhanVienCode = maNhanVienCode; }

    public String getTenNhanVienTiepNhan() { return tenNhanVienTiepNhan; }
    public void setTenNhanVienTiepNhan(String tenNhanVienTiepNhan) { this.tenNhanVienTiepNhan = tenNhanVienTiepNhan; }

    public LocalDateTime getThoiGianTiepNhan() { return thoiGianTiepNhan; }
    public void setThoiGianTiepNhan(LocalDateTime thoiGianTiepNhan) { this.thoiGianTiepNhan = thoiGianTiepNhan; }

    public Integer getSoKm() { return soKm; }
    public void setSoKm(Integer soKm) { this.soKm = soKm; }

    public String getTinhTrangNgoaiThat() { return tinhTrangNgoaiThat; }
    public void setTinhTrangNgoaiThat(String tinhTrangNgoaiThat) { this.tinhTrangNgoaiThat = tinhTrangNgoaiThat; }

    public String getYeuCauKhachHang() { return yeuCauKhachHang; }
    public void setYeuCauKhachHang(String yeuCauKhachHang) { this.yeuCauKhachHang = yeuCauKhachHang; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
