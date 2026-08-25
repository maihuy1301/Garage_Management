package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ThongBao")
public class ThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThongBao")
    private Integer maThongBao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "TieuDe", nullable = false, length = 200)
    private String tieuDe;

    @Column(name = "NoiDung", length = 1000)
    private String noiDung;

    @Column(name = "LoaiThongBao", length = 50)
    private String loaiThongBao;

    @Column(name = "MaThamChieu")
    private Integer maThamChieu;

    @Column(name = "DaDoc")
    private Boolean daDoc = false;

    @Column(name = "NgayTao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    public ThongBao() {}

    public Integer getMaThongBao() {
        return maThongBao;
    }

    public void setMaThongBao(Integer maThongBao) {
        this.maThongBao = maThongBao;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getLoaiThongBao() {
        return loaiThongBao;
    }

    public void setLoaiThongBao(String loaiThongBao) {
        this.loaiThongBao = loaiThongBao;
    }

    public Integer getMaThamChieu() {
        return maThamChieu;
    }

    public void setMaThamChieu(Integer maThamChieu) {
        this.maThamChieu = maThamChieu;
    }

    public Boolean getDaDoc() {
        return daDoc;
    }

    public void setDaDoc(Boolean daDoc) {
        this.daDoc = daDoc;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
