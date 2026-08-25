package com.garage.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Integer maThongBao;
    private Integer maNguoiDung;
    private String tieuDe;
    private String noiDung;
    private String loaiThongBao;
    private Integer maThamChieu;
    private Boolean daDoc;
    private LocalDateTime ngayTao;

    public NotificationResponse() {}

    public NotificationResponse(Integer maThongBao, Integer maNguoiDung, String tieuDe,
                                String noiDung, String loaiThongBao, Integer maThamChieu,
                                Boolean daDoc, LocalDateTime ngayTao) {
        this.maThongBao = maThongBao;
        this.maNguoiDung = maNguoiDung;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.loaiThongBao = loaiThongBao;
        this.maThamChieu = maThamChieu;
        this.daDoc = daDoc;
        this.ngayTao = ngayTao;
    }

    public Integer getMaThongBao() { return maThongBao; }
    public void setMaThongBao(Integer maThongBao) { this.maThongBao = maThongBao; }

    public Integer getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Integer maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getLoaiThongBao() { return loaiThongBao; }
    public void setLoaiThongBao(String loaiThongBao) { this.loaiThongBao = loaiThongBao; }

    public Integer getMaThamChieu() { return maThamChieu; }
    public void setMaThamChieu(Integer maThamChieu) { this.maThamChieu = maThamChieu; }

    public Boolean getDaDoc() { return daDoc; }
    public void setDaDoc(Boolean daDoc) { this.daDoc = daDoc; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
