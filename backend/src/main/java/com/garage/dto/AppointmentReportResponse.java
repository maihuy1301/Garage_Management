package com.garage.dto;

public class AppointmentReportResponse {

    private long total;
    private long choXacNhan;
    private long daXacNhan;
    private long daTiepNhan;
    private long daHuy;

    public AppointmentReportResponse() {}

    public AppointmentReportResponse(long total, long choXacNhan, long daXacNhan, long daTiepNhan, long daHuy) {
        this.total = total;
        this.choXacNhan = choXacNhan;
        this.daXacNhan = daXacNhan;
        this.daTiepNhan = daTiepNhan;
        this.daHuy = daHuy;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getChoXacNhan() { return choXacNhan; }
    public void setChoXacNhan(long choXacNhan) { this.choXacNhan = choXacNhan; }

    public long getDaXacNhan() { return daXacNhan; }
    public void setDaXacNhan(long daXacNhan) { this.daXacNhan = daXacNhan; }

    public long getDaTiepNhan() { return daTiepNhan; }
    public void setDaTiepNhan(long daTiepNhan) { this.daTiepNhan = daTiepNhan; }

    public long getDaHuy() { return daHuy; }
    public void setDaHuy(long daHuy) { this.daHuy = daHuy; }
}
