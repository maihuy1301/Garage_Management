package com.garage.dto;

public class RepairOrderReportResponse {

    private long total;
    private long choXuLy;
    private long daPhanCong;
    private long dangSua;
    private long choKhachDuyet;
    private long tamDung;
    private long hoanTat;
    private long huy;

    public RepairOrderReportResponse() {}

    public RepairOrderReportResponse(long total, long choXuLy, long daPhanCong, long dangSua,
                                     long choKhachDuyet, long tamDung, long hoanTat, long huy) {
        this.total = total;
        this.choXuLy = choXuLy;
        this.daPhanCong = daPhanCong;
        this.dangSua = dangSua;
        this.choKhachDuyet = choKhachDuyet;
        this.tamDung = tamDung;
        this.hoanTat = hoanTat;
        this.huy = huy;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getChoXuLy() { return choXuLy; }
    public void setChoXuLy(long choXuLy) { this.choXuLy = choXuLy; }

    public long getDaPhanCong() { return daPhanCong; }
    public void setDaPhanCong(long daPhanCong) { this.daPhanCong = daPhanCong; }

    public long getDangSua() { return dangSua; }
    public void setDangSua(long dangSua) { this.dangSua = dangSua; }

    public long getChoKhachDuyet() { return choKhachDuyet; }
    public void setChoKhachDuyet(long choKhachDuyet) { this.choKhachDuyet = choKhachDuyet; }

    public long getTamDung() { return tamDung; }
    public void setTamDung(long tamDung) { this.tamDung = tamDung; }

    public long getHoanTat() { return hoanTat; }
    public void setHoanTat(long hoanTat) { this.hoanTat = hoanTat; }

    public long getHuy() { return huy; }
    public void setHuy(long huy) { this.huy = huy; }
}
