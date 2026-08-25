package com.garage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO để cập nhật số lượng phụ tùng trong Phiếu sửa chữa.
 */
public class UpdateRepairPartRequest {

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phụ tùng phải lớn hơn hoặc bằng 1")
    private Integer soLuong;

    public UpdateRepairPartRequest() {}

    public UpdateRepairPartRequest(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
}
