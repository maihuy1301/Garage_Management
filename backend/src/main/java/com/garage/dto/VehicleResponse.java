package com.garage.dto;

import java.time.LocalDateTime;

/**
 * Response DTO cho Xe — không expose JPA entity trực tiếp.
 * Trả về đầy đủ thông tin Brand & Model dạng ID và Name.
 */
public class VehicleResponse {

    private Integer maXe;
    private Integer maKhachHang;
    private String tenChuXe;
    private String bienSo;
    private Integer maHangXe;
    private String tenHangXe;
    private Integer maModel;
    private String tenModel;
    private String hangXe; // Giữ để tương thích backward client cũ
    private String model;  // Giữ để tương thích backward client cũ
    private Integer namSanXuat;
    private String mauXe;
    private String soVIN;
    private Integer soKmHienTai;
    private Boolean trangThai;
    private LocalDateTime ngayTao;

    public VehicleResponse() {}

    public VehicleResponse(Integer maXe, Integer maKhachHang,
                           String tenChuXe, String bienSo,
                           String hangXe, String model,
                           Integer namSanXuat, String mauXe, String soVIN,
                           Integer soKmHienTai, Boolean trangThai, LocalDateTime ngayTao) {
        this(maXe, maKhachHang, tenChuXe, bienSo, null, hangXe, null, model, namSanXuat, mauXe, soVIN, soKmHienTai, trangThai, ngayTao);
    }

    public VehicleResponse(Integer maXe, Integer maKhachHang,
                           String tenChuXe, String bienSo,
                           Integer maHangXe, String tenHangXe,
                           Integer maModel, String tenModel,
                           Integer namSanXuat, String mauXe, String soVIN,
                           Integer soKmHienTai, Boolean trangThai, LocalDateTime ngayTao) {
        this.maXe = maXe;
        this.maKhachHang = maKhachHang;
        this.tenChuXe = tenChuXe;
        this.bienSo = bienSo;
        this.maHangXe = maHangXe;
        this.tenHangXe = tenHangXe;
        this.maModel = maModel;
        this.tenModel = tenModel;
        this.hangXe = tenHangXe;
        this.model = tenModel;
        this.namSanXuat = namSanXuat;
        this.mauXe = mauXe;
        this.soVIN = soVIN;
        this.soKmHienTai = soKmHienTai;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
    }

    public Integer getMaXe() { return maXe; }
    public void setMaXe(Integer maXe) { this.maXe = maXe; }

    public Integer getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(Integer maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenChuXe() { return tenChuXe; }
    public void setTenChuXe(String tenChuXe) { this.tenChuXe = tenChuXe; }

    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }

    public Integer getMaHangXe() { return maHangXe; }
    public void setMaHangXe(Integer maHangXe) { this.maHangXe = maHangXe; }

    public String getTenHangXe() { return tenHangXe; }
    public void setTenHangXe(String tenHangXe) { this.tenHangXe = tenHangXe; }

    public Integer getMaModel() { return maModel; }
    public void setMaModel(Integer maModel) { this.maModel = maModel; }

    public String getTenModel() { return tenModel; }
    public void setTenModel(String tenModel) { this.tenModel = tenModel; }

    public String getHangXe() { return hangXe != null ? hangXe : tenHangXe; }
    public void setHangXe(String hangXe) { this.hangXe = hangXe; }

    public String getModel() { return model != null ? model : tenModel; }
    public void setModel(String model) { this.model = model; }

    public Integer getNamSanXuat() { return namSanXuat; }
    public void setNamSanXuat(Integer namSanXuat) { this.namSanXuat = namSanXuat; }

    public String getMauXe() { return mauXe; }
    public void setMauXe(String mauXe) { this.mauXe = mauXe; }

    public String getSoVIN() { return soVIN; }
    public void setSoVIN(String soVIN) { this.soVIN = soVIN; }

    public Integer getSoKmHienTai() { return soKmHienTai; }
    public void setSoKmHienTai(Integer soKmHienTai) { this.soKmHienTai = soKmHienTai; }

    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
