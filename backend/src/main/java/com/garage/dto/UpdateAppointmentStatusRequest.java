package com.garage.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO cập nhật trạng thái lịch hẹn.
 */
public class UpdateAppointmentStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    private String trangThai;

    public UpdateAppointmentStatusRequest() {}

    public UpdateAppointmentStatusRequest(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
