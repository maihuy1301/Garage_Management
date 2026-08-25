package com.garage.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO cập nhật thông tin xe.
 * Không cho phép client thay đổi owner (maKhachHang/bienSo unique là controlled).
 */
public class UpdateVehicleRequest {

    @Size(max = 50, message = "Hãng xe không được vượt quá 50 ký tự")
    private String hangXe;

    @Size(max = 100, message = "Model xe không được vượt quá 100 ký tự")
    private String model;

    private Integer namSanXuat;

    @Size(max = 50, message = "Màu xe không được vượt quá 50 ký tự")
    private String mauXe;

    @Size(max = 50, message = "Số VIN không được vượt quá 50 ký tự")
    private String soVIN;

    private Integer soKmHienTai;

    public UpdateVehicleRequest() {}

    public String getHangXe() { return hangXe; }
    public void setHangXe(String hangXe) { this.hangXe = hangXe; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getNamSanXuat() { return namSanXuat; }
    public void setNamSanXuat(Integer namSanXuat) { this.namSanXuat = namSanXuat; }

    public String getMauXe() { return mauXe; }
    public void setMauXe(String mauXe) { this.mauXe = mauXe; }

    public String getSoVIN() { return soVIN; }
    public void setSoVIN(String soVIN) { this.soVIN = soVIN; }

    public Integer getSoKmHienTai() { return soKmHienTai; }
    public void setSoKmHienTai(Integer soKmHienTai) { this.soKmHienTai = soKmHienTai; }
}
