package com.garage.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO trả về thông tin danh mục dịch vụ (DichVu), kèm phụ tùng định mức và tổng giá ước tính.
 */
public class ServiceResponse {

    private Integer maDichVu;
    private Integer maLoaiDichVu;
    private String tenLoaiDichVu;
    private String tenDichVu;
    private String moTa;
    private BigDecimal donGia;
    private Integer thoiGianDuKien;
    private Boolean trangThai;

    private List<ServicePartResponse> parts = new ArrayList<>();
    private BigDecimal tienPhuTungDuKien = BigDecimal.ZERO;
    private BigDecimal tongGiaDuKien = BigDecimal.ZERO;

    public ServiceResponse() {}

    public ServiceResponse(Integer maDichVu, Integer maLoaiDichVu, String tenLoaiDichVu,
                           String tenDichVu, String moTa, BigDecimal donGia,
                           Integer thoiGianDuKien, Boolean trangThai) {
        this.maDichVu = maDichVu;
        this.maLoaiDichVu = maLoaiDichVu;
        this.tenLoaiDichVu = tenLoaiDichVu;
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.donGia = donGia;
        this.thoiGianDuKien = thoiGianDuKien;
        this.trangThai = trangThai;
        this.parts = new ArrayList<>();
        this.tienPhuTungDuKien = BigDecimal.ZERO;
        this.tongGiaDuKien = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public ServiceResponse(Integer maDichVu, Integer maLoaiDichVu, String tenLoaiDichVu,
                           String tenDichVu, String moTa, BigDecimal donGia,
                           Integer thoiGianDuKien, Boolean trangThai,
                           List<ServicePartResponse> parts, BigDecimal tienPhuTungDuKien, BigDecimal tongGiaDuKien) {
        this.maDichVu = maDichVu;
        this.maLoaiDichVu = maLoaiDichVu;
        this.tenLoaiDichVu = tenLoaiDichVu;
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.donGia = donGia;
        this.thoiGianDuKien = thoiGianDuKien;
        this.trangThai = trangThai;
        this.parts = parts != null ? parts : new ArrayList<>();
        this.tienPhuTungDuKien = tienPhuTungDuKien != null ? tienPhuTungDuKien : BigDecimal.ZERO;
        this.tongGiaDuKien = tongGiaDuKien != null ? tongGiaDuKien : (donGia != null ? donGia : BigDecimal.ZERO);
    }

    public Integer getMaDichVu() { return maDichVu; }
    public void setMaDichVu(Integer maDichVu) { this.maDichVu = maDichVu; }

    public Integer getMaLoaiDichVu() { return maLoaiDichVu; }
    public void setMaLoaiDichVu(Integer maLoaiDichVu) { this.maLoaiDichVu = maLoaiDichVu; }

    public String getTenLoaiDichVu() { return tenLoaiDichVu; }
    public void setTenLoaiDichVu(String tenLoaiDichVu) { this.tenLoaiDichVu = tenLoaiDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public Integer getThoiGianDuKien() { return thoiGianDuKien; }
    public void setThoiGianDuKien(Integer thoiGianDuKien) { this.thoiGianDuKien = thoiGianDuKien; }

    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }

    public List<ServicePartResponse> getParts() { return parts; }
    public void setParts(List<ServicePartResponse> parts) { this.parts = parts; }

    public BigDecimal getTienPhuTungDuKien() { return tienPhuTungDuKien; }
    public void setTienPhuTungDuKien(BigDecimal tienPhuTungDuKien) { this.tienPhuTungDuKien = tienPhuTungDuKien; }

    public BigDecimal getTongGiaDuKien() { return tongGiaDuKien; }
    public void setTongGiaDuKien(BigDecimal tongGiaDuKien) { this.tongGiaDuKien = tongGiaDuKien; }
}
