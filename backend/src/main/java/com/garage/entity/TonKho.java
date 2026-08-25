package com.garage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TonKho")
public class TonKho {

    @EmbeddedId
    private TonKhoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maChiNhanh")
    @JoinColumn(name = "MaChiNhanh", nullable = false)
    private ChiNhanh chiNhanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maPhuTung")
    @JoinColumn(name = "MaPhuTung", nullable = false)
    private PhuTung phuTung;

    @Column(name = "SoLuongTon")
    private Integer soLuongTon = 0;

    @Column(name = "SoLuongToiThieu")
    private Integer soLuongToiThieu = 0;

    public TonKho() {}

    public TonKho(ChiNhanh chiNhanh, PhuTung phuTung) {
        this.chiNhanh = chiNhanh;
        this.phuTung = phuTung;
        this.id = new TonKhoId(chiNhanh.getMaChiNhanh(), phuTung.getMaPhuTung());
    }

    public TonKhoId getId() {
        return id;
    }

    public void setId(TonKhoId id) {
        this.id = id;
    }

    public ChiNhanh getChiNhanh() {
        return chiNhanh;
    }

    public void setChiNhanh(ChiNhanh chiNhanh) {
        this.chiNhanh = chiNhanh;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        this.phuTung = phuTung;
    }

    public Integer getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(Integer soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public Integer getSoLuongToiThieu() {
        return soLuongToiThieu;
    }

    public void setSoLuongToiThieu(Integer soLuongToiThieu) {
        this.soLuongToiThieu = soLuongToiThieu;
    }
}
