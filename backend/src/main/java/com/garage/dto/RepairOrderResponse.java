package com.garage.dto;

import java.time.LocalDateTime;

/**
 * Response DTO cho Phiếu sửa chữa (Repair Order Response).
 */
public class RepairOrderResponse {

    private Integer maPhieuSuaChua;
    private Integer maPhieuCha;
    private Integer maTiepNhan;
    private Integer maDatLich;

    private Integer maXe;
    private String bienSoXe;
    private Integer maHangXe;
    private String tenHangXe;
    private Integer maModel;
    private String tenModel;
    private String hangXe;
    private String modelXe;

    private Integer maKhachHang;
    private String tenKhachHang;
    private String soDienThoaiKhachHang;

    private Integer maChiNhanh;
    private String tenChiNhanh;

    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianHoanTat;
    private String trangThai;
    private String ghiChu;

    public RepairOrderResponse() {}

    public RepairOrderResponse(Integer maPhieuSuaChua, Integer maTiepNhan, Integer maDatLich,
                               Integer maXe, String bienSoXe, String hangXe, String modelXe,
                               Integer maKhachHang, String tenKhachHang, String soDienThoaiKhachHang,
                               Integer maChiNhanh, String tenChiNhanh,
                               LocalDateTime thoiGianBatDau, LocalDateTime thoiGianHoanTat,
                               String trangThai, String ghiChu) {
        this(maPhieuSuaChua, null, maTiepNhan, maDatLich, maXe, bienSoXe,
                null, hangXe, null, modelXe,
                maKhachHang, tenKhachHang, soDienThoaiKhachHang,
                maChiNhanh, tenChiNhanh,
                thoiGianBatDau, thoiGianHoanTat, trangThai, ghiChu);
    }

    public RepairOrderResponse(Integer maPhieuSuaChua, Integer maPhieuCha, Integer maTiepNhan, Integer maDatLich,
                               Integer maXe, String bienSoXe,
                               Integer maHangXe, String tenHangXe,
                               Integer maModel, String tenModel,
                               Integer maKhachHang, String tenKhachHang, String soDienThoaiKhachHang,
                               Integer maChiNhanh, String tenChiNhanh,
                               LocalDateTime thoiGianBatDau, LocalDateTime thoiGianHoanTat,
                               String trangThai, String ghiChu) {
        this.maPhieuSuaChua = maPhieuSuaChua;
        this.maPhieuCha = maPhieuCha;
        this.maTiepNhan = maTiepNhan;
        this.maDatLich = maDatLich;
        this.maXe = maXe;
        this.bienSoXe = bienSoXe;
        this.maHangXe = maHangXe;
        this.tenHangXe = tenHangXe;
        this.maModel = maModel;
        this.tenModel = tenModel;
        this.hangXe = (tenHangXe != null) ? tenHangXe : "";
        this.modelXe = (tenModel != null) ? tenModel : "";
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoaiKhachHang = soDienThoaiKhachHang;
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianHoanTat = thoiGianHoanTat;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public Integer getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(Integer maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }

    public Integer getMaPhieuCha() { return maPhieuCha; }
    public void setMaPhieuCha(Integer maPhieuCha) { this.maPhieuCha = maPhieuCha; }

    public Integer getMaTiepNhan() { return maTiepNhan; }
    public void setMaTiepNhan(Integer maTiepNhan) { this.maTiepNhan = maTiepNhan; }

    public Integer getMaDatLich() { return maDatLich; }
    public void setMaDatLich(Integer maDatLich) { this.maDatLich = maDatLich; }

    public Integer getMaXe() { return maXe; }
    public void setMaXe(Integer maXe) { this.maXe = maXe; }

    public String getBienSoXe() { return bienSoXe; }
    public void setBienSoXe(String bienSoXe) { this.bienSoXe = bienSoXe; }

    public Integer getMaHangXe() { return maHangXe; }
    public void setMaHangXe(Integer maHangXe) { this.maHangXe = maHangXe; }

    public String getTenHangXe() { return tenHangXe; }
    public void setTenHangXe(String tenHangXe) { this.tenHangXe = tenHangXe; }

    public Integer getMaModel() { return maModel; }
    public void setMaModel(Integer maModel) { this.maModel = maModel; }

    public String getTenModel() { return tenModel; }
    public void setTenModel(String tenModel) { this.tenModel = tenModel; }

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

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }

    public LocalDateTime getThoiGianHoanTat() { return thoiGianHoanTat; }
    public void setThoiGianHoanTat(LocalDateTime thoiGianHoanTat) { this.thoiGianHoanTat = thoiGianHoanTat; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
