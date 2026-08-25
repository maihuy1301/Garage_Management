package com.garage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "NguoiDung_VaiTro")
public class NguoiDungVaiTro {

    @EmbeddedId
    private NguoiDungVaiTroId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maNguoiDung")
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maVaiTro")
    @JoinColumn(name = "MaVaiTro", nullable = false)
    private VaiTro vaiTro;

    public NguoiDungVaiTro() {}

    public NguoiDungVaiTro(NguoiDung nguoiDung, VaiTro vaiTro) {
        this.nguoiDung = nguoiDung;
        this.vaiTro = vaiTro;
        this.id = new NguoiDungVaiTroId(nguoiDung.getMaNguoiDung(), vaiTro.getMaVaiTro());
    }

    public NguoiDungVaiTroId getId() {
        return id;
    }

    public void setId(NguoiDungVaiTroId id) {
        this.id = id;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(VaiTro vaiTro) {
        this.vaiTro = vaiTro;
    }
}
