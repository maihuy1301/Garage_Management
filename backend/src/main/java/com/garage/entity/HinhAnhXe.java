package com.garage.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "HinhAnhXe")
public class HinhAnhXe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHinhAnhXe")
    private Integer maHinhAnhXe;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaXe", nullable = false, unique = true)
    private Xe xe;

    @Column(name = "DuongDanAnh", nullable = false, length = 500)
    private String duongDanAnh;

    @Column(name = "TenTepGoc", nullable = false, length = 255)
    private String tenTepGoc;

    @Column(name = "LoaiNoiDung", nullable = false, length = 100)
    private String loaiNoiDung;

    @Column(name = "KichThuoc", nullable = false)
    private Long kichThuoc;

    @Column(name = "NgayCapNhat", nullable = false)
    private LocalDateTime ngayCapNhat;

    public Integer getMaHinhAnhXe() { return maHinhAnhXe; }
    public void setMaHinhAnhXe(Integer maHinhAnhXe) { this.maHinhAnhXe = maHinhAnhXe; }

    public Xe getXe() { return xe; }
    public void setXe(Xe xe) { this.xe = xe; }

    public String getDuongDanAnh() { return duongDanAnh; }
    public void setDuongDanAnh(String duongDanAnh) { this.duongDanAnh = duongDanAnh; }

    public String getTenTepGoc() { return tenTepGoc; }
    public void setTenTepGoc(String tenTepGoc) { this.tenTepGoc = tenTepGoc; }

    public String getLoaiNoiDung() { return loaiNoiDung; }
    public void setLoaiNoiDung(String loaiNoiDung) { this.loaiNoiDung = loaiNoiDung; }

    public Long getKichThuoc() { return kichThuoc; }
    public void setKichThuoc(Long kichThuoc) { this.kichThuoc = kichThuoc; }

    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
