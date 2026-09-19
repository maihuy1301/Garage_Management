package com.garage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO để phân công kỹ thuật viên cho Phiếu sửa chữa.
 */
public class CreateAssignmentRequest {

    @NotNull(message = "Mã nhân viên kỹ thuật không được để trống")
    private Integer technicianId;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String ghiChu;

    public CreateAssignmentRequest() {}

    public CreateAssignmentRequest(Integer technicianId) {
        this.technicianId = technicianId;
    }

    public CreateAssignmentRequest(Integer technicianId, String ghiChu) {
        this.technicianId = technicianId;
        this.ghiChu = ghiChu;
    }

    public Integer getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Integer technicianId) {
        this.technicianId = technicianId;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getVaiTroTrongCongViec() {
        return ghiChu;
    }

    public void setVaiTroTrongCongViec(String vaiTroTrongCongViec) {
        this.ghiChu = vaiTroTrongCongViec;
    }
}
