package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO tạo xe mới.
 * Backend tự xác định owner từ JWT — client không được gửi customerId.
 */
public class CreateVehicleRequest {

    /**
     * Chỉ SYSTEM_ADMIN cần truyền field này khi tạo xe cho khách hàng cụ thể.
     * CUSTOMER: backend tự xác định owner từ JWT — field này bị bỏ qua.
     */
    private Integer maKhachHang;

    @NotBlank(message = "Biển số xe không được để trống")
    @Size(max = 20, message = "Biển số xe không được vượt quá 20 ký tự")
    private String bienSo;

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

    public CreateVehicleRequest() {}

    public Integer getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(Integer maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }

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
