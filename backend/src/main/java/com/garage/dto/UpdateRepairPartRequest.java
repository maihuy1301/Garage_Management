package com.garage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO để cập nhật số lượng/dịch vụ liên kết của phụ tùng trong Phiếu sửa chữa.
 */
public class UpdateRepairPartRequest {

    private Integer maDichVuChiTiet;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phụ tùng phải lớn hơn hoặc bằng 1")
    private Integer soLuong;

    public UpdateRepairPartRequest() {}

    public UpdateRepairPartRequest(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public UpdateRepairPartRequest(Integer maDichVuChiTiet, Integer soLuong) {
        this.maDichVuChiTiet = maDichVuChiTiet;
        this.soLuong = soLuong;
    }

    public Integer getMaDichVuChiTiet() {
        return maDichVuChiTiet;
    }

    public void setMaDichVuChiTiet(Integer maDichVuChiTiet) {
        this.maDichVuChiTiet = maDichVuChiTiet;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
}
