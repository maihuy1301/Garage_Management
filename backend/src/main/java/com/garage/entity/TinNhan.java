package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TinNhan")
public class TinNhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTinNhan")
    private Integer maTinNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaCuocHoiThoai", nullable = false)
    private CuocHoiThoai cuocHoiThoai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiGui", nullable = false)
    private NguoiDung nguoiGui;

    @Column(name = "NoiDung", length = 2000)
    private String noiDung;

    @Column(name = "DuongDanTep", length = 500)
    private String duongDanTep;

    @Column(name = "DaDoc")
    private Boolean daDoc = false;

    @Column(name = "ThoiGianGui", insertable = false, updatable = false)
    private LocalDateTime thoiGianGui;

    public TinNhan() {}

    public Integer getMaTinNhan() {
        return maTinNhan;
    }

    public void setMaTinNhan(Integer maTinNhan) {
        this.maTinNhan = maTinNhan;
    }

    public CuocHoiThoai getCuocHoiThoai() {
        return cuocHoiThoai;
    }

    public void setCuocHoiThoai(CuocHoiThoai cuocHoiThoai) {
        this.cuocHoiThoai = cuocHoiThoai;
    }

    public NguoiDung getNguoiGui() {
        return nguoiGui;
    }

    public void setNguoiGui(NguoiDung nguoiGui) {
        this.nguoiGui = nguoiGui;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getDuongDanTep() {
        return duongDanTep;
    }

    public void setDuongDanTep(String duongDanTep) {
        this.duongDanTep = duongDanTep;
    }

    public Boolean getDaDoc() {
        return daDoc;
    }

    public void setDaDoc(Boolean daDoc) {
        this.daDoc = daDoc;
    }

    public LocalDateTime getThoiGianGui() {
        return thoiGianGui;
    }

    public void setThoiGianGui(LocalDateTime thoiGianGui) {
        this.thoiGianGui = thoiGianGui;
    }
}
