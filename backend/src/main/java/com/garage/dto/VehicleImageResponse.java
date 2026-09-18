package com.garage.dto;

import java.time.LocalDateTime;

public class VehicleImageResponse {
    private Integer maXe;
    private String tenTep;
    private String loaiNoiDung;
    private Long kichThuoc;
    private LocalDateTime ngayCapNhat;

    public VehicleImageResponse(Integer maXe, String tenTep, String loaiNoiDung,
                                Long kichThuoc, LocalDateTime ngayCapNhat) {
        this.maXe = maXe;
        this.tenTep = tenTep;
        this.loaiNoiDung = loaiNoiDung;
        this.kichThuoc = kichThuoc;
        this.ngayCapNhat = ngayCapNhat;
    }

    public Integer getMaXe() { return maXe; }
    public String getTenTep() { return tenTep; }
    public String getLoaiNoiDung() { return loaiNoiDung; }
    public Long getKichThuoc() { return kichThuoc; }
    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
}
