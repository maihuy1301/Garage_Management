package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendMessageRequest {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Size(max = 2000, message = "Nội dung tin nhắn tối đa 2000 ký tự")
    private String noiDung;

    private String duongDanTep;

    public SendMessageRequest() {}

    public SendMessageRequest(String noiDung) {
        this.noiDung = noiDung;
    }

    public SendMessageRequest(String noiDung, String duongDanTep) {
        this.noiDung = noiDung;
        this.duongDanTep = duongDanTep;
    }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getDuongDanTep() { return duongDanTep; }
    public void setDuongDanTep(String duongDanTep) { this.duongDanTep = duongDanTep; }
}
