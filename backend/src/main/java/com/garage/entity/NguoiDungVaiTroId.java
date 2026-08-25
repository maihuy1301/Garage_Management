package com.garage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class NguoiDungVaiTroId implements Serializable {

    @Column(name = "MaNguoiDung")
    private Integer maNguoiDung;

    @Column(name = "MaVaiTro")
    private Integer maVaiTro;

    public NguoiDungVaiTroId() {}

    public NguoiDungVaiTroId(Integer maNguoiDung, Integer maVaiTro) {
        this.maNguoiDung = maNguoiDung;
        this.maVaiTro = maVaiTro;
    }

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public Integer getMaVaiTro() {
        return maVaiTro;
    }

    public void setMaVaiTro(Integer maVaiTro) {
        this.maVaiTro = maVaiTro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NguoiDungVaiTroId that = (NguoiDungVaiTroId) o;
        return Objects.equals(maNguoiDung, that.maNguoiDung) && Objects.equals(maVaiTro, that.maVaiTro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNguoiDung, maVaiTro);
    }
}
