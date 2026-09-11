package com.garage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ModelXe", uniqueConstraints = {
    @UniqueConstraint(name = "UQ_ModelXe_HangXe", columnNames = {"MaHangXe", "TenModel"})
})
public class ModelXe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaModel")
    private Integer maModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHangXe", nullable = false)
    private HangXe hangXe;

    @Column(name = "TenModel", nullable = false, length = 100)
    private String tenModel;

    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai = true;

    public ModelXe() {}

    public ModelXe(HangXe hangXe, String tenModel) {
        this.hangXe = hangXe;
        this.tenModel = tenModel;
        this.trangThai = true;
    }

    public Integer getMaModel() {
        return maModel;
    }

    public void setMaModel(Integer maModel) {
        this.maModel = maModel;
    }

    public HangXe getHangXe() {
        return hangXe;
    }

    public void setHangXe(HangXe hangXe) {
        this.hangXe = hangXe;
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
