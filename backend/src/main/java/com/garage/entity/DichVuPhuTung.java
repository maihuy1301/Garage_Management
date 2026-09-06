package com.garage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DichVu_PhuTung")
public class DichVuPhuTung {

    @EmbeddedId
    private DichVuPhuTungId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maDichVu")
    @JoinColumn(name = "MaDichVu", nullable = false)
    private DichVu dichVu;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maPhuTung")
    @JoinColumn(name = "MaPhuTung", nullable = false)
    private PhuTung phuTung;

    public DichVuPhuTung() {}

    public DichVuPhuTung(DichVu dichVu, PhuTung phuTung) {
        this.dichVu = dichVu;
        this.phuTung = phuTung;
        this.id = new DichVuPhuTungId(dichVu.getMaDichVu(), phuTung.getMaPhuTung());
    }

    public DichVuPhuTungId getId() {
        return id;
    }

    public void setId(DichVuPhuTungId id) {
        this.id = id;
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        this.phuTung = phuTung;
    }
}
