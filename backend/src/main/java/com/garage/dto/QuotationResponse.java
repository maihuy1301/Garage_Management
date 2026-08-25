package com.garage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuotationResponse {

    private Integer maBaoGia;
    private Integer maPhieuSuaChua;
    private Integer maChiNhanh;
    private String tenChiNhanh;
    private String lyDoPhatSinh;
    private BigDecimal tongTien;
    private String trangThai;
    private LocalDateTime thoiGianTao;
    private LocalDateTime thoiGianDuyet;

    private List<QuotationServiceItemResponse> services = new ArrayList<>();
    private List<QuotationPartItemResponse> parts = new ArrayList<>();

    public QuotationResponse() {}

    public QuotationResponse(Integer maBaoGia, Integer maPhieuSuaChua,
                             Integer maChiNhanh, String tenChiNhanh,
                             String lyDoPhatSinh, BigDecimal tongTien,
                             String trangThai, LocalDateTime thoiGianTao, LocalDateTime thoiGianDuyet,
                             List<QuotationServiceItemResponse> services,
                             List<QuotationPartItemResponse> parts) {
        this.maBaoGia = maBaoGia;
        this.maPhieuSuaChua = maPhieuSuaChua;
        this.maChiNhanh = maChiNhanh;
        this.tenChiNhanh = tenChiNhanh;
        this.lyDoPhatSinh = lyDoPhatSinh;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.thoiGianTao = thoiGianTao;
        this.thoiGianDuyet = thoiGianDuyet;
        this.services = services != null ? services : new ArrayList<>();
        this.parts = parts != null ? parts : new ArrayList<>();
    }

    public Integer getMaBaoGia() { return maBaoGia; }
    public void setMaBaoGia(Integer maBaoGia) { this.maBaoGia = maBaoGia; }

    public Integer getMaPhieuSuaChua() { return maPhieuSuaChua; }
    public void setMaPhieuSuaChua(Integer maPhieuSuaChua) { this.maPhieuSuaChua = maPhieuSuaChua; }

    public Integer getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Integer maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public String getLyDoPhatSinh() { return lyDoPhatSinh; }
    public void setLyDoPhatSinh(String lyDoPhatSinh) { this.lyDoPhatSinh = lyDoPhatSinh; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }

    public LocalDateTime getThoiGianDuyet() { return thoiGianDuyet; }
    public void setThoiGianDuyet(LocalDateTime thoiGianDuyet) { this.thoiGianDuyet = thoiGianDuyet; }

    public List<QuotationServiceItemResponse> getServices() { return services; }
    public void setServices(List<QuotationServiceItemResponse> services) { this.services = services; }

    public List<QuotationPartItemResponse> getParts() { return parts; }
    public void setParts(List<QuotationPartItemResponse> parts) { this.parts = parts; }
}
