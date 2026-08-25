package com.garage.dto;

import java.time.LocalDateTime;

public class ConversationResponse {

    private Integer maCuocHoiThoai;
    private Integer maKhachHang;
    private String tenKhachHang;
    private Integer maNhanVien;
    private String tenNhanVien;
    private Integer maTiepNhan;
    private String bienSoXe;
    private String trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhatCuoi;
    private long unreadCount;
    private ChatMessageResponse lastMessage;

    public ConversationResponse() {}

    public ConversationResponse(Integer maCuocHoiThoai, Integer maKhachHang, String tenKhachHang,
                                Integer maNhanVien, String tenNhanVien, Integer maTiepNhan,
                                String bienSoXe, String trangThai, LocalDateTime ngayTao,
                                LocalDateTime ngayCapNhatCuoi, long unreadCount,
                                ChatMessageResponse lastMessage) {
        this.maCuocHoiThoai = maCuocHoiThoai;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.maTiepNhan = maTiepNhan;
        this.bienSoXe = bienSoXe;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.ngayCapNhatCuoi = ngayCapNhatCuoi;
        this.unreadCount = unreadCount;
        this.lastMessage = lastMessage;
    }

    public Integer getMaCuocHoiThoai() { return maCuocHoiThoai; }
    public void setMaCuocHoiThoai(Integer maCuocHoiThoai) { this.maCuocHoiThoai = maCuocHoiThoai; }

    public Integer getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(Integer maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public Integer getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(Integer maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public Integer getMaTiepNhan() { return maTiepNhan; }
    public void setMaTiepNhan(Integer maTiepNhan) { this.maTiepNhan = maTiepNhan; }

    public String getBienSoXe() { return bienSoXe; }
    public void setBienSoXe(String bienSoXe) { this.bienSoXe = bienSoXe; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public LocalDateTime getNgayCapNhatCuoi() { return ngayCapNhatCuoi; }
    public void setNgayCapNhatCuoi(LocalDateTime ngayCapNhatCuoi) { this.ngayCapNhatCuoi = ngayCapNhatCuoi; }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }

    public ChatMessageResponse getLastMessage() { return lastMessage; }
    public void setLastMessage(ChatMessageResponse lastMessage) { this.lastMessage = lastMessage; }
}
