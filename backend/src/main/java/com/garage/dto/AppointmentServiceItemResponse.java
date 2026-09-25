package com.garage.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO trả về thông tin dịch vụ được chọn trong Lịch hẹn (DatLich_DichVu), kèm phụ tùng định mức và tổng giá ước tính.
 */
public class AppointmentServiceItemResponse {

    private Integer maDichVu;
    private String tenDichVu;
    private String moTa;
    private BigDecimal donGia;
    private Integer thoiGianDuKien;
    private Integer soLuong;
    private String ghiChu;

    private List<ServicePartResponse> parts = new ArrayList<>();
    private BigDecimal tienPhuTungDuKien = BigDecimal.ZERO;
    private BigDecimal tongGiaDuKien = BigDecimal.ZERO;

    public AppointmentServiceItemResponse() {}

    public AppointmentServiceItemResponse(Integer maDichVu, String tenDichVu, String moTa,
                                          BigDecimal donGia, Integer thoiGianDuKien,
                                          Integer soLuong, String ghiChu) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.donGia = donGia;
        this.thoiGianDuKien = thoiGianDuKien;
        this.soLuong = soLuong;
        this.ghiChu = ghiChu;
        this.parts = new ArrayList<>();
        this.tienPhuTungDuKien = BigDecimal.ZERO;
        this.tongGiaDuKien = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public AppointmentServiceItemResponse(Integer maDichVu, String tenDichVu, String moTa,
                                          BigDecimal donGia, Integer thoiGianDuKien,
                                          Integer soLuong, String ghiChu,
                                          List<ServicePartResponse> parts,
                                          BigDecimal tienPhuTungDuKien,
                                          BigDecimal tongGiaDuKien) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.donGia = donGia;
        this.thoiGianDuKien = thoiGianDuKien;
        this.soLuong = soLuong;
        this.ghiChu = ghiChu;
        this.parts = parts != null ? parts : new ArrayList<>();
        this.tienPhuTungDuKien = tienPhuTungDuKien != null ? tienPhuTungDuKien : BigDecimal.ZERO;
        this.tongGiaDuKien = tongGiaDuKien != null ? tongGiaDuKien : (donGia != null ? donGia : BigDecimal.ZERO);
    }

    public Integer getMaDichVu() { return maDichVu; }
    public void setMaDichVu(Integer maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public Integer getThoiGianDuKien() { return thoiGianDuKien; }
    public void setThoiGianDuKien(Integer thoiGianDuKien) { this.thoiGianDuKien = thoiGianDuKien; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public List<ServicePartResponse> getParts() { return parts; }
    public void setParts(List<ServicePartResponse> parts) { this.parts = parts; }

    public BigDecimal getTienPhuTungDuKien() { return tienPhuTungDuKien; }
    public void setTienPhuTungDuKien(BigDecimal tienPhuTungDuKien) { this.tienPhuTungDuKien = tienPhuTungDuKien; }

    public BigDecimal getTongGiaDuKien() { return tongGiaDuKien; }
    public void setTongGiaDuKien(BigDecimal tongGiaDuKien) { this.tongGiaDuKien = tongGiaDuKien; }
}
