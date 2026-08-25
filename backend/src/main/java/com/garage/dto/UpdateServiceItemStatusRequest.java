package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO để kỹ thuật viên cập nhật trạng thái hạng mục dịch vụ.
 */
public class UpdateServiceItemStatusRequest {

    @NotBlank(message = "Trạng thái dịch vụ không được để trống")
    @Size(max = 30, message = "Trạng thái không được vượt quá 30 ký tự")
    private String trangThai;

    public UpdateServiceItemStatusRequest() {}

    public UpdateServiceItemStatusRequest(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
