package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Xe")
public class Xe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaXe")
    private Integer maXe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaKhachHang", nullable = false)
    private KhachHang khachHang;

    @Column(name = "BienSo", nullable = false, unique = true, length = 20)
    private String bienSo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaModel")
    private ModelXe modelXe;

    @Column(name = "NamSanXuat")
    private Integer namSanXuat;

    @Column(name = "MauXe", length = 50)
    private String mauXe;

    @Column(name = "SoVIN", length = 50)
    private String soVIN;

    @Column(name = "SoKmHienTai")
    private Integer soKmHienTai = 0;

    @Column(name = "NgayTao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    public Xe() {}

    public Integer getMaXe() {
        return maXe;
    }

    public void setMaXe(Integer maXe) {
        this.maXe = maXe;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String bienSo) {
        this.bienSo = bienSo;
    }

    public ModelXe getModelXe() {
        return modelXe;
    }

    public void setModelXe(ModelXe modelXe) {
        this.modelXe = modelXe;
    }

    public String getModel() {
        return getTenModel();
    }

    public void setModel(String model) {
        if (model != null && this.modelXe == null) {
            ModelXe m = new ModelXe();
            m.setTenModel(model);
            this.modelXe = m;
        } else if (model != null && this.modelXe != null) {
            this.modelXe.setTenModel(model);
        }
    }

    public Integer getNamSanXuat() {
        return namSanXuat;
    }

    public void setNamSanXuat(Integer namSanXuat) {
        this.namSanXuat = namSanXuat;
    }

    public String getMauXe() {
        return mauXe;
    }

    public void setMauXe(String mauXe) {
        this.mauXe = mauXe;
    }

    public String getSoVIN() {
        return soVIN;
    }

    public void setSoVIN(String soVIN) {
        this.soVIN = soVIN;
    }

    public Integer getSoKmHienTai() {
        return soKmHienTai;
    }

    public void setSoKmHienTai(Integer soKmHienTai) {
        this.soKmHienTai = soKmHienTai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }

    /**
     * Helper tiện ích lấy tên Hãng xe an toàn từ ModelXe
     */
    public String getTenHangXe() {
        if (modelXe != null && modelXe.getHangXe() != null) {
            return modelXe.getHangXe().getTenHangXe();
        }
        return null;
    }

    public String getHangXe() {
        return getTenHangXe();
    }

    public void setHangXe(String hangXe) {
        // Fallback for legacy fixtures
        if (hangXe != null && this.modelXe == null) {
            ModelXe m = new ModelXe();
            HangXe h = new HangXe();
            h.setTenHangXe(hangXe);
            m.setHangXe(h);
            this.modelXe = m;
        }
    }

    /**
     * Helper tiện ích lấy tên Model an toàn
     */
    public String getTenModel() {
        if (modelXe != null) {
            return modelXe.getTenModel();
        }
        return null;
    }
}
