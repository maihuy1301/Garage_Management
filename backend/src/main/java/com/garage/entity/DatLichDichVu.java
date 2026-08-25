package com.garage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DatLich_DichVu")
public class DatLichDichVu {

    @EmbeddedId
    private DatLichDichVuId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maDatLich")
    @JoinColumn(name = "MaDatLich", nullable = false)
    private DatLich datLich;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maDichVu")
    @JoinColumn(name = "MaDichVu", nullable = false)
    private DichVu dichVu;

    @Column(name = "SoLuong")
    private Integer soLuong = 1;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    public DatLichDichVu() {}

    public DatLichDichVu(DatLich datLich, DichVu dichVu) {
        this.datLich = datLich;
        this.dichVu = dichVu;
        this.id = new DatLichDichVuId(datLich.getMaDatLich(), dichVu.getMaDichVu());
    }

    public DatLichDichVuId getId() {
        return id;
    }

    public void setId(DatLichDichVuId id) {
        this.id = id;
    }

    public DatLich getDatLich() {
        return datLich;
    }

    public void setDatLich(DatLich datLich) {
        this.datLich = datLich;
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
