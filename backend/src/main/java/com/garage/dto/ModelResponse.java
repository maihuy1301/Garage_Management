package com.garage.dto;

public class ModelResponse {

    private Integer maModel;
    private Integer maHangXe;
    private String tenHangXe;
    private String tenModel;
    private Boolean trangThai;

    public ModelResponse() {}

    public ModelResponse(Integer maModel, String tenModel, Integer maHangXe, String tenHangXe) {
        this(maModel, maHangXe, tenHangXe, tenModel, true);
    }

    public ModelResponse(Integer maModel, Integer maHangXe, String tenHangXe, String tenModel) {
        this(maModel, maHangXe, tenHangXe, tenModel, true);
    }

    public ModelResponse(Integer maModel, Integer maHangXe, String tenHangXe, String tenModel, Boolean trangThai) {
        this.maModel = maModel;
        this.maHangXe = maHangXe;
        this.tenHangXe = tenHangXe;
        this.tenModel = tenModel;
        this.trangThai = trangThai;
    }

    public Integer getMaModel() {
        return maModel;
    }

    public void setMaModel(Integer maModel) {
        this.maModel = maModel;
    }

    public Integer getMaHangXe() {
        return maHangXe;
    }

    public void setMaHangXe(Integer maHangXe) {
        this.maHangXe = maHangXe;
    }

    public String getTenHangXe() {
        return tenHangXe;
    }

    public void setTenHangXe(String tenHangXe) {
        this.tenHangXe = tenHangXe;
    }

    public String getTenModel() {
        return tenModel;
    }

    public void setTenModel(String tenModel) {
        this.tenModel = tenModel;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}
