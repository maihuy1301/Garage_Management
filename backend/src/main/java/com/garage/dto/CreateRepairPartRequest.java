package com.garage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO để thêm phụ tùng vào Phiếu sửa chữa.
 * Bổ sung maDichVuChiTiet để liên kết với hạng mục dịch vụ cụ thể.
 */
public class CreateRepairPartRequest {

    @NotNull(message = "Mã phụ tùng không được để trống")
    private Integer maPhuTung;

    private Integer maDichVuChiTiet;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phụ tùng phải lớn hơn hoặc bằng 1")
    private Integer soLuong;

    public CreateRepairPartRequest() {}

    public CreateRepairPartRequest(Integer maPhuTung, Integer soLuong) {
        this.maPhuTung = maPhuTung;
        this.soLuong = soLuong;
    }

    public CreateRepairPartRequest(Integer maPhuTung, Integer maDichVuChiTiet, Integer soLuong) {
        this.maPhuTung = maPhuTung;
        this.maDichVuChiTiet = maDichVuChiTiet;
        this.soLuong = soLuong;
    }

    public Integer getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(Integer maPhuTung) {
        this.maPhuTung = maPhuTung;
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
