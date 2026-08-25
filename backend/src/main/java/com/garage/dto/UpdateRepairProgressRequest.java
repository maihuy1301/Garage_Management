package com.garage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO để kỹ thuật viên cập nhật tiến độ sửa chữa.
 */
public class UpdateRepairProgressRequest {

    @NotBlank(message = "Trạng thái tiến độ không được để trống")
    @Size(max = 30, message = "Trạng thái không được vượt quá 30 ký tự")
    private String trangThai;

    @Min(value = 0, message = "Phần trăm hoàn thành phải từ 0 đến 100")
    @Max(value = 100, message = "Phần trăm hoàn thành phải từ 0 đến 100")
    private Integer phanTramHoanThanh;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String moTa;

    public UpdateRepairProgressRequest() {}

    public UpdateRepairProgressRequest(String trangThai, Integer phanTramHoanThanh, String moTa) {
        this.trangThai = trangThai;
        this.phanTramHoanThanh = phanTramHoanThanh;
        this.moTa = moTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getPhanTramHoanThanh() {
        return phanTramHoanThanh;
    }

    public void setPhanTramHoanThanh(Integer phanTramHoanThanh) {
        this.phanTramHoanThanh = phanTramHoanThanh;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
