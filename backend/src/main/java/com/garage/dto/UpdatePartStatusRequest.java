package com.garage.dto;

import jakarta.validation.constraints.NotNull;

public class UpdatePartStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean trangThai;

    public UpdatePartStatusRequest() {}

    public UpdatePartStatusRequest(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
