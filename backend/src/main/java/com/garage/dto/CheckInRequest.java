package com.garage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request DTO cho thao tác Tiếp nhận xe (Check-in).
 * Nhân viên tiếp nhận và chi nhánh được xác định tự động từ JWT SecurityContext.
 */
public class CheckInRequest {

    @Min(value = 0, message = "Số km không được nhỏ hơn 0")
    private Integer soKm;

    @Size(max = 1000, message = "Tình trạng ngoại thất không được vượt quá 1000 ký tự")
    private String tinhTrangNgoaiThat;

    @Size(max = 1000, message = "Yêu cầu khách hàng không được vượt quá 1000 ký tự")
    private String yeuCauKhachHang;

    public CheckInRequest() {}

    public CheckInRequest(Integer soKm, String tinhTrangNgoaiThat, String yeuCauKhachHang) {
        this.soKm = soKm;
        this.tinhTrangNgoaiThat = tinhTrangNgoaiThat;
        this.yeuCauKhachHang = yeuCauKhachHang;
    }

    public Integer getSoKm() { return soKm; }
    public void setSoKm(Integer soKm) { this.soKm = soKm; }

    public String getTinhTrangNgoaiThat() { return tinhTrangNgoaiThat; }
    public void setTinhTrangNgoaiThat(String tinhTrangNgoaiThat) { this.tinhTrangNgoaiThat = tinhTrangNgoaiThat; }

    public String getYeuCauKhachHang() { return yeuCauKhachHang; }
    public void setYeuCauKhachHang(String yeuCauKhachHang) { this.yeuCauKhachHang = yeuCauKhachHang; }
}
