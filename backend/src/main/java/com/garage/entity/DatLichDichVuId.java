package com.garage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DatLichDichVuId implements Serializable {

    @Column(name = "MaDatLich")
    private Integer maDatLich;

    @Column(name = "MaDichVu")
    private Integer maDichVu;

    public DatLichDichVuId() {}

    public DatLichDichVuId(Integer maDatLich, Integer maDichVu) {
        this.maDatLich = maDatLich;
        this.maDichVu = maDichVu;
    }

    public Integer getMaDatLich() {
        return maDatLich;
    }

    public void setMaDatLich(Integer maDatLich) {
        this.maDatLich = maDatLich;
    }

    public Integer getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(Integer maDichVu) {
        this.maDichVu = maDichVu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatLichDichVuId that = (DatLichDichVuId) o;
        return Objects.equals(maDatLich, that.maDatLich) && Objects.equals(maDichVu, that.maDichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDatLich, maDichVu);
    }
}
