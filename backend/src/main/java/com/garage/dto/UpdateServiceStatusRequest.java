package com.garage.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateServiceStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean trangThai;

    public UpdateServiceStatusRequest() {}

    public UpdateServiceStatusRequest(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
