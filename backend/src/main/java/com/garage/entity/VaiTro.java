package com.garage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "VaiTro")
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaVaiTro")
    private Integer maVaiTro;

    @Column(name = "TenVaiTro", nullable = false, unique = true, length = 50)
    private String tenVaiTro;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    public VaiTro() {}

    public Integer getMaVaiTro() {
        return maVaiTro;
    }

    public void setMaVaiTro(Integer maVaiTro) {
        this.maVaiTro = maVaiTro;
    }

    public String getTenVaiTro() {
        return tenVaiTro;
    }

    public void setTenVaiTro(String tenVaiTro) {
        this.tenVaiTro = tenVaiTro;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
