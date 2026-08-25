package com.garage.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateEmployeeStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean trangThai;

    public UpdateEmployeeStatusRequest() {}

    public UpdateEmployeeStatusRequest(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
