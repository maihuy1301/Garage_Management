package com.garage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TonKhoId implements Serializable {

    @Column(name = "MaChiNhanh")
    private Integer maChiNhanh;

    @Column(name = "MaPhuTung")
    private Integer maPhuTung;

    public TonKhoId() {}

    public TonKhoId(Integer maChiNhanh, Integer maPhuTung) {
        this.maChiNhanh = maChiNhanh;
        this.maPhuTung = maPhuTung;
    }

    public Integer getMaChiNhanh() {
        return maChiNhanh;
    }

    public void setMaChiNhanh(Integer maChiNhanh) {
        this.maChiNhanh = maChiNhanh;
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
        TonKhoId tonKhoId = (TonKhoId) o;
        return Objects.equals(maChiNhanh, tonKhoId.maChiNhanh) && Objects.equals(maPhuTung, tonKhoId.maPhuTung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maChiNhanh, maPhuTung);
    }
}
