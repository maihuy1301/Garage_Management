package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "KiemTraXe")
public class KiemTraXe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKiemTra")
    private Integer maKiemTra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTiepNhan", nullable = false)
    private PhieuTiepNhan phieuTiepNhan;

    @Column(name = "NoiDungKiemTra", nullable = false, length = 500)
    private String noiDungKiemTra;

    @Column(name = "KetQua", length = 1000)
    private String ketQua;

    @Column(name = "MucDo", length = 30)
    private String mucDo;

    @Column(name = "NgayKiemTra", insertable = false, updatable = false)
    private LocalDateTime ngayKiemTra;

    public KiemTraXe() {}

    public Integer getMaKiemTra() {
        return maKiemTra;
    }

    public void setMaKiemTra(Integer maKiemTra) {
        this.maKiemTra = maKiemTra;
    }

    public PhieuTiepNhan getPhieuTiepNhan() {
        return phieuTiepNhan;
    }

    public void setPhieuTiepNhan(PhieuTiepNhan phieuTiepNhan) {
        this.phieuTiepNhan = phieuTiepNhan;
    }

    public String getNoiDungKiemTra() {
        return noiDungKiemTra;
    }

    public void setNoiDungKiemTra(String noiDungKiemTra) {
        this.noiDungKiemTra = noiDungKiemTra;
    }

    public String getKetQua() {
        return ketQua;
    }

    public void setKetQua(String ketQua) {
        this.ketQua = ketQua;
    }

    public String getMucDo() {
        return mucDo;
    }

    public void setMucDo(String mucDo) {
        this.mucDo = mucDo;
    }

    public LocalDateTime getNgayKiemTra() {
        return ngayKiemTra;
    }

    public void setNgayKiemTra(LocalDateTime ngayKiemTra) {
        this.ngayKiemTra = ngayKiemTra;
    }
}
