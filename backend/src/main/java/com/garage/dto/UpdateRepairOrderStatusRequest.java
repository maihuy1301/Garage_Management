package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO cập nhật trạng thái phiếu sửa chữa.
 * Các giá trị hợp lệ: CHO_XU_LY, DA_PHAN_CONG, DANG_SUA, CHO_KH_DUYET, TAM_DUNG, HOAN_TAT, HUY
 */
public class UpdateRepairOrderStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 30, message = "Trạng thái không được vượt quá 30 ký tự")
    private String trangThai;

    public UpdateRepairOrderStatusRequest() {}

    public UpdateRepairOrderStatusRequest(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
