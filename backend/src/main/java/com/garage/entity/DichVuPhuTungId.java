package com.garage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DichVuPhuTungId implements Serializable {

    @Column(name = "MaDichVu")
    private Integer maDichVu;

    @Column(name = "MaPhuTung")
    private Integer maPhuTung;

    public DichVuPhuTungId() {}

    public DichVuPhuTungId(Integer maDichVu, Integer maPhuTung) {
        this.maDichVu = maDichVu;
        this.maPhuTung = maPhuTung;
    }

    public Integer getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(Integer maDichVu) {
        this.maDichVu = maDichVu;
    }

    public Integer getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(Integer maPhuTung) {
        this.maPhuTung = maPhuTung;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DichVuPhuTungId that = (DichVuPhuTungId) o;
        return Objects.equals(maDichVu, that.maDichVu) && Objects.equals(maPhuTung, that.maPhuTung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDichVu, maPhuTung);
    }
}
