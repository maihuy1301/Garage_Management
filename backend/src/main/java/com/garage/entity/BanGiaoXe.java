package com.garage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BanGiaoXe")
public class BanGiaoXe {
    @Id
    @Column(name = "MaTiepNhan")
    private Integer maTiepNhan;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiBanGiao", nullable = false)
    private NguoiDung nguoiBanGiao;
    @Column(name = "ThoiGianBanGiao", nullable = false)
    private LocalDateTime thoiGianBanGiao;
    @Column(name = "GhiChu", length = 500)
    private String ghiChu;
    public Integer getMaTiepNhan() { return maTiepNhan; }
    public void setMaTiepNhan(Integer value) { maTiepNhan = value; }
    public NguoiDung getNguoiBanGiao() { return nguoiBanGiao; }
    public void setNguoiBanGiao(NguoiDung value) { nguoiBanGiao = value; }
    public LocalDateTime getThoiGianBanGiao() { return thoiGianBanGiao; }
    public void setThoiGianBanGiao(LocalDateTime value) { thoiGianBanGiao = value; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String value) { ghiChu = value; }
}
