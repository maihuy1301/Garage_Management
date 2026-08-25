package com.garage.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO cập nhật hạng mục dịch vụ trong phiếu sửa chữa.
 */
public class UpdateRepairItemRequest {

    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private Integer soLuong;

    @DecimalMin(value = "0.0", message = "Đơn giá không được nhỏ hơn 0")
    private BigDecimal donGia;

    @Size(max = 30, message = "Trạng thái không được vượt quá 30 ký tự")
    private String trangThai;

    public UpdateRepairItemRequest() {}

    public UpdateRepairItemRequest(Integer soLuong, BigDecimal donGia) {
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public UpdateRepairItemRequest(Integer soLuong, BigDecimal donGia, String trangThai) {
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.trangThai = trangThai;
    }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
