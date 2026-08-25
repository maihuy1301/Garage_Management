package com.garage.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO thêm dịch vụ vào phiếu sửa chữa.
 * Giá dịch vụ sẽ được ưu tiên lấy tự động từ catalog Giá dịch vụ của chi nhánh.
 */
public class CreateRepairItemRequest {

    @NotNull(message = "Mã dịch vụ không được để trống")
    private Integer maDichVu;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private Integer soLuong = 1;

    @DecimalMin(value = "0.0", message = "Đơn giá không được nhỏ hơn 0")
    private BigDecimal donGia;

    @Size(max = 30, message = "Trạng thái không được vượt quá 30 ký tự")
    private String trangThai;

    public CreateRepairItemRequest() {}

    public CreateRepairItemRequest(Integer maDichVu, Integer soLuong) {
        this.maDichVu = maDichVu;
        this.soLuong = soLuong;
    }

    public CreateRepairItemRequest(Integer maDichVu, Integer soLuong, BigDecimal donGia) {
        this.maDichVu = maDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public Integer getMaDichVu() { return maDichVu; }
    public void setMaDichVu(Integer maDichVu) { this.maDichVu = maDichVu; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
