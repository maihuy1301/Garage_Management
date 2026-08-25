package com.garage.dto;

import java.time.LocalDateTime;

public class BranchResponse {

    private Integer maChiNhanh;
    private String maChiNhanhCode;
    private String tenChiNhanh;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private Boolean trangThai;
    private LocalDateTime ngayTao;

    public BranchResponse() {}

    public BranchResponse(Integer maChiNhanh, String maChiNhanhCode, String tenChiNhanh,
                          String diaChi, String soDienThoai, String email,
                          Boolean trangThai, LocalDateTime ngayTao) {
        this.maChiNhanh = maChiNhanh;
        this.maChiNhanhCode = maChiNhanhCode;
        this.tenChiNhanh = tenChiNhanh;
        this.diaChi = diaChi;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
    }

    public Integer getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(Integer maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getMaChiNhanhCode() { return maChiNhanhCode; }
    public void setMaChiNhanhCode(String maChiNhanhCode) { this.maChiNhanhCode = maChiNhanhCode; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
