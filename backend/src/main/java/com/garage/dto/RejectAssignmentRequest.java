package com.garage.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO cho thao tác từ chối phân công kỹ thuật viên.
 */
public class RejectAssignmentRequest {

    @Size(max = 500, message = "Lý do từ chối không được vượt quá 500 ký tự")
    private String ghiChu;

    public RejectAssignmentRequest() {}

    public RejectAssignmentRequest(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
